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

	public SHA256Digest() throws NoSuchAlgorithmException {
		this.digest = MessageDigest.getInstance("SHA-256");
	}

	public byte[] digest() {
		return digest.digest();
	}

	public byte[] digest(byte[] input) {
		return digest(input);
	}

	public int digest(byte[] buf, int offset, int len) throws DigestException {
		return digest.digest(buf, offset, len);
	}

	public void update(byte input) {
		digest.update(input);
	}

	public void update(byte[] input) {
		digest.update(input);
	}

	public void update(byte[] input, int offset, int len) {
		update(input, offset, len);
	}
}
