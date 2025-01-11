/*
Copyright 2024 Fausto Spoto

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

/**
 * A support class for concatenating objects into strings.
 * This guarantees that only objects with deterministic {@code toString()}
 * are concatenated and charged gas accordingly.
 */
@WhiteListedDuringInitialization
public final class StringSupport {
	private StringSupport() {}

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