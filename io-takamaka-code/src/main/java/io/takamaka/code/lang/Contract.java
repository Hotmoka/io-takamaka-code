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

package io.takamaka.code.lang;

import java.math.BigInteger;

import io.takamaka.code.math.BigIntegerSupport;

/**
 * A contract is a storage object with a balance of coin. It is controlled
 * by the methods of its code.
 */
@Exported
public abstract class Contract extends Storage {

	/**
	 * The balance of this contract.
	 */
	private BigInteger balance;

	/**
	 * Builds a contract with zero balance.
	 */
	protected Contract() {
		this.balance = BigInteger.ZERO;
	}

	/**
	 * Yields the balance of this contract.
	 * 
	 * @return the balance
	 */
	public final @View BigInteger balance() {
		return balance;
	}

	@Override
	public @View String toString() {
		return "a contract";
	}

	/**
	 * Increases the balance of a contract by the given amount of coins,
	 * taken away from the balance of this contract.
	 * 
	 * @param beneficiary the beneficiary of the amount of coins
	 * @param amount the amount of coins
	 */
	private void pay(Contract beneficiary, BigInteger amount) {
		Takamaka.require(amount != null, "the paid amount cannot be null");
		Takamaka.require(amount.signum() >= 0, "the paid amount cannot be negative");
		if (BigIntegerSupport.compareTo(balance, amount) < 0)
			throw new InsufficientFundsError(BigIntegerSupport.subtract(amount, balance));

		balance = BigIntegerSupport.subtract(balance, amount);
		beneficiary.balance = BigIntegerSupport.add(beneficiary.balance, amount);
	}

	/**
	 * Called at the beginning of the instrumentation of a payable method or constructor.
	 * It sets the caller of the code and transfers the amount of coins to the contract.
	 * It is private, so that programmers cannot call
	 * it directly. Instead, instrumented code will call it by reflection.
	 * 
	 * @param payer the payer
	 * @param amount the amount of coins
	 */
	private void payableFromContract(Contract payer, BigInteger amount) {
		payer.pay(this, amount);
	}

	/**
	 * Called at the beginning of the instrumentation of a payable method or constructor.
	 * It sets the caller of the code and transfers the amount of coins to the contract.
	 * It is private, so that programmers cannot call
	 * it directly. Instead, instrumented code will call it by reflection.
	 *
	 * @param payer the payer
	 * @param amount the amount of coins
	 */
	@SuppressWarnings("unused")
	private void payableFromContract(Contract payer, int amount) {
		payableFromContract(payer, BigInteger.valueOf(amount));
	}

	/**
	 * Called at the beginning of the instrumentation of a payable method or constructor.
	 * It sets the caller of the code and transfers the amount of coins to the contract.
	 * It is private, so that programmers cannot call
	 * it directly. Instead, instrumented code will call it by reflection.
	 * 
	 * @param payer the payer
	 * @param amount the amount of coins
	 */
	@SuppressWarnings("unused")
	private void payableFromContract(Contract payer, long amount) {
		payableFromContract(payer, BigInteger.valueOf(amount));
	}
}