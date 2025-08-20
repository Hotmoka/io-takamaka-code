/*
Copyright 2021 Fausto Spoto

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package io.takamaka.code.governance.tendermint;

import static io.takamaka.code.lang.Takamaka.event;
import static io.takamaka.code.lang.Takamaka.isSystemCall;
import static io.takamaka.code.lang.Takamaka.require;
import static java.math.BigInteger.ZERO;

import java.math.BigInteger;
import java.util.function.Function;

import io.takamaka.code.governance.AbstractValidators;
import io.takamaka.code.governance.Manifest;
import io.takamaka.code.governance.ValidatorsUpdate;
import io.takamaka.code.lang.Event;
import io.takamaka.code.lang.Exported;
import io.takamaka.code.lang.FromContract;
import io.takamaka.code.lang.Payable;
import io.takamaka.code.lang.PayableContract;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.StringSupport;
import io.takamaka.code.lang.View;
import io.takamaka.code.math.BigIntegerSupport;
import io.takamaka.code.util.StorageLinkedList;
import io.takamaka.code.util.StorageList;
import io.takamaka.code.util.StorageMap;
import io.takamaka.code.util.StorageTreeMap;

/**
 * The validators of a Tendermint blockchain. They have an ED25519 public key
 * and an id derived from the public key, according to the algorithm used by Tendermint.
 */
public class TendermintValidators extends AbstractValidators<TendermintED25519Validator> {

	/**
	 * The amount of rewards that gets staked. The rest is sent to the validators immediately.
	 * 1000000 = 1%.
	 */
	private final int percentStaked;

	/**
	 * Extra tax paid when a validator acquires the shares of another validator
	 * (in percent of the offer cost). 1000000 = 1%.
	 */
	private final int buyerSurcharge;

	/**
	 * The percent of stake that gets slashed for each misbehaving. 1000000 means 1%.
	 */
	private final int slashingForMisbehaving;

	/**
	 * The percent of stake that gets slashed for not behaving (no vote). 1000000 means 1%.
	 */
	private final int slashingForNotBehaving;

	/**
	 * The number of times that a validators didn't behave (didn't answer) in the
	 * immediately previous rewards. If this reaches zero, they will be slashed.
	 */
	private final StorageMap<String, BigInteger> alreadyNotBehaving = new StorageTreeMap<>();

	/**
	 * Creates a set of validators of a Tendermint blockchain.
	 * 
	 * @param manifest the manifest of the node having these validators
	 * @param validators the initial validators
	 * @param powers the initial powers of the initial validators
	 * @param ticketForNewPoll the amount of coins to pay for starting a new poll among the validators;
	 *                         both {@link #newPoll(BigInteger, io.takamaka.code.dao.SimplePoll.Action)} and
	 *                         {@link #newPoll(BigInteger, io.takamaka.code.dao.SimplePoll.Action, long, long)}
	 *                         require to pay this amount for starting a poll
	 * @param finalSupply the final supply of coins that will be reached, eventually
	 * @param heightAtFinalSupply the height after which coins are not minted anymore and the current
	 *                            supply reaches the final supply
	 * @param percentStaked the amount of rewards that gets staked. The rest is sent to the validators immediately.
	 *                      1000000 = 1%
	 * @param buyerSurcharge the extra tax paid when a validator acquires the shares of another validator
	 *                       (in percent of the offer cost). 1000000 = 1%
	 * @param slashingForMisbehaving the percent of stake that gets slashed for each misbehaving. 1000000 means 1%
	 * @param slashingForNotBehaving the percent of stake that gets slashed for not behaving (no vote). 1000000 means 1%
	 */
	private TendermintValidators(Manifest<TendermintED25519Validator> manifest, TendermintED25519Validator[] validators,
			BigInteger[] powers, BigInteger ticketForNewPoll, BigInteger finalSupply, BigInteger heightAtFinalSupply,
			int percentStaked, int buyerSurcharge, int slashingForMisbehaving, int slashingForNotBehaving) {

		super(manifest, validators, powers, ticketForNewPoll, finalSupply, heightAtFinalSupply);
		
		this.percentStaked = percentStaked;
		this.buyerSurcharge = buyerSurcharge;
		this.slashingForMisbehaving = slashingForMisbehaving;
		this.slashingForNotBehaving = slashingForNotBehaving;
	}

