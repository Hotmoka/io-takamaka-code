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

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * A map from storage keys to (possibly {@code null}) storage values,
 * that can be kept in storage. By iterating on this object, one gets
 * the key/value pairs of the map, in increasing key order. This interface includes
 * read and modification methods.
 * 
 * @param <K> the type of the keys
 * @param <V> the type of the values
 */

public interface StorageMap<K,V> extends StorageMapView<K,V> {

	/**
	 * Inserts the specified key-value pair into this symbol table, overwriting the old 
	 * value with the new value if the symbol table already contains the specified key.
	 *
	 * @param key the key
	 * @param value the value
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	void put(K key, V value);

	/**
	 * If the given key is unmapped or is mapped to {@code null}, map it to the given value.
	 * 
	 * @param key the key
	 * @param value the value
	 * @return the previous value at the given key. Yields {@code null} if {@code key} was previously unmapped
	 *         or was mapped to {@code null}
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	V putIfAbsent(K key, V value);

	/**
	 * If the given key is unmapped or is mapped to {@code null}, map it to the value given by a supplier.
	 * 
	 * @param key the key
	 * @param supplier the supplier
	 * @return the previous value at the given key, if it was already mapped to a non-{@code null} value.
	 *         If the key was unmapped or was mapped to {@code null}, yields the new value
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	V computeIfAbsent(K key, Supplier<? extends V> supplier);

	/**
	 * If the given key is unmapped or is mapped to {@code null}, map it to the value given by a supplier.
	 * 
	 * @param key the key
	 * @param supplier the supplier
	 * @return the previous value at the given key, if it was already mapped to a non-{@code null} value.
	 *         If the key was unmapped or was mapped to {@code null}, yields the new value
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	V computeIfAbsent(K key, Function<? super K, ? extends V> supplier);

	/**
	 * Removes the smallest key and associated value from the symbol table.
	 * 
	 * @throws NoSuchElementException if the symbol table is empty
	 */
	void removeMin();

	/**
	 * Removes the largest key and associated value from the symbol table.
	 * 
	 * @throws NoSuchElementException if the symbol table is empty
	 */
	void removeMax();

	/**
	 * Removes the specified key and its associated value from this symbol table     
	 * (if the key is in this symbol table).    
	 *
	 * @param  key the key
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	void remove(Object key);

	/**
	 * Replaces the old value {@code e} at {@code key} with {@code how.apply(e)}.
	 * If {@code key} was unmapped, it will be replaced with {@code how.apply(null)},
	 * which might well lead to a run-time exception.
	 *
	 * @param key the key whose value must be replaced
	 * @param how the replacement function
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	void update(K key, UnaryOperator<V> how);

	/**
	 * Replaces the old value {@code e} at {@code key} with {@code how.apply(e)}.
	 * If {@code key} was unmapped, it will be replaced with {@code how.apply(_default)}.
	 *
	 * @param key the key whose value must be replaced
	 * @param _default the default value
	 * @param how the replacement function
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	void update(K key, V _default, UnaryOperator<V> how);

	/**
	 * Replaces the old value {@code e} at {@code key} with {@code how.apply(e)}.
	 * If {@code key} was unmapped, it will be replaced with {@code how.apply(_default.get())}.
	 *
	 * @param key the key whose value must be replaced
	 * @param _default the supplier of the default value
	 * @param how the replacement function
	 * @throws IllegalArgumentException if {@code key} is {@code null}
	 */
	void update(K key, Supplier<? extends V> _default, UnaryOperator<V> how);

	/**
	 * Removes all bindings from this map.
	 */
	void clear();

	/**
	 * Yields a view of this map. The view reflects the elements in this map:
	 * any future modification of this map will be seen also through the view.
	 * A view is always {@link io.takamaka.code.lang.Exported}.
	 * 
	 * @return a view of this map
	 */
	StorageMapView<K,V> view();
}