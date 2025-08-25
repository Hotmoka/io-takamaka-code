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
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import io.takamaka.code.lang.Exported;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.StorageSupport;
import io.takamaka.code.lang.StringSupport;
import io.takamaka.code.lang.View;

/**
 * A map from storage keys to (possibly {@code null}) storage values,
 * that can be kept in storage. By iterating on this object, one gets
 * the key/value pairs of the map, in increasing key order.
 *
 * This code is derived from Sedgewick and Wayne's code for
 * red-black trees, with some adaptation. It implements an associative
 * map from keys to values. The map can be kept in storage. Keys
 * and values must have types allowed in storage. Keys are kept in
 * comparable order, if they implement {@link java.lang.Comparable}.
 * Otherwise, they must extend {@link io.takamaka.code.lang.Storage} and
 * are kept in storage reference order.
 *
 * This class represents an ordered symbol table of generic key-value pairs.
 * It supports the usual <em>put</em>, <em>get</em>, <em>contains</em>,
 * <em>remove</em>, <em>size</em>, and <em>is-empty</em> methods.
 * It also provides ordered methods for finding the <em>minimum</em>,
 * <em>maximum</em>, <em>floor</em>, and <em>ceiling</em>.
 * A symbol table implements the <em>associative array</em> abstraction:
 * when associating a value with a key that is already in the symbol table,
 * the convention is to replace the old value with the new value.
 * <p>
 * This implementation uses a left-leaning red-black BST. It requires that
 * the key type is a storage class or implements the {@code Comparable} interface
 * and in such a case calls the
 * {@code compareTo()} method to compare two keys. It does not call neither
 * {@code equals()} nor {@code hashCode()}.
 * The <em>put</em>, <em>contains</em>, <em>delete</em>, <em>minimum</em>,
 * <em>maximum</em>, <em>ceiling</em>, and <em>floor</em> operations each take
 * logarithmic time in the worst case, if the tree becomes unbalanced.
 * The <em>size</em>, and <em>is-empty</em> operations take constant time.
 * Construction takes constant time.
 * <p>
 * For additional documentation, see <a href="https://algs4.cs.princeton.edu/33balanced">Section 3.3</a> of
 * <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 * @param <K> the type of the keys
 * @param <V> the type of the values
 */

public class StorageSimpleTreeMap<K,V> extends Storage implements StorageSimpleMap<K,V> {

	/**
	 * The root of the tree.
	 */
	private Node<K,V> root;

	/**
	 * Builds an empty map.
	 */
	public StorageSimpleTreeMap() {}

	private void mkRootBlack() {
		root.color = Node.BLACK;
	}

	private void mkRootRed() {
		root.color = Node.RED;
	}

	/**
	 * A node of the binary search tree that implements the map.
	 */
	private final static class Node<K,V> extends Storage implements Entry<K,V> {
		private boolean color;
		private final static boolean RED = true;
		private final static boolean BLACK = false;
		private final K key; // always non-null
		private V value; // possibly null
		private Node<K,V> left, right;

		/**
		 * Count of the subtree nodes.
		 */
		private int size;

		private Node(boolean color, K key, V value, int size, Node<K,V> left, Node<K,V> right) {
			this.color = color;
			this.key = key;
			this.value = value;
			this.size = size;
			this.left = left;
			this.right = right;
		}

		private static <K,V> Node<K,V> mkBlack(K key, V value, int size, Node<K,V> left, Node<K,V> right) {
			return new Node<>(BLACK, key, value, size, left, right);
		}

		private static <K,V> Node<K,V> mkRed(K key, V value, int size, Node<K,V> left, Node<K,V> right) {
			return new Node<>(RED, key, value, size, left, right);
		}