	@Override
	public @FromContract(PayableContract.class) @Payable void accept(BigInteger amount, TendermintED25519Validator buyer, Offer<TendermintED25519Validator> offer) {
		// it is important to redefine this method, so that the same method with
		// argument of type PayableContract is redefined by the compiler with a bridge method
		// that casts the argument to Validator and calls this method. In this way
		// only instances of Validator can become shareholders (ie, actual validators)

		BigInteger costWithSurchage = BigIntegerSupport.divide(BigIntegerSupport.multiply(offer.cost, BigInteger.valueOf(buyerSurcharge + 100_000_000L)), _100_000_000);
		require(BigIntegerSupport.compareTo(costWithSurchage, amount) <= 0, StringSupport.concat("not enough money to accept the offer: you need ", costWithSurchage));
		super.accept(amount, buyer, offer);

		// if the seller is not a validator anymore, we send to it its staked coins
		TendermintED25519Validator seller = offer.seller;
		if (sharesOf(seller).signum() == 0) {
			seller.receive(getStake(seller));
			getStakes().remove(seller);
		}

		event(new ValidatorsUpdate());
	}

	/**
	 * Rewards validators that behaved correctly and punishes validators that
	 * misbehaved. Hotmoka nodes based on Tendermint call this method at regular
	 * intervals, after each committed block in the blockchain.
	 * Its goal is to reward the behaving validators and punish the
	 * misbehaving ones. Note that a validator might not be in
	 * {@code behaving} nor in {@code misbehaving} if, for instance, it
	 * failed to vote because it was down. The implementation of this
	 * method can decide what to do in that case.
	 * Normally, it is expected that the identifiers in {@code behaving}
	 * and {@code misbehaving} are those of validators in this validators set.
	 * 
	 * @param amount the amount to distribute to the validators
	 * @param minted the subset of {@code amount} that has been minted during the last reward;
	 *               this means that {@code amount} is the sum of gas costs incurred by the
	 *               payers of the transactions and an extra inflation that is exactly {@code minted} coins
	 * @param behaving space-separated identifiers of validators that behaved correctly
	 * @param misbehaving space-separated identifiers of validators that misbehaved
	 * @param gasConsumed the gas consumed for CPU, RAM usage or storage by the transactions
	 *                    executed since the previous reward
	 * @param numberOfTransactionsSinceLastReward the number of transactions executed since the previous reward
	 */
	@FromContract @Payable public void reward(BigInteger amount, BigInteger minted, String behaving, String misbehaving, BigInteger gasConsumed, BigInteger numberOfTransactionsSinceLastReward) {
		require(isSystemCall(), "the validators can only be rewarded with a system request");

		String[] behavingIDs = splitAtSpaces(behaving);
		String[] misbehavingIDs = splitAtSpaces(misbehaving);
		rewardBehavingValidators(behavingIDs);
		slashMisbehavingValidators(misbehavingIDs);
		slashNotBehavingValidators(behavingIDs, misbehavingIDs);
		updateGasPrice(gasConsumed);
		updateParameters(minted, numberOfTransactionsSinceLastReward);
	}

	/**
	 * Yields the percent of validators' rewards that gets staked. The rest is sent to the validators immediately.
	 * 1000000 = 1%.
	 * 
	 * @return the percent of validators' reward that gets staked
	 */
	public final @View int getPercentStaked() {
		return percentStaked;
	}

	/**
	 * Yields the extra tax paid when a validator acquires the shares of another validator
	 * (in percent of the sale offer cost).
	 * 
	 * @return the extra tax paid. 1000000 means 1%
	 */
	public final @View int getBuyerSurcharge() {
		return buyerSurcharge;
	}

	/**
	 * Yields the slashing percent applied to stakes for each misbehavior.
	 * 
	 * @return the slashing percent. 1000000 means 1%
	 */
	public final @View int getSlashingForMisbehaving() {
		return slashingForMisbehaving;
	}

	/**
	 * Yields the slashing percent applied to stakes for no misbehavior (no vote).
	 * 
	 * @return the slashing percent. 1000000 means 1%
	 */
	public final @View int getSlashingForNotBehaving() {
		return slashingForNotBehaving;
	}

