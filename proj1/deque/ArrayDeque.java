package deque;

import java.util.Iterator;

// 环形数组
public class ArrayDeque<T> implements  Iterable<T>, Deque<T> {
    public T[] items;
    public int size;
    public int head = 0;
    public static final int INIT = 8;

    public ArrayDeque() {
        items = (T[]) new Object[INIT];
        size = 0;
    }

    public int LogicToReal (int logic) {
        return (head + logic) % items.length;
    }

    public void resize(int newCap) {
        Object[] newItems = new Object[newCap];
        for (int i = 0; i < size; i++) {
            newItems[i] = get(i);
        }
        items = (T[]) newItems;
        head = 0;
    }

    @Override
    public void addFirst(T item) {
        if (size == items.length) {
            resize(items.length * 2);
        }
        head = (head - 1 + items.length) % items.length;
        items[head] = item;
        size += 1;
    }

    @Override
    public void addLast(T item) {
        if (size == items.length) {
            resize (items.length * 2);
        }
        int LastIndex = LogicToReal(size);
        items[LastIndex] = item;
        size += 1;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {return size;}

    @Override
    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(get(i) + (i == size - 1 ? "\n" : " "));
        }
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        T item = (T) items[head];
        items[head] = null;
        head = (head + 1) % items.length;
        size -= 1;
        if (items.length >= 16 && size < items.length / 4) {
            resize(items.length / 2);
        }
        return item;

    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        int last = LogicToReal(size - 1);
        T item = (T) items[last];
        items[last] = null;
        size -= 1;
        if (items.length >= 16 && size < items.length / 4) {
            resize(items.length / 2);
        }
        return item;
    }

    @Override
    public T get(int index) {
        int RealIndex = LogicToReal(index);
        return (T) items[RealIndex];
    }

    public  Iterator<T> iterator() {
        return new Iterator<T>() {
            private int pos = 0;
            @Override
            public boolean hasNext() {
                return pos < size;
            }

            @Override
            public T next() {
                T item = get(pos);
                pos += 1;
                return item;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof ArrayDeque)) {
            return false;
        }
        ArrayDeque<?> other = (ArrayDeque<?>) o;
        if (size != other.size) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            T a = get(i);
            Object b = other.get(i);
            if (a == null ? b != null : !a.equals(b)) {
                return false;
            }
        }
        return true;
    }
}