		private static <K,V> Node<K,V> mkRed(K key, V value) {
			return new Node<>(RED, key, value, 1, null, null);
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public int hashCode() { // unused, but needed to satisfy white-listing for addition of Nodes inside Java collections
			return 42;
		}

		private void setValue(V value) {
			this.value = value;
		}

		private void setLeft(Node<K,V> left) {
			this.left = left;
		}

		private void setRight(Node<K,V> right) {
			this.right = right;
		}

		private Node<K,V> rotateRight() {
			final Node<K,V> x = left;
			Node<K,V> newThis = mkRed(key, value, size(x.right) + size(right) + 1, x.right, right);

			if (color == RED)
				return mkRed(x.key, x.value, size, x.left, newThis);
			else
				return mkBlack(x.key, x.value, size, x.left, newThis);
		}

		private Node<K,V> rotateLeft() {
			final Node<K,V> x = right;
			Node<K,V> newThis = mkRed(key, value, size(x.left) + size(left) + 1, left, x.left);

			if (color == RED)
				return mkRed(x.key, x.value, size, newThis, x.right);
			else
				return mkBlack(x.key, x.value, size, newThis, x.right);
		}

		private void flipColors() {
			color = !color;
			left.color = !left.color;
			right.color = !right.color;
		}

		private void fixSize() {
			size = size(left) + size(right) + 1;
		}

		private Node<K,V> moveRedLeft() {
			flipColors();
			if (isRed(right.left)) {
				setRight(right.rotateRight());
				var result = rotateLeft();
				result.flipColors();
				return result;
			}
			else
				return this;
		}

		private Node<K,V> moveRedRight() {
			flipColors();
			if (isRed(left.left)) {
				var result = rotateRight();
				result.flipColors();
				return result;
			}
			else
				return this;
		}

		// restore red-black tree invariant
		private Node<K,V> balance() {
			Node<K,V> h = this;
			if (isRed(h.right))                      h = h.rotateLeft();
			if (isRed(h.left) && isRed(h.left.left)) h = h.rotateRight();
			if (isRed(h.left) && isRed(h.right))     h.flipColors();

			h.fixSize();
			return h;
		}
	}

	/**
	 * Determines if the given node is red.
	 * 
	 * @param x the node
	 * @return true if and only if {@code x} is red
	 */
	private static <K,V> boolean isRed(Node<K,V> x) {
		return x != null && x.color == Node.RED;
	}

	/**
	 * Determines if the given node is black.
	 * 
	 * @param x the node
	 * @return true if and only if {@code x} is black
	 */
	private static <K,V> boolean isBlack(Node<K,V> x) {
		return x == null || x.color == Node.BLACK;
	}

	/**
	 * Yields the number of nodes in the subtree rooted at x.
	 * 
	 * @param x the root of the subtree
	 * @return the number of nodes. Yields 0 if {@code x} is {@code null}
	 */
	private static <K,V> int size(Node<K,V> x) {
		return x == null ? 0 : x.size;
	}

	@Override
	public @View int size() {
		return size(root);
	}

	@Override
	public @View boolean isEmpty() {
		return root == null;
	}

	@Override
	public @View V get(Object key) {
		if (key == null) throw new IllegalArgumentException("key is null");
		return get(root, key);
	}

	/**
	 * Yields the value associated with the given key in subtree rooted at x;
	 * 
	 * @param x the root of the subtree
	 * @param key the key
	 * @return the value. Yields {@code null} if the key is not found
	 */
	private static <K,V> V get(Node<K,V> x, Object key) {
		while (x != null) {
			int cmp = StorageSupport.compare(key, x.key);
			if      (cmp < 0) x = x.left;
			else if (cmp > 0) x = x.right;
			else              return x.value;
		}
		return null;
	}

	@Override
	public @View V getOrDefault(Object key, V _default) {
		if (key == null) throw new IllegalArgumentException("key is null");
		return getOrDefault(root, key, _default);
	}

