/*
Copyright 2025 Fausto Spoto

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

package io.takamaka.code.security;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.takamaka.code.lang.WhiteListedDuringInitialization;

/**
 * A digest for SHA256 hashing.
 */
@WhiteListedDuringInitialization
public final class SHA256Digest {

	/**
	 * We guarantee that the digest never escapes this class, so that
	 * programmers cannot call any other method on it.
	 */
	private final MessageDigest digest;

	/**
	 * Creates a digest object.
	 * 
	 * @throws NoSuchAlgorithmException if the sha256 algorithm is not installed
	 */
	public SHA256Digest() throws NoSuchAlgorithmException {
		this.digest = MessageDigest.getInstance("SHA-256");
	}

    /**
     * Completes the hash computation by performing final operations
     * such as padding. The digest is reset after this call is made.
     *
     * @return the array of bytes for the resulting hash value.
     */
	public byte[] digest() {
		return digest.digest();
	}

    /**
     * Performs a final update on the digest using the specified array
     * of bytes, then completes the digest computation. That is, this
     * method first calls {@link #update(byte[]) update(input)},
     * passing the <i>input</i> array to the {@code update} method,
     * then calls {@link #digest() digest()}.
     *
     * @param input the input to be updated before the digest is completed.
     * @return the array of bytes for the resulting hash value.
     */
	public byte[] digest(byte[] input) {
		return digest.digest(input);
	}

    /**
     * Completes the hash computation by performing final operations
     * such as padding. The digest is reset after this call is made.
     *
     * @param buf output buffer for the computed digest
     * @param offset offset into the output buffer to begin storing the digest
     * @param len number of bytes within buf allotted for the digest
     * @return the number of bytes placed into {@code buf}
     * @throws DigestException if an error occurs.
     */
	public int digest(byte[] buf, int offset, int len) throws DigestException {
		return digest.digest(buf, offset, len);
	}

    /**
     * Updates the digest using the specified byte.
     *
     * @param input the byte with which to update the digest.
     */
	public void update(byte input) {
		digest.update(input);
	}

	/**
     * Updates the digest using the specified array of bytes.
     *
     * @param input the array of bytes.
     */
	public void update(byte[] input) {
		digest.update(input);
	}

    /**
     * Updates the digest using the specified array of bytes, starting
     * at the specified offset.
     *
     * @param input the array of bytes.
     * @param offset the offset to start from in the array of bytes.
     * @param len the number of bytes to use, starting at {@code offset}.
     */
	public void update(byte[] input, int offset, int len) {
		digest.update(input, offset, len);
	}
}
