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
 * A map from storage keys to (possibly {@code null}) storage values,
 * that can be kept in storage. By iterating on this object, one gets
 * the key/value pairs of the map, in increasing key order. This interface includes
 * read and modification methods and allows to perform snapshots of the map.
 * 
 * @param <K> the type of the keys
 * @param <V> the type of the values
 */

public interface SnapshottableStorageMap<K,V> extends SnapshottableStorageMapView<K,V>, StorageMap<K,V> {

	@Override
	SnapshottableStorageMapView<K,V> view();
}