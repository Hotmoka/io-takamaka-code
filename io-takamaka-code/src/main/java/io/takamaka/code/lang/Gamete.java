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
import static java.math.BigInteger.ZERO;

import java.math.BigInteger;

import io.takamaka.code.governance.tendermint.TendermintED25519Validator;
import io.takamaka.code.math.BigIntegerSupport;

/**
 * A contract that can be used as gamete of a network. It is an externally-owned
 * account with a faucet method for providing funds to other externally-owned accounts.
 * The faucet can be disabled by fixing its maximum to zero.
 */
public final class Gamete extends ExternallyOwnedAccount {

	/**
	 * The maximal amount of coins that the faucet can provide at each call.
	 */
	private BigInteger maxFaucet = ZERO;

	/**
	 * Creates a gamete without initial funds.
	 * 
	 * @param publicKey the Base64-encoded public key of the gamete
	 * @throws NullPointerException if {@code publicKey} is null
	 */
	public Gamete(String publicKey) {
		super(publicKey);
	}

	/**
	 * Creates a gamete with the given initial fund.
	 * 
	 * @param initialAmount the initial fund
	 * @param publicKey the Base64-encoded public key of the gamete
	 * @throws NullPointerException if {@code publicKey} is null
	 */
	@Payable @FromContract
	public Gamete(int initialAmount, String publicKey) {
		super(publicKey);
	}

	/**
	 * Creates a gamete with the given initial fund.
	 * 
	 * @param initialAmount the initial fund
	 * @param publicKey the Base64-encoded public key of the gamete
	 * @throws NullPointerException if {@code publicKey} is null
	 */
	@Payable @FromContract
	public Gamete(long initialAmount, String publicKey) {
		super(publicKey);
	}

	/**
	 * Creates a gamete with the given initial fund.
	 * 
	 * @param initialAmount the initial fund
	 * @param publicKey the Base64-encoded public key of the gamete
	 * @throws NullPointerException if {@code publicKey} is null
	 */
	@Payable @FromContract
	public Gamete(BigInteger initialAmount, String publicKey) {
		super(publicKey);
	}

	@Override
	public String toString() {
		return "a gamete";
	}

	/**
	 * Yields the maximal amount of coins that the faucet can provide at each call.
	 * 
	 * @return the maximal amount of coins
	 */
	public final @View BigInteger getMaxFaucet() {
		return maxFaucet;
	}

	/**
	 * Sets the maximal threshold for the faucet of this gamete.
	 * Only the gamete itself can call this method.
	 * 
	 * @param maxFaucet the maximal threshold for the coins; use zero to disable the faucet
	 */
	public final @FromContract void setMaxFaucet(BigInteger maxFaucet) {
		require(maxFaucet != null && maxFaucet.signum() >= 0, "the threshold of the faucet must be a non-negative BigInteger");
		require(caller() == this, "only the gamete can change the thresholds of its own faucet");

		this.maxFaucet = maxFaucet;
	}

	/**
	 * Yields a new account with the given initial green coins, paid by this gamete.
	 * Only the gamete itself can call this method.
	 * This method is special, in the sense that it can be called without a correct
	 * signature, if the {@code allowsUnsignedFaucet} consensus option is set.
	 * 
	 * @param balance the initial funds of the new account, between 0 and the maximal threshold
	 *                set with {@link #setMaxFaucet(BigInteger)}
	 * @param publicKey the public key of the new account
	 * @return the new account
	 */
	public final @FromContract ExternallyOwnedAccount faucet(BigInteger balance, String publicKey) {
		require(balance != null && balance.signum() >= 0 && BigIntegerSupport.compareTo(balance, maxFaucet) <= 0, () -> StringSupport.concat("the balance must be between 0 and ", maxFaucet, " inclusive"));
		require(caller() == this, "only the gamete can call its own faucet");
		return new ExternallyOwnedAccount(balance, publicKey);
	}

