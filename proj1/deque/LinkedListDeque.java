package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private final Node<T> head;
    private final Node<T> tail;
    private int size = 0;
    
    public LinkedListDeque() {
        head = new Node<T>();
        tail = new Node<T>();
        head.next = tail;
        tail.prev = head;
        size = 0;
    }

    @Override
    public void addFirst(T item) {
        Node<T> newNode = new Node<T>(item);
        newNode.next = head.next;
        newNode.prev = head;
        head.next.prev = newNode;
        head.next = newNode;
        size += 1;
    }

    @Override
    public void addLast(T item) {
        Node<T> newNode = new Node<T>(item);
        newNode.prev = tail.prev;
        newNode.next = tail;
        tail.prev.next = newNode;
        tail.prev = newNode;
        size += 1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        Node<T> current = head.next;
        while (current != tail) {
            System.out.print(current.item + " ");
            current = current.next;
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        Node<T> removeNode = head.next;
        head.next = removeNode.next;
        removeNode.next.prev = head;
        removeNode.prev = null;
        removeNode.next = null;
        size -= 1;
        return removeNode.item;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        Node<T> removeNode = tail.prev;
        tail.prev = removeNode.prev;
        removeNode.prev.next = tail;
        removeNode.prev = null;
        removeNode.next = null;
        size -= 1;
        return removeNode.item;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }

        Node<T> cur = head;
        for (int i = 0; i < index; i++) {
            cur = cur.next;
        }
        return cur.item;
    }

    public T getRecursive(int index) {
        if (index > size) {
            return null;
        }
        return head.next.getItem(index);
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> cur = head.next;   // 指向首元素
            @Override
            public boolean hasNext() {
                return cur != tail;
            }
            @Override
            public T next() {
                if (!hasNext()) {
                    throw new RuntimeException("No more");
                }
                T val = cur.item;
                cur = cur.next;
                return val;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof LinkedListDeque)) {
            return false;
        }
        LinkedListDeque<?> other = (LinkedListDeque<?>) o;
        if (size != other.size) {
            return false;
        }
        Node<T> a = head.next;
        Node<?> b = other.head.next;
        while (a != tail && b != other.tail) {
            if (!a.item.equals(b.item)) {
                return false;
            }
            a = a.next;
            b = b.next;
        }
        return true;
    }

    private static class Node<T> {
        T item;
        Node<T> next;
        Node<T> prev;

        Node(T item) {
            this.item = item;
        }
        
        Node() {
            this(null);
        }

        T getItem(int index) {
            if (index == 0) {
                return this.item;
            }
            return this.next.getItem(index - 1);
        }
    }
}