	private static <K,V> V getOrDefault(Node<K,V> x, Object key, V _default) {
		while (x != null) {
			int cmp = StorageSupport.compare(key, x.key);
			if      (cmp < 0) x = x.left;
			else if (cmp > 0) x = x.right;
			else              return x.value;
		}
		return _default;
	}

	@Override
	public V getOrDefault(Object key, Supplier<? extends V> _default) {
		if (key == null) throw new IllegalArgumentException("key is null");
		return getOrDefault(root, key, _default);
	}

	// value associated with the given key in subtree rooted at x; uses supplier if no such key is found
	private static <K,V> V getOrDefault(Node<K,V> x, Object key, Supplier<? extends V> _default) {
		while (x != null) {
			int cmp = StorageSupport.compare(key, x.key);
			if      (cmp < 0) x = x.left;
			else if (cmp > 0) x = x.right;
			else              return x.value;
		}
		return _default.get();
	}

	@Override
	public @View boolean containsKey(Object key) {
		return containsKey(root, key);
	}

	/**
	 * Checks if the given key is contained in the subtree rooted at x.
	 * 
	 * @param x the root of the subtree
	 * @param key the key
	 * @return true if and only if that condition holds
	 */
	private static <K,V> boolean containsKey(Node<K,V> x, Object key) {
		while (x != null) {
			int cmp = StorageSupport.compare(key, x.key);
			if      (cmp < 0) x = x.left;
			else if (cmp > 0) x = x.right;
			else              return true;
		}
		return false;
	}

	@Override
	public void put(K key, V value) {
		if (key == null) throw new IllegalArgumentException("key is null");
		root = put(root, key, value);
		mkRootBlack();
	}

	// insert the key-value pair in the subtree rooted at h
	private static <K,V> Node<K,V> put(Node<K,V> h, K key, V value) { 
		if (h == null) return Node.mkRed(key, value);

		int cmp = StorageSupport.compare(key, h.key);
		if      (cmp < 0) h.setLeft(put(h.left, key, value)); 
		else if (cmp > 0) h.setRight(put(h.right, key, value));
		else              h.setValue(value);

		// fix-up any right-leaning links
		if (isRed(h.right) &&  isBlack(h.left))    h = h.rotateLeft();
		if (isRed(h.left)  &&  isRed(h.left.left)) h = h.rotateRight();
		if (isRed(h.left)  &&  isRed(h.right))     h.flipColors();
		
		h.fixSize();
		return h;
	}

	@Override
	public void removeMin() {
		if (isEmpty()) throw new NoSuchElementException();

		// if both children of root are black, set root to red
		if (isBlack(root.left) && isBlack(root.right))
			mkRootRed();

		root = removeMin(root);
		if (!isEmpty()) mkRootBlack();
	}

	// delete the key-value pair with the minimum key rooted at h
	private static <K,V> Node<K,V> removeMin(Node<K,V> h) { 
		if (h.left == null)
			return null;

		if (isBlack(h.left) && isBlack(h.left.left))
			h = h.moveRedLeft();

		h.setLeft(removeMin(h.left));
		h.balance();
		return h;
	}

	@Override
	public void removeMax() {
		if (isEmpty()) throw new NoSuchElementException();

		// if both children of root are black, set root to red
		if (isBlack(root.left) && isBlack(root.right))
			mkRootRed();

		root = removeMax(root);
		if (!isEmpty()) mkRootBlack();
	}

	// delete the key-value pair with the maximum key rooted at h
	private static <K,V> Node<K,V> removeMax(Node<K,V> h) { 
		if (isRed(h.left))
			h = h.rotateRight();

		if (h.right == null)
			return null;

		if (isBlack(h.right) && isBlack(h.right.left))
			h = h.moveRedRight();

		h.setRight(removeMax(h.right));
		return h.balance();
	}

	@Override
	public void remove(Object key) { 
		if (key == null) throw new IllegalArgumentException("key is null");
		if (containsKey(key)) {
			// if both children of root are black, set root to red
			if (isBlack(root.left) && isBlack(root.right))
				mkRootRed();

			root = remove(root, key);
			if (!isEmpty()) mkRootBlack();
		}
	}