	/**
	 * Yields a new ED25519 account with the given initial green coins, paid by this gamete.
	 * Only the gamete itself can call this method.
	 * This method is special, in the sense that it can be called without a correct
	 * signature, if the {@code allowsUnsignedFaucet} consensus option is set.
	 * 
	 * @param balance the initial funds of the new account, between 0 and the maximal threshold
	 *                set with {@link #setMaxFaucet(BigInteger)}
	 * @param publicKey the public key of the new account
	 * @return the new account
	 */
	public final @FromContract ExternallyOwnedAccountED25519 faucetED25519(BigInteger balance, String publicKey) {
		require(balance != null && balance.signum() >= 0 && BigIntegerSupport.compareTo(balance, maxFaucet) <= 0, () -> StringSupport.concat("the balance must be between 0 and ", maxFaucet, " inclusive"));
		require(caller() == this, "only the gamete can call its own faucet");
		return new ExternallyOwnedAccountED25519(balance, publicKey);
	}

	/**
	 * Yields a new Tendermint validator with the given initial green coins, paid by this gamete.
	 * Only the gamete itself can call this method.
	 * This method is special, in the sense that it can be called without a correct
	 * signature, if the {@code allowsUnsignedFaucet} consensus option is set.
	 * 
	 * @param balance the initial funds of the new account, between 0 and the maximal threshold
	 *                set with {@link #setMaxFaucet(BigInteger)}
	 * @param publicKey the public key of the new validator
	 * @return the new validator
	 */
	public final @FromContract TendermintED25519Validator faucetTendermintED25519Validator(BigInteger balance, String publicKey) {
		require(balance != null && balance.signum() >= 0 && BigIntegerSupport.compareTo(balance, maxFaucet) <= 0, () -> StringSupport.concat("the balance must be between 0 and ", maxFaucet, " inclusive"));
		require(caller() == this, "only the gamete can call its own faucet");
		return new TendermintED25519Validator(balance, publicKey);
	}

	/**
	 * Yields a new SHA256DSA account with the given initial green coins, paid by this gamete.
	 * Only the gamete itself can call this method.
	 * This method is special, in the sense that it can be called without a correct
	 * signature, if the {@code allowsUnsignedFaucet} consensus option is set.
	 * 
	 * @param balance the initial funds of the new account, between 0 and the maximal threshold
	 *                set with {@link #setMaxFaucet(BigInteger)}
	 * @param publicKey the public key of the new account
	 * @return the new account
	 */
	public final @FromContract ExternallyOwnedAccountSHA256DSA faucetSHA256DSA(BigInteger balance, String publicKey) {
		require(balance != null && balance.signum() >= 0 && BigIntegerSupport.compareTo(balance, maxFaucet) <= 0, () -> StringSupport.concat("the balance must be between 0 and ", maxFaucet, " inclusive"));
		require(caller() == this, "only the gamete can call its own faucet");
		return new ExternallyOwnedAccountSHA256DSA(balance, publicKey);
	}

	/**
	 * Yields a new QTESLA1 account with the given initial green coins, paid by this gamete.
	 * Only the gamete itself can call this method.
	 * This method is special, in the sense that it can be called without a correct
	 * signature, if the {@code allowsUnsignedFaucet} consensus option is set.
	 * 
	 * @param balance the initial funds of the new account, between 0 and the maximal threshold
	 *                set with {@link #setMaxFaucet(BigInteger)}
	 * @param publicKey the public key of the new account
	 * @return the new account
	 */
	public final @FromContract ExternallyOwnedAccountQTESLA1 faucetQTESLA1(BigInteger balance, String publicKey) {
		require(balance != null && balance.signum() >= 0 && BigIntegerSupport.compareTo(balance, maxFaucet) <= 0, () -> StringSupport.concat("the balance must be between 0 and ", maxFaucet, " inclusive"));
		require(caller() == this, "only the gamete can call its own faucet");
		return new ExternallyOwnedAccountQTESLA1(balance, publicKey);
	}

	/**
	 * Yields a new QTESLA3 account with the given initial green coins, paid by this gamete.
	 * Only the gamete itself can call this method.
	 * This method is special, in the sense that it can be called without a correct
	 * signature, if the {@code allowsUnsignedFaucet} consensus option is set.
	 * 
	 * @param balance the initial funds of the new account, between 0 and the maximal threshold
	 *                set with {@link #setMaxFaucet(BigInteger)}
	 * @param publicKey the public key of the new account
	 * @return the new account
	 */
	public final @FromContract ExternallyOwnedAccountQTESLA3 faucetQTESLA3(BigInteger balance, String publicKey) {
		require(balance != null && balance.signum() >= 0 && BigIntegerSupport.compareTo(balance, maxFaucet) <= 0, () -> StringSupport.concat("the balance must be between 0 and ", maxFaucet, " inclusive"));
		require(caller() == this, "only the gamete can call its own faucet");
		return new ExternallyOwnedAccountQTESLA3(balance, publicKey);
	}

