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

package io.takamaka.code.governance.disk;

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

/**
 * The validators of a disk node. It is empty, since disk nodes do not implement
 * any consensus based on validators. It just contains the code for updating the
 * chain parameters and gas price.
 */
public class DiskValidators extends AbstractValidators<Validator> {

	/**
	 * Creates the (empty) set of validators of a disk node.
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
	private DiskValidators(Manifest<Validator> manifest, BigInteger ticketForNewPoll, BigInteger finalSupply, BigInteger heightAtFinalSupply) {
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
	 * Hotmoka disk nodes call this method at regular intervals, after the creation of each new block.
	 * Its goal is only to update the chain parameters, since there are no validators in such Hotmoka nodes.
	 * The amount of coins minted and the fees get accumulated inside this contract, since there
	 * is nobody to distribute them to.
	 * 
	 * @param amount the amount to distribute, that will be accumulated inside this contract
	 * @param minted the subset of {@code amount} that has been minted during the last reward;
	 *               this means that {@code amount} is the sum of gas costs incurred by the
	 *               payers of the transactions and an extra inflation that is exactly {@code minted} coins
	 * @param gasConsumed the gas consumed for CPU, RAM usage or storage by the transactions
	 *                    executed since the previous reward
	 * @param numberOfTransactionsSinceLastReward the number of transactions executed since the previous reward
	 */
	@FromContract @Payable public void reward(BigInteger amount, BigInteger minted, BigInteger gasConsumed, BigInteger numberOfTransactionsSinceLastReward) {
		require(isSystemCall(), "the chain parameters can only be updated with a system request");
		updateGasPrice(gasConsumed);
		updateParameters(minted, numberOfTransactionsSinceLastReward);
	}

	/**
	 * The builder of a disk node validators object.
	 */
	@Exported
	public static class Builder extends Storage implements Function<Manifest<Validator>, DiskValidators> {
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
		public DiskValidators apply(Manifest<Validator> manifest) {
			return new DiskValidators(manifest, ticketForNewPoll, finalSupply, heightAtFinalSupply);
		}
	}
}