	// delete the key-value pair with the given key rooted at h
	private static <K,V> Node<K,V> remove(Node<K,V> h, Object key) { 
		// assert get(h, key) != null;

		if (StorageSupport.compare(key, h.key) < 0)  {
			if (isBlack(h.left) && isBlack(h.left.left))
				h = h.moveRedLeft();

			h.setLeft(remove(h.left, key));
		}
		else {
			if (isRed(h.left))
				h = h.rotateRight();
			if (StorageSupport.compare(key, h.key) == 0 && (h.right == null))
				return null;
			if (isBlack(h.right) && isBlack(h.right.left))
				h = h.moveRedRight();
			if (StorageSupport.compare(key, h.key) == 0) {
				var x = min(h.right);
				if (isRed(h))
					h = Node.mkRed(x.key, x.value, h.size, h.left, removeMin(h.right));
				else
					h = Node.mkBlack(x.key, x.value, h.size, h.left, removeMin(h.right));
			}
			else
				h.setRight(remove(h.right, key));
		}

		return h.balance();
	}

	@Override
	public @View K min() {
		if (isEmpty()) throw new NoSuchElementException("call to min() with empty symbol table");
		return min(root).key;
	} 

	// the smallest key in subtree rooted at x
	private static <K,V> Node<K,V> min(Node<K,V> x) { 
		// assert x != null;
		if (x.left == null) return x; 
		else                return min(x.left); 
	} 

	@Override
	public @View K max() {
		if (isEmpty()) throw new NoSuchElementException("call to max() with empty symbol table");
		return max(root).key;
	} 

	// the largest key in the subtree rooted at x
	private static <K,V> Node<K,V> max(Node<K,V> x) { 
		// assert x != null;
		if (x.right == null) return x; 
		else                 return max(x.right); 
	}

	@Override
	public @View K floorKey(K key) {
		if (key == null) throw new IllegalArgumentException("key is null");
		if (isEmpty()) throw new NoSuchElementException();
		var x = floorKey(root, key);
		if (x == null) throw new NoSuchElementException();
		else           return x.key;
	}    

	// the largest key in the subtree rooted at x less than or equal to the given key
	private static <K,V> Node<K,V> floorKey(Node<K,V> x, K key) {
		if (x == null) return null;
		int cmp = StorageSupport.compare(key, x.key);
		if (cmp == 0) return x;
		if (cmp < 0)  return floorKey(x.left, key);
		var t = floorKey(x.right, key);
		if (t != null) return t; 
		else           return x;
	}

	@Override
	public @View K ceilingKey(K key) {
		if (key == null) throw new IllegalArgumentException("key is null");
		if (isEmpty()) throw new NoSuchElementException();
		var x = ceilingKey(root, key);
		if (x == null) throw new NoSuchElementException();
		else           return x.key;  
	}

	// the smallest key in the subtree rooted at x greater than or equal to the given key
	private static <K,V> Node<K,V> ceilingKey(Node<K,V> x, K key) {  
		if (x == null) return null;
		int cmp = StorageSupport.compare(key, x.key);
		if (cmp == 0) return x;
		if (cmp > 0)  return ceilingKey(x.right, key);
		var t = ceilingKey(x.left, key);
		if (t != null) return t; 
		else           return x;
	}

	@Override
	public @View K select(int k) {
		if (k < 0 || k >= size()) throw new IllegalArgumentException(StringSupport.concat("argument to select() is invalid: ", k));
		return select(root, k).key;
	}

	// the key of rank k in the subtree rooted at x
	private static <K,V> Node<K,V> select(Node<K,V> x, int k) {
		// assert x != null;
		// assert k >= 0 && k < size(x);
		int t = size(x.left); 
		if      (t > k) return select(x.left,  k); 
		else if (t < k) return select(x.right, k-t-1); 
		else            return x; 
	} 

