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

/**
 * A sorted set of (non-{@code null}) storage values.
 * By iterating on this object, one gets the values in the set, in increasing order.
 * This interface has only access methods. Its sub-interface
 * {@link StorageSet} includes modification methods as well.
 * 
 * @param <V> the type of the values. This type must be allowed in storage
 */

public interface SnapshottableStorageSetView<V> extends StorageSetView<V> {

	/**
	 * Yields a snapshot of this set. The snapshot contains the elements in this set
	 * but is independent from this set: any future modification of this set will
	 * not be seen through the snapshot. A snapshot is always
	 * {@link io.takamaka.code.lang.Exported}.
	 * 
	 * @return a snapshot of this set
	 */
	StorageSetView<V> snapshot();
}