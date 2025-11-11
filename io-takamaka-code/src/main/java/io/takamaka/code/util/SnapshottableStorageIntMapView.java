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
 * A map from integer keys to (possibly {@code null}) storage values,
 * that can be kept in storage. By iterating on this object, one gets
 * the key/value pairs of the map, in increasing key order. This interface includes
 * read methods only. It supports the creation of snapshots.
 * 
 * @param <V> the type of the values
 */
public interface SnapshottableStorageIntMapView<V> extends StorageIntMapView<V> {

	/**
	 * Yields a snapshot of this map. The snapshot contains the elements in this map
	 * but is independent from this map: any future modification of this map will
	 * not be seen through the snapshot. A snapshot is always
	 * {@link io.takamaka.code.lang.Exported}.
	 * 
	 * @return a snapshot of this map
	 */
	StorageIntMapView<V> snapshot();
}