	@Override
	public @View int rank(K key) {
		if (key == null) throw new IllegalArgumentException("key is null");
		return rank(key, root);
	} 

	// number of keys less than key in the subtree rooted at x
	private static <K,V> int rank(K key, Node<K,V> x) {
		if (x == null) return 0; 
		int cmp = StorageSupport.compare(key, x.key); 
		if      (cmp < 0) return rank(key, x.left); 
		else if (cmp > 0) return 1 + size(x.left) + rank(key, x.right); 
		else              return size(x.left);
	} 

	@Override
	public void update(K key, UnaryOperator<V> how) {
		if (key == null) throw new IllegalArgumentException("key is null");
		root = update(root, key, how);
		mkRootBlack();
	}

	private static <K,V> Node<K,V> update(Node<K,V> h, K key, UnaryOperator<V> how) { 
		if (h == null) return Node.mkRed(key, how.apply(null));

		int cmp = StorageSupport.compare(key, h.key);
		if      (cmp < 0) h.setLeft(update(h.left,  key, how)); 
		else if (cmp > 0) h.setRight(update(h.right, key, how)); 
		else              h.setValue(how.apply(h.value));

		// fix-up any right-leaning links
		if (isRed(h.right) && isBlack(h.left))     h = h.rotateLeft();
		if (isRed(h.left)  &&  isRed(h.left.left)) h = h.rotateRight();
		if (isRed(h.left)  &&  isRed(h.right))     h.flipColors();

		h.fixSize();
		return h;
	}

	@Override
	public void update(K key, V _default, UnaryOperator<V> how) {
		if (key == null) throw new IllegalArgumentException("key is null");
		root = update(root, key, _default, how);
		mkRootBlack();
	}

	private static <K,V> Node<K,V> update(Node<K,V> h, K key, V _default, UnaryOperator<V> how) { 
		if (h == null) return Node.mkRed(key, how.apply(_default));

		int cmp = StorageSupport.compare(key, h.key);
		if      (cmp < 0) h.setLeft(update(h.left, key, _default, how)); 
		else if (cmp > 0) h.setRight(update(h.right, key, _default, how));
		else if (h.value == null)
			h.setValue(how.apply(_default));
		else
			h.setValue(how.apply(h.value));

		// fix-up any right-leaning links
		if (isRed(h.right) && isBlack(h.left))     h = h.rotateLeft();
		if (isRed(h.left)  &&  isRed(h.left.left)) h = h.rotateRight();
		if (isRed(h.left)  &&  isRed(h.right))     h.flipColors();

		h.fixSize();
		return h;
	}

	@Override
	public void update(K key, Supplier<? extends V> _default, UnaryOperator<V> how) {
		if (key == null) throw new IllegalArgumentException("key is null");
		root = update(root, key, _default, how);
		mkRootBlack();
	}

	private static <K,V> Node<K,V> update(Node<K,V> h, K key, Supplier<? extends V> _default, UnaryOperator<V> how) { 
		if (h == null) return Node.mkRed(key, how.apply(_default.get()));

		int cmp = StorageSupport.compare(key, h.key);
		if      (cmp < 0) h.setLeft(update(h.left, key, _default, how)); 
		else if (cmp > 0) h.setRight(update(h.right, key, _default, how));
		else if (h.value == null)
			h.setValue(how.apply(_default.get()));
		else
			h.setValue(how.apply(h.value));

		// fix-up any right-leaning links
		if (isRed(h.right) && isBlack(h.left))     h = h.rotateLeft();
		if (isRed(h.left)  &&  isRed(h.left.left)) h = h.rotateRight();
		if (isRed(h.left)  &&  isRed(h.right))     h.flipColors();

		h.fixSize();
		return h;
	}