	private void rewardBehavingValidators(String[] behavingIDs) {
		if (behavingIDs.length > 0) {
			// compute the total power of the well behaving validators; this is always positive
			class WrappedBigInteger {
				private BigInteger bi = ZERO;
			}

			var wbi = new WrappedBigInteger();

			getShares().forEachKey(validator -> {
				if (contains(behavingIDs, validator.id())) {
					wbi.bi = BigIntegerSupport.add(wbi.bi, sharesOf(validator));
				}
			});

			for (String id: behavingIDs)
				alreadyNotBehaving.remove(id);

			// compute the total amount of staked coins
			var wbi2 = new WrappedBigInteger();
			var stakes = getStakes();
	    	stakes.forEachValue(value -> wbi2.bi = BigIntegerSupport.add(wbi2.bi, value));
			BigInteger totalStaked = wbi2.bi;

			// compute the balance that is not staked and must be distributed
			BigInteger toDistribute = BigIntegerSupport.subtract(balance(), totalStaked);

			if (toDistribute.signum() > 0) {
				// percentStaked of the distribution gets staked for the well-behaving validators, in proportion to their power
				final BigInteger addedToStakes = BigIntegerSupport.divide(BigIntegerSupport.multiply(toDistribute, BigInteger.valueOf(percentStaked)), _100_000_000);
				getShares().forEachKey(validator -> {
					if (contains(behavingIDs, validator.id())) {
						BigInteger toAdd = BigIntegerSupport.divide(BigIntegerSupport.multiply(addedToStakes, sharesOf(validator)), wbi.bi);
						BigInteger old = stakes.get(validator);
						if (old == null)
							stakes.put(validator, toAdd);
						else if (toAdd.signum() != 0) // adding 0 modifies the state for nothing
							stakes.update(validator, bi -> BigIntegerSupport.add(bi, toAdd));
					}
				});

				// distribute immediately the rest to the well-behaving validators, in proportion to their power
				final BigInteger paid = BigIntegerSupport.subtract(toDistribute, addedToStakes);
				getShares().forEachKey(validator -> {
					if (contains(behavingIDs, validator.id()))
						validator.receive(BigIntegerSupport.divide(BigIntegerSupport.multiply(paid, sharesOf(validator)), wbi.bi));
				});
			}
		}
	}

	private void slashMisbehavingValidators(String[] misbehavingIDs) {
		if (misbehavingIDs.length > 0) {
			getShares().forEachKey(validator -> {
				if (contains(misbehavingIDs, validator.id()))
					slashForMisbehaving(validator);
			});
		}
	}

	private void slashNotBehavingValidators(String[] behavingIDs, String[] misbehavingIDs) {
		getShares().forEachKey(validator -> {
			if (!contains(behavingIDs, validator.id()) && !contains(misbehavingIDs, validator.id()))
				slashForNotBehaving(validator);
		});
	}

	private void slashForNotBehaving(TendermintED25519Validator validator) {
		String id = validator.id();

		// we tolerate a slight delay before starting slashing for not voting:
		// this is important for Tendermint nodes, because Tendermint
		// does not change the set of validators immediately hence a couple
		// of blocks are created without the vote of a new validator after it gets added
		if (BigIntegerSupport.equals(BigInteger.ZERO, alreadyNotBehaving.get(id)))
			slash(validator, slashingForNotBehaving);
		else
			alreadyNotBehaving.update(id, BigInteger.valueOf(3L), old -> BigIntegerSupport.subtract(old, BigInteger.ONE));
	}

	private void slashForMisbehaving(TendermintED25519Validator validator) {
		slash(validator, slashingForMisbehaving);
	}

	private void slash(TendermintED25519Validator validator, int percent) {
		BigInteger oldStakes = getStake(validator);
		BigInteger newStakes = BigIntegerSupport.divide(BigIntegerSupport.multiply(oldStakes, BigInteger.valueOf(100_000_000L - percent)), _100_000_000);
		event(new ValidatorSlashed(validator, BigIntegerSupport.subtract(oldStakes, newStakes)));

		if (newStakes.signum() == 0) {
			// if the staked coins reached zero, we remove the validator altogether
			removeShareholderAndDistributeToOthers(validator);
			getStakes().remove(validator);
			event(new ValidatorsUpdate());
		}
		else
			getStakes().put(validator, newStakes);
	}

