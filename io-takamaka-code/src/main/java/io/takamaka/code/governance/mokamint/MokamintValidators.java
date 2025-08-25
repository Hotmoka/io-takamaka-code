/*
Copyright 2024 Fausto Spoto

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

package io.takamaka.code.governance.mokamint;

import static io.takamaka.code.lang.Takamaka.isSystemCall;
import static io.takamaka.code.lang.Takamaka.require;

import java.math.BigInteger;
import java.util.function.Function;

import io.takamaka.code.governance.AbstractValidators;
import io.takamaka.code.governance.Manifest;
import io.takamaka.code.governance.Validator;
import io.takamaka.code.lang.Exported;
import io.takamaka.code.lang.FromContract;
import io.takamaka.code.lang.Payable;
import io.takamaka.code.lang.PayableContract;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.math.BigIntegerSupport;

/**
 * The validators of a Hotmoka node based on Mokamint. It is empty, since such nodes
 * do not implement consensus based on validators. It just contains the code for rewarding
 * the node that creates a block and the miner that provides the deadline in the block,
 * and the code for updating the chain parameters and gas price.
 */
public class MokamintValidators extends AbstractValidators<Validator> {

	/**
	 * Creates the (empty) set of validators of a Hotmoka node based on Mokamint.
	 * 
	 * @param manifest the manifest of the node having these validators
	 * @param ticketForNewPoll the amount of coins to pay for starting a new poll among the validators;
	 *                         both {@link #newPoll(BigInteger, io.takamaka.code.dao.SimplePoll.Action)} and
	 *                         {@link #newPoll(BigInteger, io.takamaka.code.dao.SimplePoll.Action, long, long)}
	 *                         require to pay this amount for starting a poll
	 * @param finalSupply the final supply of coins that will be reached, eventually
	 * @param heightAtFinalSupply the height after which coins are not minted anymore and the current
	 *                            supply reaches the final supply
	 */
	private MokamintValidators(Manifest<Validator> manifest, BigInteger ticketForNewPoll, BigInteger finalSupply, BigInteger heightAtFinalSupply) {
		super(manifest, new Validator[0], new BigInteger[0], ticketForNewPoll, finalSupply, heightAtFinalSupply);
	}

	@Override
	public @FromContract(PayableContract.class) @Payable void accept(BigInteger amount, Validator buyer, Offer<Validator> offer) {
		// it is important to redefine this method, so that the same method with
		// argument of type PayableContract and Validator is redefined by the compiler with a bridge method
		// that casts the argument to Validator and calls this method. In this way
		// only instances of Validator can become shareholders (ie, actual validators)
		super.accept(amount, buyer, offer);
	}

	/**
	 * Rewards the Mokamint node and miner that has created a block or provided the deadline
	 * in the block, respectively. Hotmoka Mokamint nodes call this method at the end of the creation of
	 * each block of the blockchain.
	 * 
	 * @param amount the total amount to distribute among the node and the miner
	 * @param forNode the subset of {@code amount} that goes to the node; the rest goes to the miner
	 * @param minted the subset of {@code amount} that has been minted during the last reward
	 * @param publicKeyOfNodeBase64 the public key of the node
	 * @param publicKeyOfMinerBase64 the public key of the miner
	 * @param gasConsumed the gas consumed for CPU, RAM usage or storage by the transactions
	 *                    executed since the previous reward
	 * @param numberOfTransactionsSinceLastReward the number of transactions executed since the previous reward
	 * @return true if and only if also the miner account has been rewarded; otherwise,
	 *         only the node has been rewarded and a call to reward the miner must be executed later; this trick
	 *         guarantees that the accounts created during this transaction are only with progressive equal to 0
	 */
	@FromContract @Payable public boolean rewardMokamint(BigInteger amount, BigInteger forNode, BigInteger minted, String publicKeyOfNodeBase64, String publicKeyOfMinerBase64, BigInteger gasConsumed, BigInteger numberOfTransactionsSinceLastReward) {
		require(isSystemCall(), "node and miner can only be rewarded with a system request");

		var accountsLedger = getManifest().getAccountsLedger();

		// if at least one among the node and the miner has been created already, then the subsequent add() call
		// will not create objects and we can reward the miner as well, since there is no risk of creating
		// a new account, in the second add() call, whose progressive is not 0
		boolean alsoMiner = accountsLedger.get(publicKeyOfNodeBase64) != null || accountsLedger.get(publicKeyOfMinerBase64) != null;

		accountsLedger.add(forNode, publicKeyOfNodeBase64);
		if (alsoMiner)
			accountsLedger.add(BigIntegerSupport.subtract(amount, forNode), publicKeyOfMinerBase64);

		updateGasPrice(gasConsumed);
		updateParameters(minted, numberOfTransactionsSinceLastReward);

		return alsoMiner;
	}

	/**
	 * Rewards the miner that has provided the deadline in a block. Hotmoka Mokamint nodes call this method
	 * at the end of the creation of each block of the blockchain but only when
	 * {@link #rewardMokamint(BigInteger, BigInteger, BigInteger, String, String, BigInteger, BigInteger)}
	 * returns false. Note that this method is not payable, since it uses the reward previously sent
	 * through {@link #rewardMokamint(BigInteger, BigInteger, BigInteger, String, String, BigInteger, BigInteger)}
	 * but that has not been distributed to the miner and remained in this contract.
	 * 
	 * @param amount the amount to reward to the miner
	 * @param publicKeyOfMinerBase64 the public key of the miner
	 */
	@FromContract public void rewardMokamintMiner(BigInteger amount, String publicKeyOfMinerBase64) {
		require(isSystemCall(), "a miner can only be rewarded with a system request");

		getManifest().accountsLedger.add(amount, publicKeyOfMinerBase64);
	}

	/**
	 * The builder of a disk node validators object.
	 */
	@Exported
	public static class Builder extends Storage implements Function<Manifest<Validator>, MokamintValidators> {
		private final BigInteger ticketForNewPoll;
		private final BigInteger finalSupply;
		private final BigInteger heightAtFinalSupply;

		/**
		 * Creates the builder of a disk node validators object.
		 * 
		 * @param ticketForNewPoll the amount of coins to pay for starting a new poll among the validators;
		 *                         both {@link #newPoll(BigInteger, io.takamaka.code.dao.SimplePoll.Action)} and
		 *                         {@link #newPoll(BigInteger, io.takamaka.code.dao.SimplePoll.Action, long, long)}
		 *                         require to pay this amount for starting a poll
		 * @param finalSupply the final supply of coins that will be reached, eventually
		 * @param heightAtFinalSupply the height after which coins are not minted anymore and the current
		 *                            supply reaches the final supply
		 */
		public Builder(BigInteger ticketForNewPoll, BigInteger finalSupply, BigInteger heightAtFinalSupply) {
			this.ticketForNewPoll = ticketForNewPoll;
			this.finalSupply = finalSupply;
			this.heightAtFinalSupply = heightAtFinalSupply;
		}

		@Override
		public MokamintValidators apply(Manifest<Validator> manifest) {
			return new MokamintValidators(manifest, ticketForNewPoll, finalSupply, heightAtFinalSupply);
		}
	}
}