	@Override
	public V putIfAbsent(K key, V value) {
		if (key == null) throw new IllegalArgumentException("key is null");

		class PutIfAbsent {
			private V result;

			private Node<K,V> putIfAbsent(Node<K,V> h) {
				// not found: result remains null
				if (h == null)
					// not found
					return Node.mkRed(key, value);

				int cmp = StorageSupport.compare(key, h.key);
				if      (cmp < 0) h.setLeft(putIfAbsent(h.left));
				else if (cmp > 0) h.setRight(putIfAbsent(h.right));
				else if (h.value == null) {
					// found but was bound to null: result remains null
					h.setValue(value);
					return h;
				}
				else {
					// found and was bound to a non-null value
					result = h.value;
					return h;
				}

				// fix-up any right-leaning links
				if (isRed(h.right) && isBlack(h.left))     h = h.rotateLeft();
				if (isRed(h.left)  &&  isRed(h.left.left)) h = h.rotateRight();
				if (isRed(h.left)  &&  isRed(h.right))     h.flipColors();

				h.fixSize();
				return h;
			}
		}

		PutIfAbsent pia = new PutIfAbsent();
		root = pia.putIfAbsent(root);
		mkRootBlack();

		return pia.result;
	}

	@Override
	public V computeIfAbsent(K key, Supplier<? extends V> supplier) {
		if (key == null) throw new IllegalArgumentException("key is null");

		class ComputeIfAbsent {
			private V result;

			private Node<K,V> computeIfAbsent(Node<K,V> h) { 
				if (h == null)
					// not found
					return Node.mkRed(key, result = supplier.get());

				int cmp = StorageSupport.compare(key, h.key);
				if      (cmp < 0) h.setLeft(computeIfAbsent(h.left));
				else if (cmp > 0) h.setRight(computeIfAbsent(h.right));
				else if (h.value == null) {
					// found but was bound to null
					h.setValue(supplier.get());
					result = h.value;
					return h;
				}
				else {
					// found and was bound to a non-null value
					result = h.value;
					return h;
				}

				// fix-up any right-leaning links
				if (isRed(h.right) && isBlack(h.left))     h = h.rotateLeft();
				if (isRed(h.left)  &&  isRed(h.left.left)) h = h.rotateRight();
				if (isRed(h.left)  &&  isRed(h.right))     h.flipColors();

				h.fixSize();
				return h;
			}
		}

		ComputeIfAbsent cia = new ComputeIfAbsent();
		root = cia.computeIfAbsent(root);
		mkRootBlack();

		return cia.result;
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> supplier) {
		if (key == null) throw new IllegalArgumentException("key is null");

		class ComputeIfAbsent {
			private V result;

			private Node<K,V> computeIfAbsent(Node<K,V> h) { 
				if (h == null)
					// not found
					return Node.mkRed(key, result = supplier.apply(key));

				int cmp = StorageSupport.compare(key, h.key);
				if      (cmp < 0) h.setLeft(computeIfAbsent(h.left));
				else if (cmp > 0) h.setRight(computeIfAbsent(h.right));
				else if (h.value == null) {
					// found but was bound to null
					h.setValue(supplier.apply(key));
					result = h.value;
					return h;
				}
				else {
					// found and was bound to a non-null value
					result = h.value;
					return h;
				}

				// fix-up any right-leaning links
				if (isRed(h.right) && isBlack(h.left))     h = h.rotateLeft();
				if (isRed(h.left)  &&  isRed(h.left.left)) h = h.rotateRight();
				if (isRed(h.left)  &&  isRed(h.right))     h.flipColors();

				h.fixSize();
				return h;
			}
		}

		ComputeIfAbsent cia = new ComputeIfAbsent();
		root = cia.computeIfAbsent(root);
		mkRootBlack();

		return cia.result;
	}

	@Override
	public void clear() {
		root = null;
	}

	@Override
	public Iterator<Entry<K,V>> iterator() {
		return new StorageMapIterator<>(root);
	}

