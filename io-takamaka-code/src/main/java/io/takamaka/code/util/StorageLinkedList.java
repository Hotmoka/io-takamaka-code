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

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import io.takamaka.code.lang.Exported;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.StringSupport;
import io.takamaka.code.lang.View;

/**
 * A list of elements that can be kept in storage. It is possible to
 * add and access elements at both sides of the list. This list can hold
 * {@code null} elements.
 *
 * @param <E> the type of the elements. This type must be allowed in storage
 */
public class StorageLinkedList<E> extends Storage implements StorageList<E> {

	/**
	 * The first node of the list.
	 */
	private Node<E> first;

	/**
	 * The last node of the list.
	 */
	private Node<E> last;

	/**
	 * The size of the list.
	 */
	private int size;

	/**
	 * A node of the list.
	 * 
	 * @param <E> the type of the element inside the node
	 */
	private static class Node<E> extends Storage {

		/**
		 * The element inside the node.
		 */
		private final E element;

		/**
		 * The next node.
		 */
		private Node<E> next;

		/**
		 * Builds the node.
		 * 
		 * @param element the element to put inside the node
		 */
		private Node(E element) {
			this.element = element;
		}

		/**
		 * Builds the node.
		 * 
		 * @param element the element to put inside the node
		 * @param next the next node
		 */		
		private Node(E element, Node<E> next) {
			this.element = element;
			this.next = next;
		}
	}

	/**
	 * Creates an empty list.
	 */
	public StorageLinkedList() {
	}

	/**
	 * Creates a list initialized to the same elements as the given parent collection.
	 * 
	 * @param parent the parent collection
	 */
	public StorageLinkedList(Collection<? extends E> parent) {
		parent.forEach(this::add);
	}

	@Override
	public void addFirst(E element) {
		if (first == null)
			first = last = new Node<>(element);
		else
			first = new Node<>(element, first);

		size++;
	}

	@Override
	public void addLast(E element) {
		if (last == null)
			first = last = new Node<>(element);
		else
			last = last.next = new Node<>(element);

		size++;
	}

	@Override
	public void add(E element) {
		addLast(element);
	}

	@Override
	public void clear() {
		first = last = null;
		size = 0;
	}

	@Override
	public E removeFirst() {
		if (first == null)
			throw new NoSuchElementException();
		else {
			E element = first.element;

			if (first == last)
				first = last = null;
			else
				first = first.next;

			size--;
			return element;
		}
	}

	@SuppressWarnings("unchecked")
	private static <K> boolean equals(K e1, K e2) {
		if (e1 instanceof Comparable<?>)
			return ((Comparable<K>) e1).compareTo(e2) == 0;
		else
			return ((Storage) e1).compareByStorageReference((Storage) e2) == 0;
	}

	@Override
	public boolean remove(Object e) {
		for (Node<E> cursor = first, previous = null; cursor != null; previous = cursor, cursor = cursor.next) {
			E element = cursor.element;
			if (e == null ? element == null : (element != null && equals(e, element))) {
				if (last == cursor)
					last = previous;

				if (first == cursor)
					first = cursor.next;

				if (previous != null)
					previous.next = cursor.next;

				size--;

				return true;
			}
		}

		return false;
	}

	@Override
	public @View boolean contains(Object e) {
		for (Node<E> cursor = first; cursor != null; cursor = cursor.next) {
			E element = cursor.element;
			if (e == null ? element == null : (element != null && equals(e, element)))
				return true;
		}

		return false;
	}

	@Override
	public @View E first() {
		if (first == null)
			throw new NoSuchElementException();
		else
			return first.element;
	}

	@Override
	public @View E last() {
		if (last == null)
			throw new NoSuchElementException();
		else
			return last.element;
	}

	@Override
	public @View E get(int index) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException(String.valueOf(index));

		Node<E> cursor = first;
		for (int indexCopy = index; indexCopy > 0; indexCopy--)
			cursor = cursor.next;

		return cursor.element;
	}

	@Override
	public @View int size() {
		return size;
	}

	@Override
	public void forEach(Consumer<? super E> what) {
		for (Node<E> cursor = first; cursor != null; cursor = cursor.next)
			what.accept(cursor.element);
	}

	@Override @View
	public String toString() {
		String result = "[";
		boolean first = true;
		for (E element: this)
			if (first) {
				result = StringSupport.concat(result, element);
				first = false;
			}
			else
				result = StringSupport.concat(result, ",", element);

		return StringSupport.concat(result, "]");
	}

	@Override
	public Iterator<E> iterator() {
		return new Iterator<>() {
			private Node<E> cursor = first;

			@Override
			public boolean hasNext() {
				return cursor != null;
			}

			@Override
			public E next() {
				E result = cursor.element;
				cursor = cursor.next;

				return result;
			}
		};
	}

	@Override
	public Stream<E> stream() {
		var it = iterator();
		return Stream.generate(() -> null)
			.takeWhile(__ -> it.hasNext())
			.map(__ -> it.next());
	}

	@Override
	public E[] toArray(IntFunction<E[]> generator) {
		E[] result = generator.apply(size);
		int pos = 0;
		for (E element: this)
			result[pos++] = element;

		return result;
	}

	@Override
	public StorageListView<E> view() {

		/**
		 * A read-only view of a parent storage list. A view contains the same elements
		 * as the parent storage list, but does not include modification methods.
		 * Moreover, a view is exported, so that it can be safely divulged
		 * outside the store of a node. Calls to the view are simply forwarded to
		 * the parent list.
		 */

		@Exported
		class StorageListViewImpl extends Storage implements StorageListView<E> {

			@Override
			public @View int size() {
				return StorageLinkedList.this.size();
			}

			@Override
			public @View boolean contains(Object value) {
				return StorageLinkedList.this.contains(value);
			}

			@Override
			public Iterator<E> iterator() {
				return StorageLinkedList.this.iterator();
			}

			@Override
			public Stream<E> stream() {
				return StorageLinkedList.this.stream();
			}

			@Override
			public E first() {
				return StorageLinkedList.this.first();
			}

			@Override
			public E last() {
				return StorageLinkedList.this.last();
			}

			@Override
			public E get(int index) {
				return StorageLinkedList.this.get(index);
			}

			@Override
			public String toString() {
				return StorageLinkedList.this.toString();
			}

			@Override
			public E[] toArray(IntFunction<E[]> generator) {
				return StorageLinkedList.this.toArray(generator);
			}

			@Override
			public StorageListView<E> snapshot() {
				return StorageLinkedList.this.snapshot();
			}
		}

		return new StorageListViewImpl();
	}

	@Override
	public StorageListView<E> snapshot() {
		StorageLinkedList<E> copy = new StorageLinkedList<>();
		stream().forEachOrdered(copy::addLast);
		return copy.view();
	}
}