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

import io.takamaka.code.math.BigIntegerSupport;

@WhiteListedDuringInitialization
public class StorageSupport {

	/**
	 * Compares two storage values. If they are comparable, it calls
	 * {@code compareTo} among them, otherwise compares them by storage reference.
	 * 
	 * @param <K> the type of the elements
	 * @param e1 the first element
	 * @param e2 the second element
	 * @return negative if {@code e1} comes first, positive if {@code e2} comes first,
	 *         zero if they are considered equals
	 */
	@SuppressWarnings("unchecked")
	public static <K> int compare(K e1, K e2) {
		if (e1 instanceof BigInteger e1bi && e2 instanceof BigInteger e2bi)
			return BigIntegerSupport.compareTo(e1bi, e2bi);
		else if (e1 instanceof String e1s && e2 instanceof String e2s)
			return StringSupport.compareTo(e1s, e2s);
		else if (e1 instanceof Storage e1s && e2 instanceof Storage e2s) {
			if (e1 instanceof Comparable<?>)
				return ((Comparable<K>) e1).compareTo(e2);
			else
				return e1s.compareByStorageReference(e2s);
		}
		else
			throw new IllegalArgumentException("Illegal comparison between non-storage values");
	}

	/**
	 * Compares two storage values for equality. If they are comparable, it calls
	 * {@code compareTo} among them and checks if the result is 0, otherwise compares their storage reference.
	 * 
	 * @param <K> the type of the elements
	 * @param e1 the first element
	 * @param e2 the second element
	 * @return true if and only if {@code e1} is equal to {@code e2}
	 */
	@SuppressWarnings("unchecked")
	public static <K> boolean equals(K e1, K e2) {
		if (e1 instanceof BigInteger e1bi && e2 instanceof BigInteger e2bi)
			return BigIntegerSupport.equals(e1bi, e2bi);
		else if (e1 instanceof String e1s && e2 instanceof String e2s)
			return StringSupport.equals(e1s, e2s);
		else if (e1 instanceof Storage e1s && e2 instanceof Storage e2s)
			if (e1 instanceof Comparable<?>)
				return ((Comparable<K>) e1).compareTo(e2) == 0;
			else
				return e1s.compareByStorageReference(e2s) == 0;
		else
			throw new IllegalArgumentException("Illegal comparison between non-storage values");
	}
}