	private static class Stack<V> {
		private final V head;
		private final Stack<V> tail;

		private Stack(V head, Stack<V> tail) {
			this.head = head;
			this.tail = tail;
		}
	}

	private static class StorageMapIterator<K,V> implements Iterator<Entry<K,V>> {
		// the path under enumeration; it holds that the left children
		// have already been enumerated
		private Stack<Node<K,V>> stack = null;

		private StorageMapIterator(Node<K,V> root) {
			// initially, the stack contains the leftmost path of the tree
			for (var cursor = root; cursor != null; cursor = cursor.left)
				stack = new Stack<>(cursor, stack);
		}

		@Override
		public boolean hasNext() {
			return stack != null;
		}

		@Override
		public Entry<K,V> next() {
			var topmost = stack.head;
			stack = stack.tail;

			// we add the leftmost path of the right child of topmost
			for (var cursor = topmost.right; cursor != null; cursor = cursor.left)
				stack = new Stack<>(cursor, stack);

			return topmost;
		}
	}

	@Override
	public void forEach(Consumer<? super Entry<K, V>> action) {
		for (var entry: this)
			action.accept(entry);
	}

	@Override
	public void forEachKey(Consumer<? super K> action) {
		forEach(entry -> action.accept(entry.getKey()));
	}

	@Override
	public void forEachValue(Consumer<? super V> action) {
		forEach(entry -> action.accept(entry.getValue()));
	}

	@Override
	public StorageSimpleMapView<K,V> view() {

		/**
		 * A read-only view of a parent storage map. A view contains the same bindings
		 * as the parent storage map, but does not include modification methods.
		 * Moreover, a view is exported, so that it can be safely divulged outside
		 * the store of a node. Calls to the view are simply forwarded to the parent map.
		 */

		@Exported
		class StorageSimpleMapViewImpl extends Storage implements StorageSimpleMapView<K,V> {

			@Override
			public @View int size() {
				return StorageSimpleTreeMap.this.size();
			}

			@Override
			public @View boolean isEmpty() {
				return StorageSimpleTreeMap.this.isEmpty();
			}

			@Override
			public @View boolean containsKey(Object value) {
				return StorageSimpleTreeMap.this.containsKey(value);
			}

			@Override
			public Iterator<Entry<K, V>> iterator() {
				return StorageSimpleTreeMap.this.iterator();
			}

			@Override
			public V get(Object key) {
				return StorageSimpleTreeMap.this.get(key);
			}

			@Override
			public V getOrDefault(Object key, V _default) {
				return StorageSimpleTreeMap.this.getOrDefault(key, _default);
			}

			@Override
			public V getOrDefault(Object key, Supplier<? extends V> _default) {
				return StorageSimpleTreeMap.this.getOrDefault(key, _default);
			}

			@Override
			public K min() {
				return StorageSimpleTreeMap.this.min();
			}

			@Override
			public K max() {
				return StorageSimpleTreeMap.this.max();
			}

			@Override
			public K floorKey(K key) {
				return StorageSimpleTreeMap.this.floorKey(key);
			}

			@Override
			public K ceilingKey(K key) {
				return StorageSimpleTreeMap.this.ceilingKey(key);
			}

			@Override
			public K select(int k) {
				return StorageSimpleTreeMap.this.select(k);
			}

			@Override
			public int rank(K key) {
				return StorageSimpleTreeMap.this.rank(key);
			}

			@Override
			public void forEach(Consumer<? super Entry<K, V>> action) {
				StorageSimpleTreeMap.this.forEach(action);
			}

			@Override
			public void forEachKey(Consumer<? super K> action) {
				StorageSimpleTreeMap.this.forEachKey(action);
			}

			@Override
			public void forEachValue(Consumer<? super V> action) {
				StorageSimpleTreeMap.this.forEachValue(action);
			}
		}

		return new StorageSimpleMapViewImpl();
	}
}