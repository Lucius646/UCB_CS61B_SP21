package deque;

import java.util.Comparator;

/*不使用继承而是使用转发
public class MaxArrayDeque<T> {
    private final ArrayDeque<T> ad;
    private final Comparator<T> defaultCmp;

    public MaxArrayDeque(Comparator<T> c) {
        ad = new ArrayDeque<>();
        defaultCmp = c;
    }

    public void addFirst(T item) {
        ad.addFirst(item);
    }

    public void addLast(T item) {
        ad.addLast(item);
    }

    public T removeFirst() {
        return ad.removeFirst();
    }

    public T removeLast() {
        return ad.removeLast();
    }

    public int size() {
        return ad.size();
    }

    public boolean isEmpty() {
        return ad.isEmpty();
    }

    public T get(int i) {
        return ad.get(i);
    }

    public void printDeque() {
        ad.printDeque();
    }

    public T max() {
        return maxHelper(defaultCmp);
    }

    public T max(Comparator<T> c) {
        return maxHelper(c);
    }

    private T maxHelper(Comparator<T> cmp) {
        if (ad.isEmpty()) {
            return null;
        }
        T best = ad.get(0);
        for (int i = 1; i < ad.size(); i++) {
            T cur = ad.get(i);
            if (cmp.compare(cur, best) > 0) {
                best = cur;
            }
        }
        return best;
    }
}
*/

//使用继承
public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private final Comparator<T> defaultCmp;

    public MaxArrayDeque(Comparator<T> c) {
        super();  // 调用父类构造器
        defaultCmp = c;
    }

    public T max() {
        return maxHelper(defaultCmp);
    }

    public T max(Comparator<T> c) {
        return maxHelper(c);
    }

    private T maxHelper(Comparator<T> cmp) {
        if (this.isEmpty()) {
            return null;
        }
        T best = this.get(0);
        for (int i = 1; i < this.size(); i++) {
            T cur = this.get(i);
            if (cmp.compare(cur, best) > 0) {
                best = cur;
            }
        }
        return best;
    }
}