	/**
	 * Yields a new account with the given initial green coins, paid by this gamete.
	 * Only the gamete itself can call this method.
	 * This method is special, in the sense that it can be called without a correct
	 * signature, if the {@code allowsUnsignedFaucet} consensus option is set.
	 * 
	 * @param balance the initial funds of the new account, between 0 and the maximal threshold
	 *                set with {@link #setMaxFaucet(BigInteger)}
	 * @param publicKey the public key of the new account
	 * @return the new account
	 */
	public final @FromContract ExternallyOwnedAccount faucet(int balance, String publicKey) {
		require(balance >= 0 && BigIntegerSupport.compareTo(BigInteger.valueOf(balance), maxFaucet) <= 0, () -> StringSupport.concat("the balance must be between 0 and ", maxFaucet, " inclusive"));
		require(caller() == this, "only the gamete can call its own faucet");
		return new ExternallyOwnedAccount(balance, publicKey);
	}
	
	/**
	 * Yields a new account with the given initial green coins, paid by this gamete.
	 * Only the gamete itself can call this method.
	 * This method is special, in the sense that it can be called without a correct
	 * signature, if the {@code allowsUnsignedFaucet} consensus option is set.
	 * 
	 * @param balance the initial funds of the new account, between 0 and the maximal threshold
	 *                set with {@link #setMaxFaucet(BigInteger)}
	 * @param publicKey the public key of the new account
	 * @return the new account
	 */
	public final @FromContract ExternallyOwnedAccount faucet(long balance, String publicKey) {
		require(balance >= 0L && BigIntegerSupport.compareTo(BigInteger.valueOf(balance), maxFaucet) <= 0, () -> StringSupport.concat("the balance must be between 0 and ", maxFaucet, " inclusive"));
		require(caller() == this, "only the gamete can call its own faucet");
		return new ExternallyOwnedAccount(balance, publicKey);
	}

	/**
	 * Sends the given amount of coins to the given payable contract.
	 * Only the gamete itself can call this method.
	 * This method is special, in the sense that it can be called without a correct
	 * signature, if the {@code allowsUnsignedFaucet} consensus option is set.
	 * 
	 * @param contract the payable contract that will receive the coins
	 * @param amount the coins to send to {@code contract}
	 */
	public final @FromContract void faucet(PayableContract contract, BigInteger amount) {
		require(amount != null && amount.signum() >= 0 && BigIntegerSupport.compareTo(amount, maxFaucet) <= 0, () -> StringSupport.concat("the balance must be between 0 and ", maxFaucet, " inclusive"));
		require(caller() == this, "only the gamete can call its own faucet");
		contract.receive(amount);
	}

	/**
	 * Sends the given amount of coins to the given payable contract.
	 * Only the gamete itself can call this method.
	 * This method is special, in the sense that it can be called without a correct
	 * signature, if the {@code allowsUnsignedFaucet} consensus option is set.
	 * 
	 * @param contract the payable account that will receive the coins
	 * @param amount the coins to send to {@code contract}
	 */
	public final @FromContract void faucet(PayableContract contract, int amount) {
		require(amount >= 0 && BigIntegerSupport.compareTo(BigInteger.valueOf(amount), maxFaucet) <= 0, () -> StringSupport.concat("the balance must be between 0 and ", maxFaucet, " inclusive"));
		require(caller() == this, "only the gamete can call its own faucet");
		contract.receive(amount);
	}

	/**
	 * Sends the given amount of coins to the given payable contract.
	 * Only the gamete itself can call this method.
	 * This method is special, in the sense that it can be called without a correct
	 * signature, if the {@code allowsUnsignedFaucet} consensus option is set.
	 * 
	 * @param contract the payable contract that will receive the coins
	 * @param amount the coins to send to {@code contract}
	 */
	public final @FromContract void faucet(PayableContract contract, long amount) {
		require(amount >= 0 && BigIntegerSupport.compareTo(BigInteger.valueOf(amount), maxFaucet) <= 0, () -> StringSupport.concat("the balance must be between 0 and ", maxFaucet, " inclusive"));
		require(caller() == this, "only the gamete can call its own faucet");
		contract.receive(amount);
	}
}