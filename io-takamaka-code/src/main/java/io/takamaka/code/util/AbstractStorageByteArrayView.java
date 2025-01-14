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

package io.takamaka.code.util;

import java.util.Iterator;

import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.StringSupport;
import io.takamaka.code.lang.View;

/**
 * A partial implementation of the {@link io.takamaka.code.util.StorageByteArrayView} interface,
 * containing all methods common to its subclasses.
 */
abstract class AbstractStorageByteArrayView extends Storage implements StorageByteArrayView {

	@Override
	public final String toString() {
		String result = "[";
		boolean first = true;
		for (Byte b: this)
			if (first) {
				result = StringSupport.concat(result, b);
				first = false;
			}
			else
				result = StringSupport.concat(result, ",", b);

		return StringSupport.concat(result, "]");
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof StorageByteArrayView sbaw && length() == sbaw.length()) {
			Iterator<Byte> otherIt = sbaw.iterator();
			for (byte b: this)
				if (b != otherIt.next())
					return false;

			return true;
		}

		return false;
	}

	@Override @View
	public int hashCode() {
		int shift = 0;
		int result = 0;
		for (byte b: this) {
			result ^= b << shift;
			shift = (shift + 1) % 24;
		}

		return result;
	}
}