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

package io.takamaka.code.util;

import io.takamaka.code.lang.WhiteListedDuringInitialization;

/**
 * A Base64 encoding/decoding utility.
 */
@WhiteListedDuringInitialization
public final class Base64 {
	private final java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
	private final java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();

	/**
	 * Creates a Base64 conversion utility.
	 */
	public Base64() {}

	/**
     * Decodes all bytes from the input byte array using the Base64
     * encoding scheme, writing the results into a newly-allocated output
     * byte array. The returned byte array is of the length of the resulting
     * bytes.
     *
     * @param src the byte array to decode
     * @return a newly-allocated byte array containing the decoded bytes
     * @throws IllegalArgumentException if {@code src} is not in valid Base64 scheme
     */
	public final byte[] decode(byte[] src) {
		return decoder.decode(src);
	}

    /**
     * Decodes a Base64 encoded String into a newly-allocated byte array
     * using the Base64 encoding scheme.
     *
     * @param src the string to decode
     * @return a newly-allocated byte array containing the decoded bytes
     * @throws IllegalArgumentException if {@code src} is not in valid Base64 scheme
     */
	public final byte[] decode(String src) {
		return decoder.decode(src);
	}

    /**
     * Decodes all bytes from the input byte array using the Base64
     * encoding scheme, writing the results into the given output byte array,
     * starting at offset 0.
     *
     * <p> It is the responsibility of the invoker of this method to make
     * sure the output byte array {@code dst} has enough space for decoding
     * all bytes from the input byte array. No bytes will be written to
     * the output byte array if the output byte array is not big enough.
     *
     * <p> If the input byte array is not in valid Base64 encoding scheme
     * then some bytes may have been written to the output byte array before
     * IllegalargumentException is thrown.
     *
     * @param src the byte array to decode
     * @param dst the output byte array
     * @return the number of bytes written to the output byte array
     * @throws IllegalArgumentException if {@code src} is not in valid Base64
     *                                  scheme, or {@code dst} does not have enough
     *                                  space for decoding all input bytes
     */
	public final int decode(byte[] src, byte[] dst) {
		return decoder.decode(src, dst);
	}

    /**
     * Encodes all bytes from the specified byte array into a newly-allocated
     * byte array using the Base64 encoding scheme. The returned byte
     * array is of the length of the resulting bytes.
     *
     * @param src the byte array to encode
     * @return a newly-allocated byte array containing the resulting encoded bytes
     */
	public final byte[] encode(byte[] src) {
		return encoder.encode(src);
	}

    /**
     * Encodes all bytes from the specified byte array using the
     * Base64 encoding scheme, writing the resulting bytes to the
     * given output byte array, starting at offset 0.
     *
     * <p> It is the responsibility of the invoker of this method to make
     * sure the output byte array {@code dst} has enough space for encoding
     * all bytes from the input byte array. No bytes will be written to the
     * output byte array if the output byte array is not big enough.
     *
     * @param src the byte array to encode
     * @param dst the output byte array
     * @return the number of bytes written to the output byte array
     * @throws IllegalArgumentException if {@code dst} does not have enough
     *                                  space for encoding all input bytes.
     */
	public final int encode(byte[] src, byte[] dst) {
		return encoder.encode(src, dst);
	}

    /**
     * Encodes the specified byte array into a String using the Base64
     * encoding scheme.
     *
     * @param src the byte array to encode
     * @return a String containing the resulting Base64 encoded characters
     */
	public final String encodeToString(byte[] src) {
		return encoder.encodeToString(src);
	}
}
