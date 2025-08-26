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

/**
 * An array of (possibly {@code null}) storage values, that can be kept in storage.
 * By iterating on this object, one gets the values of the array, in increasing index
 * order, including {@code null}s. This interface contains both read and modification methods.
 * It supports the creation of snapshots.
 * 
 * @param <V> the type of the values
 */

public interface SnapshottableStorageArray<V> extends StorageArray<V>, SnapshottableStorageArrayView<V> {

	/**
	 * Yields a view of this array. The view reflects the elements in this array:
	 * any future modification of this array will be seen also through the view.
	 * A view is always {@link io.takamaka.code.lang.Exported}.
	 * 
	 * @return a view of this array
	 */
	SnapshottableStorageArrayView<V> view();
}