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

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.takamaka.code.lang.View;

/**
 * A map from storage keys to (possibly {@code null}) storage values,
 * that can be kept in storage. By iterating on this object, one gets
 * the key/value pairs of the map, in increasing key order.
 * This interface has only access methods. Its sub-interface
 * {@link StorageMap} includes modification methods as well.
 * 
 * @param <K> the type of the keys
 * @param <V> the type of the values
 */

public interface StorageMapView<K,V> extends Iterable<StorageMapView.Entry<K,V>> {

	/**
	 * A key/value pair.
	 *
	 * @param <K> the type of the keys
	 * @param <V> the type of the values
	 */
	interface Entry<K,V> {

		/**
		 * Yields the key of the pair.
		 * 
		 * @return the key
		 */
		K getKey();

		/**
		 * Yields the value of the pair.
		 * 
		 * @return the value
		 */
		V getValue();
	}

	/**
	 * Returns the number of key-value pairs in this symbol table.
	 * 
	 * @return the number of key-value pairs in this symbol table
	 */
	@View int size();

	/**
	 * Determines if this symbol table is empty.
	 * 
	 * @return {@code true} if and only if this symbol table is empty
	 */
	@View boolean isEmpty();

	/**
	 * Yields the value associated with the given key, if any.
	 * 
	 * @param key the key
	 * @return the value associated with the given key if the key is in the symbol table
	 *         and {@code null} if the key is not in the symbol table
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	@View V get(Object key);

	/**
	 * Returns the value associated with the given key.
	 * 
	 * @param key the key
	 * @param _default the default value
	 * @return the value associated with the given key if the key is in the symbol table.
	 *         Yields {@code _default} if the key is not in the symbol table
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	@View V getOrDefault(Object key, V _default);

	/**
	 * Yields the value associated with the given key.
	 * 
	 * @param key the key
	 * @param _default the function yielding the default value
	 * @return the value associated with the given key if the key is in the symbol table.
	 *         Yields {@code _default.get()} if the key is not in the symbol table
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	V getOrDefault(Object key, Supplier<? extends V> _default);

	/**
	 * Determines if this symbol table contains the given key (possily bound to {@code null}).
	 * 
	 * @param key the key
	 * @return {@code true} if and only if this symbol table contains {@code key}
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	@View boolean containsKey(Object key);

	/**
	 * Yields the smallest key in the symbol table.
	 * 
	 * @return the smallest key in the symbol table
	 * @throws NoSuchElementException if the symbol table is empty
	 */
	@View K min();

	/**
	 * Yields the largest key in the symbol table.
	 * 
	 * @return the largest key in the symbol table
	 * @throws NoSuchElementException if the symbol table is empty
	 */
	@View K max();

	/**
	 * Yields the largest key in the symbol table less than or equal to {@code key}.
	 * 
	 * @param key the key
	 * @return the largest key in the symbol table less than or equal to {@code key}
	 * @throws NoSuchElementException if there is no such key
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	@View K floorKey(K key);

	/**
	 * Yields the smallest key in the symbol table greater than or equal to {@code key}.
	 * 
	 * @param key the key
	 * @return the smallest key in the symbol table greater than or equal to {@code key}
	 * @throws NoSuchElementException if there is no such key
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	@View K ceilingKey(K key);

	/**
	 * Yields the key in the symbol table whose rank is {@code k}.
	 * This is the (k+1)st smallest key in the symbol table. 
	 *
	 * @param  k the rank
	 * @return the key in the symbol table of rank {@code k}
	 * @throws IllegalArgumentException unless {@code k} is between 0 and {@code size()-1}
	 */
	@View K select(int k);

	/**
	 * Yields the number of keys in the symbol table strictly less than {@code key}.
	 * 
	 * @param key the key
	 * @return the number of keys in the symbol table strictly less than {@code key}
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	@View int rank(K key);

	/**
	 * Yields an ordered stream of the entries (key/value) in this map, in
	 * increasing order of keys.
	 * 
	 * @return the stream
	 */
	Stream<Entry<K,V>> stream();

	/**
	 * Yields the keys of this map, in increasing order.
	 * 
	 * @return the keys
	 */
	List<K> keyList();

	/**
	 * Yields the ordered stream of the keys of this map, in increasing order.
	 * 
	 * @return the stream
	 */
	Stream<K> keys();

	/**
	 * Yields the ordered stream of the values of this map, in increasing order of corresponding key.
	 * 
	 * @return the stream
	 */
	Stream<V> values();

	/**
	 * Yields a snapshot of this map. The snapshot contains the elements in this map
	 * but is independent from this map: any future modification of this map will
	 * not be seen through the snapshot. A snapshot is always
	 * {@link io.takamaka.code.lang.Exported}.
	 * 
	 * @return a snapshot of this map
	 */
	StorageMapView<K,V> snapshot();
}