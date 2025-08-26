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

import static io.takamaka.code.lang.Takamaka.require;

import java.math.BigInteger;
import java.util.function.Consumer;

import io.takamaka.code.math.BigIntegerSupport;
import io.takamaka.code.util.StorageIntMap;
import io.takamaka.code.util.StorageTreeIntMap;

/**
 * A collector of accounts.
 *
 * @param <A> the type of the accounts contained in this collector
 */
public abstract class Accounts<A extends ExternallyOwnedAccount> extends Contract {

	/**
	 * The accounts contained in this container, in order of creation.
	 */
	private final StorageIntMap<A> accounts;

	/**
	 * Creates the container.
	 * 
	 * @param amount the total amount of coins distributed to the accounts that get created;
	 *               this must be the sum of all {@code balances}
	 * @param balances the initial balances of the accounts; they must be as many as the {@code publicKeys}
	 *                 and their sum must be {@code amount}
	 * @param publicKeys the Base64-encoded public keys of the accounts
	 */
	protected @FromContract @Payable Accounts(BigInteger amount, BigInteger[] balances, String[] publicKeys) {
		require(balances != null, "balances cannot be null");
		require(publicKeys != null, "the public keys cannot be null");
		int length = balances.length;
		require(length == publicKeys.length, "the balances must be as many as the public keys");
		BigInteger sum = BigInteger.ZERO;
		for (var balance: balances)
			sum = BigIntegerSupport.add(sum, balance);
		require(BigIntegerSupport.equals(amount, sum),
			"the amount paid for creating this collector must be equal to the sum of the balances of the accounts being created");

		this.accounts = new StorageTreeIntMap<>();
		for (int pos = 0; pos < length; pos++)
			accounts.put(pos, mkAccount(balances[pos], publicKeys[pos]));
	}

	/**
	 * Creates the container.
	 * 
	 * @param amount the total amount of coins distributed to the accounts that get created;
	 *                this must be the sum of all {@code balances}
	 * @param balances the initial balances of the accounts,
	 *               as a space-separated sequence of big integers; they must be as many
	 *               as there are public keys in {@code publicKeys}
	 * @param publicKeys the public keys of the accounts,
	 *                   as a space-separated sequence of Base64-encoded public keys
	 */
	protected @FromContract @Payable Accounts(BigInteger amount, String balances, String publicKeys) {
		this(amount, buildBalances(balances), buildPublicKeys(publicKeys));
	}

	/**
	 * Yields a new account with the given initial balance and public key.
	 * 
	 * @param balance the balance
	 * @param publicKey the public key
	 * @return the account
	 */
	protected abstract A mkAccount(BigInteger balance, String publicKey);

	private static BigInteger[] buildBalances(String balancesAsStringSequence) {
		String[] segments = splitAtSpaces(balancesAsStringSequence);
		var result = new BigInteger[segments.length];
		int pos = 0;
		for (String s: segments)
			result[pos++] = BigIntegerSupport.from(s);

		return result;
	}

	private static String[] buildPublicKeys(String publicKeysAsStringSequence) {
		return splitAtSpaces(publicKeysAsStringSequence);
	}

	private static String[] splitAtSpaces(String s) {
		int counter = s.isEmpty() ? 0 : 1;
		for (int i = 0; i < s.length() - 1; i++)
			if (s.charAt(i) == ' ')
				counter++;

		var result = new String[counter];
		int pos;
		int i = 0;
		while ((pos = StringSupport.indexOf(s, ' ')) >= 0) {
			result[i++] = StringSupport.substring(s, 0, pos);
			s = StringSupport.substring(s, pos + 1);
		}

		if (!s.isEmpty())
			result[i++] = s;

		return result;
	}

	/**
	 * Performs the given action for each account in this container.
	 * 
	 * @param action the action the action to perform
	 */
	public final void forEach(Consumer<? super A> action) {
		accounts.forEachValue(action);
	}

	/**
	 * Yields the number of accounts in this collector.
	 * 
	 * @return the number of accounts
	 */
	public final @View int size() {
		return accounts.size();
	}

	/**
	 * Checks if this collector is empty.
	 * 
	 * @return true if and only if this collector is empty
	 */
	public final @View boolean isEmpty() {
		return accounts.isEmpty();
	}

	/**
	 * Yields the {@code key}th account in this collector, in the same order as balances and
	 * public keys have been passed to the constructor.
	 * 
	 * @param key the number of the account, from 0 inclusive to {@code size()} exclusive
	 * @return the account
	 */
	public final @View A get(int key) {
		return accounts.get(key);
	}
}