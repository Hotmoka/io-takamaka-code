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

package io.takamaka.code.lang;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

/**
 * A support class for operations on {@code String} that are deterministic,
 * can be white-listed but require to charge a non-constant amount of gas for their execution.
 */
@WhiteListedDuringInitialization
public final class StringSupport {
	private StringSupport() {}

	/**
	 * Yields a copy of the given string.
	 * 
	 * @param s the string
	 * @return the resulting copy
	 */
	public static String clone(String s) {
		return new String(s);
	}

	/**
	 * Yields a string built from the given UTF8 bytes.
	 * 
	 * @param bytes the bytes
	 * @return the resulting string
	 */
	public static String fromUTF8(byte[] bytes) {
		return new String(bytes, StandardCharsets.UTF_8);
	}

	/**
	 * Determines if two strings are equal.
	 * 
	 * @param s1 the first string
	 * @param s2 the second string
	 * @return true if and only if that condition holds
	 */
	public static boolean equals(String s1, String s2) {
		return s1.equals(s2);
	}

	/**
	 * Determines which string comes first in alphabetical order.
	 * 
	 * @param s1 the first string
	 * @param s2 the second string
	 * @return negative if {@code s1} comes first, positive if {@code s2} comes first, zero if they are equal
	 */
	public static int compareTo(String s1, String s2) {
		return s1.compareTo(s2);
	}

		//public abstract boolean endsWith(java.lang.String suffix);
		//public abstract boolean startsWith(java.lang.String prefix);
		//public abstract java.lang.String toLowerCase();
		//public abstract java.lang.String toUpperCase();

	/**
	 * Yields the first position of the given character in the given string.
	 * 
	 * @param s the string
	 * @param c the character
	 * @return the first position, or -1 if {@code c} does not occur in {@code s}
	 */
	public static int indexOf(String s, int c) {
		return s.indexOf(c);
	}

	/**
	 * Yields a substring of a given substring.
	 * 
	 * @param s the string
	 * @param begin the initial position of the substring (included)
	 * @param end the final position of the substring (excluded)
	 * @return the resulting substring, from {@code begin} to {@code end}
	 */
	public static String substring(String s, int begin, int end) {
		return s.substring(begin, end);
	}

	/**
	 * Yields a substring of a given substring.
	 * 
	 * @param s the string
	 * @param begin the initial position of the substring (included)
	 * @return the resulting substring from {@code begin} till the end of {@code s}
	 */
	public static String substring(String s, int begin) {
		return s.substring(begin);
	}

	/**
	 * Yields the concatenation of the {@code toString()} of the given objects.
	 * 
	 * @param objects the objects
	 * @return the resulting string
	 */
	public static String concat(Object... objects) {
		StringBuilder result = new StringBuilder();
		for (Object object: objects)
			if (isSafeToConcatenate(object))
				result.append(String.valueOf(object));
			else
				throw new IllegalArgumentException("Illegal object in string concatenation: try to call toString() before concatenation");

		return result.toString();
	}

	private static boolean isSafeToConcatenate(Object object) {
		// check explicitly, do not use the Number interface since the programmer
		// might implement Number with a class that does not redefine toString() deterministically
		return object == null || object instanceof String || object instanceof BigInteger || object instanceof Storage ||
				object instanceof Byte ||
				object instanceof Boolean || object instanceof Character || object instanceof Short ||
				object instanceof Integer || object instanceof Long || object instanceof Float ||
				object instanceof Double;
	}
}