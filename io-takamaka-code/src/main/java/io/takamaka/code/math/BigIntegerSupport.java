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

package io.takamaka.code.math;

import java.math.BigInteger;

import io.takamaka.code.lang.WhiteListedDuringInitialization;

/**
 * A support class for operations on {@code BigInteger} that are deterministic,
 * can be white-listed but require to charge a non-constant amount of gas for their execution.
 */
@WhiteListedDuringInitialization
public abstract class BigIntegerSupport {
	private BigIntegerSupport() {}

	/**
	 * Creates a big integer from the given string.
	 * 
	 * @param val the string
	 * @return the resulting big integer
	 */
	public static BigInteger from(String val) {
		return new BigInteger(val);
	}

	/**
	 * Checks if two big integers are equal.
	 * 
	 * @param bi the first big integer
	 * @param other the other big integer
	 * @return true if and only if that condition holds
	 */
	public static boolean equals(BigInteger bi, BigInteger other) {
		return bi.equals(other);
	}

	/**
	 * Transforms a big integer in its string representation.
	 * 
	 * @param bi the big integer
	 * @return the string representation
	 */
	public static String toString(BigInteger bi) {
		return bi.toString();
	}

	/**
	 * Yields the two-complement representation of a big integer.
	 * 
	 * @param bi the big integer
	 * @return the representation
	 */
	public static byte[] toByteArray(BigInteger bi) {
		return bi.toByteArray();
	}

	/**
	 * Compares two big integers.
	 * 
	 * @param first the first big integer
	 * @param second the second big integer
	 * @return negative if {@code first} is smaller; positive if {@code second}
	 */
	public static int compareTo(BigInteger first, BigInteger second) {
		return first.compareTo(second);
	}

	/**
	 * Yields the addition of two big integers.
	 * 
	 * @param first the first big integer
	 * @param second the second big integer
	 * @return the addition of the two
	 */
	public static BigInteger add(BigInteger first, BigInteger second) {
		return first.add(second);
	}

	/**
	 * Yields the subtraction of two big integers.
	 * 
	 * @param first the first big integer
	 * @param second the second big integer
	 * @return the subtraction of the two
	 */
	public static BigInteger subtract(BigInteger first, BigInteger second) {
		return first.subtract(second);
	}

	/**
	 * Yields the multiplication of two big integers.
	 * 
	 * @param first the first big integer
	 * @param second the second big integer
	 * @return the multiplication of the two
	 */
	public static BigInteger multiply(BigInteger first, BigInteger second) {
		return first.multiply(second);
	}

	/**
	 * Yields the quotient of two big integers.
	 * 
	 * @param first the first big integer
	 * @param second the second big integer
	 * @return the quotient of the two
	 */
	public static BigInteger divide(BigInteger first, BigInteger second) {
		return first.divide(second);
	}

	/**
	 * Yields quotient and remainder of a division of big integers.
	 * 
	 * @param bi the divided big integer
	 * @param divisor the divisor big integer
	 * @return the quotient and the remainder
	 */
	public static BigInteger[] divideAndRemainder(BigInteger bi, BigInteger divisor) {
		return bi.divideAndRemainder(divisor);
	}

	/**
	 * Yields the remainder of a division of big integers.
	 * 
	 * @param bi the divided big integer
	 * @param divisor the divisor big integer
	 * @return the remainder
	 */
	public static BigInteger mod(BigInteger bi, BigInteger divisor) {
		return bi.mod(divisor);
	}

	/**
	 * Yields the power of a big integer.
	 * 
	 * @param bi the base of the power
	 * @param exponent the exponent of the power
	 * @return the result of the power
	 */
	public static BigInteger pow(BigInteger bi, int exponent) {
		return bi.pow(exponent);
	}

	/**
	 * Yields the maximum of two big integers.
	 * 
	 * @param first the first big integer
	 * @param second the second big integer
	 * @return the maximum of the two
	 */
	public static BigInteger max(BigInteger first, BigInteger second) {
		return first.max(second);
	}

	/**
	 * Yields the minimum of two big integers.
	 * 
	 * @param first the first big integer
	 * @param second the second big integer
	 * @return the minimum of the two
	 */
	public static BigInteger min(BigInteger first, BigInteger second) {
		return first.min(second);
	}
}