	private static boolean contains(String[] array, String element) {
		for (String a: array)
			if (StringSupport.equals(a, element))
				return true;

		return false;
	}

	/**
	 * An event triggered when a validator gets slashed for misbehaving.
	 */
	public final class ValidatorSlashed extends Event {

		/**
		 * The slashed validator.
		 */
		public final TendermintED25519Validator validator;

		/**
		 * The amount of stakes slashed from the validator.
		 */
		public final BigInteger amount;

		/**
		 * Creates the event.
		 * 
		 * @param validator the slashed validator
		 * @param amount the amount of stakes slashed from the validator
		 */
		private @FromContract ValidatorSlashed(TendermintED25519Validator validator, BigInteger amount) {
			this.validator = validator;
			this.amount = amount;
		}
	}

	/**
	 * The builder  of a tendermint validators object.
	 */
	@Exported
	public static class Builder extends Storage implements Function<Manifest<TendermintED25519Validator>, TendermintValidators> {
		private final StorageList<TendermintED25519Validator> validators = new StorageLinkedList<>();
		private final StorageList<BigInteger> powers = new StorageLinkedList<>();
		private final BigInteger ticketForNewPoll;
		private final BigInteger finalSupply;
		private final BigInteger heightAtFinalSupply;
		private final int percentStaked;
		private final int buyerSurcharge;
		private final int slashingForMisbehaving;
		private final int slashingForNotBehaving;

		/**
		 * Creates the builder of a set of validators of a Tendermint blockchain.
		 * 
		 * @param ticketForNewPoll the amount of coins to pay for starting a new poll among the validators;
		 *                         both {@link #newPoll(BigInteger, io.takamaka.code.dao.SimplePoll.Action)} and
		 *                         {@link #newPoll(BigInteger, io.takamaka.code.dao.SimplePoll.Action, long, long)}
		 *                         require to pay this amount for starting a poll
		 * @param finalSupply the final supply of coins that will be reached, eventually
		 * @param heightAtFinalSupply the height after which coins are not minted anymore and the current
		 *                            supply reaches the final supply
		 * @param percentStaked the amount of rewards that gets staked. The rest is sent to the validators immediately.
		 *                      1000000 = 1%
		 * @param buyerSurcharge the extra tax paid when a validator acquires the shares of another validator
		 *                       (in percent of the offer cost). 1000000 = 1%
		 * @param slashingForMisbehaving the percent of stake that gets slashed for each misbehaving. 1000000 means 1%
		 * @param slashingForNotBehaving the percent of stake that gets slashed for not behaving (no vote). 1000000 means 1%
		 */
		public Builder(BigInteger ticketForNewPoll, BigInteger finalSupply, BigInteger heightAtFinalSupply, int percentStaked, int buyerSurcharge, int slashingForMisbehaving, int slashingForNotBehaving) {
			this.ticketForNewPoll = ticketForNewPoll;
			this.finalSupply = finalSupply;
			this.heightAtFinalSupply = heightAtFinalSupply;
			this.percentStaked = percentStaked;
			this.buyerSurcharge = buyerSurcharge;
			this.slashingForMisbehaving = slashingForMisbehaving;
			this.slashingForNotBehaving = slashingForNotBehaving;
		}

		/**
		 * Adds a new validator to this builder.
		 * 
		 * @param publicKey the public key of the validator
		 * @param power the power of the added validator
		 */
		public void addValidator(String publicKey, long power) {
			validators.add(new TendermintED25519Validator(publicKey));
			powers.add(BigInteger.valueOf(power));
		}

		@Override
		public TendermintValidators apply(Manifest<TendermintED25519Validator> manifest) {
			return new TendermintValidators(manifest, validators.toArray(TendermintED25519Validator[]::new),
				powers.toArray(BigInteger[]::new), ticketForNewPoll, finalSupply, heightAtFinalSupply, percentStaked, buyerSurcharge, slashingForMisbehaving, slashingForNotBehaving);
		}
	}
}