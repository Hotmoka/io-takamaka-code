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

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import io.takamaka.code.governance.Validator;
import io.takamaka.code.lang.AccountED25519;
import io.takamaka.code.lang.FromContract;
import io.takamaka.code.lang.Payable;
import io.takamaka.code.lang.View;

/**
 * The validator of a Tendermint network. It can be used to
 * collect money when transactions get validated. It is an account
 * with an identity string, that is used to identify validators that
 * must be rewarded or punished at each validation step.
 * The identity is derived from the public key of the validator,
 * as the first 40 characters of the hexadecimal representation
 * of the sha256 hashing of the public key bytes.
 */
public final class TendermintED25519Validator extends Validator implements AccountED25519 {

	/**
	 * The first 40 characters of the hexadecimal representation
	 * of the sha256 hashing of the public key bytes.
	 */
	private final String id;

	/**
	 * Creates a Tendermint validator with no initial funds, that uses ED25519 keys.
	 * 
	 * @param publicKey the Base64-encoded ED25519 public key of the validator
	 * @throws NullPointerException if {@code publicKey} is null
	 */
	public TendermintED25519Validator(String publicKey) {
		super(publicKey);

		this.id = computeId();
	}

	/**
	 * Creates a Tendermint validator with the given initial green funds.
	 * 
	 * @param initialAmount the initial funds
	 * @param publicKey the Base64-encoded public key that will be assigned to the validator
	 */
	@Payable @FromContract
	public TendermintED25519Validator(int initialAmount, String publicKey) {
		super(initialAmount, publicKey);

		this.id = computeId();
	}

	/**
	 * Creates a Tendermint validator with the given initial green funds.
	 * 
	 * @param initialAmount the initial funds
	 * @param publicKey the Base64-encoded public key that will be assigned to the validator
	 */
	@Payable @FromContract
	public TendermintED25519Validator(long initialAmount, String publicKey) {
		super(initialAmount, publicKey);

		this.id = computeId();
	}

	/**
	 * Creates a Tendermint validator with the given initial green funds.
	 * 
	 * @param initialAmount the initial funds
	 * @param publicKey the Base64-encoded public key that will be assigned to the validator
	 */
	@Payable @FromContract
	public TendermintED25519Validator(BigInteger initialAmount, String publicKey) {
		super(initialAmount, publicKey);

		this.id = computeId();
	}

	/**
	 * Yields the identifier of the validator, derived from its public key.
	 * 
	 * @return the first 40 characters of the hexadecimal representation
	 *         of the sha256 hashing of the public key bytes
	 */
	@Override
	public final @View String id() {
		return id;
	}

	private String computeId() {
		try {
			MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
			sha256.update(Base64.getDecoder().decode(publicKey()));
			return bytesToHex(sha256.digest()).substring(0, 40);
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Translates an array of bytes into a hexadecimal string.
	 * 
	 * @param bytes the bytes
	 * @return the string
	 */
	private static String bytesToHex(byte[] bytes) {
		final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes();
	    byte[] hexChars = new byte[bytes.length * 2];
	    for (int j = 0; j < bytes.length; j++) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = HEX_ARRAY[v >>> 4];
	        hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
	    }
	
	    return new String(hexChars, StandardCharsets.UTF_8);
	}
}