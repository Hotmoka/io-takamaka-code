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
 * A list of elements. It is possible to access elements at both sides of the list.
 * A list can hold {@code null} elements.
 * This interface has only access methods. It supports the creation of snapshots.
 *
 * @param <E> the type of the elements. This type must be allowed in storage
 */
public interface SnapshottableStorageListView<E> extends StorageListView<E> {

	/**
	 * Yields a snapshot of this list. The snapshot contains the elements in this list
	 * but is independent from this list: any future modification of this list will
	 * not be seen through the snapshot. A snapshot is always
	 * {@link io.takamaka.code.lang.Exported}.
	 * 
	 * @return a snapshot of this list
	 */
	StorageListView<E> snapshot();
}