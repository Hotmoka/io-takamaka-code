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

import java.util.Random;

import io.takamaka.code.lang.Takamaka;
import io.takamaka.code.lang.WhiteListedDuringInitialization;

/**
 * A support class for operations on {@code Random} that are deterministic,
 * can be white-listed but require to charge a non-constant amount of gas for their execution.
 */
@WhiteListedDuringInitialization
public abstract class RandomSupport {
	private RandomSupport() {}

	/**
	 * Fills the given array with random bytes.
	 * 
	 * @param random the object used to generate the random bytes
	 * @param bytes the array to fill
	 */
	public static void nextBytes(Random random, byte[] bytes) {
		if (bytes != null)
			Takamaka.charge(bytes.length);
		random.nextBytes(bytes);
	}
}