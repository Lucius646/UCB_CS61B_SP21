package bstmap;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V>{
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> left;
        Node<K, V> right;
        int size; //以该节点为root的子树的总结点数

        Node(K key, V value, int size) {
            this.key = key;
            this.value = value;
            this.size = size;
        }
    }

    private Node<K, V> root;
    private int size;

    public BSTMap() {
        this.root = null;
        this.size = 0;
    }

    @Override
    public void put(K k, V v) {
        if (!containsKey(k)) {
            size++;
        }
        root = put(root, k, v);
    }

    private Node<K, V> put(Node<K, V> node, K k, V v) {
        if (node == null) {
            return new Node<>(k, v, 1);
        }

        int cmp = k.compareTo(node.key);

        if (cmp < 0) {
            node.left = put(node.left, k ,v);
        } else if (cmp > 0) {
            node.right = put(node.right, k, v);
        } else {
            node.value = v;
        }

        int leftSize = (node.left == null) ? 0 : node.left.size;
        int rightSize = (node.right == null) ? 0 : node.right.size;
        node.size = 1 + leftSize + rightSize;

        return node;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public V get(K k) {
        return get(root, k);
    }

    private V get(Node<K, V> node, K k) {
        if (node == null) {
            return null;
        }

        int cmp = k.compareTo(node.key);

        if (cmp < 0) {
            return get(node.left, k);
        } else if (cmp > 0) {
            return get(node.right, k);
        } else {
            return node.value;
        }
    }

    @Override
    public  boolean containsKey(K key) {
        return (get(key) != null);
    }

    public void printInOrder() {
        printInOrder(root);
    }

    private void printInOrder(Node<K, V> node) {
        if (node == null) {
            return;
        }

        printInOrder(node.left);
        System.out.println(node.key.toString() + "->" + node.value.toString());
        printInOrder(node.right);
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new TreeSet<>();
        addKeysInOrder(root, keys);
        return keys;
    }

    private void addKeysInOrder(Node<K, V> node, Set<K> keys) {
        if (node == null) {
            return;
        }

        addKeysInOrder(node.left, keys);
        keys.add(node.key);
        addKeysInOrder(node.right, keys);
    }

    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }

    @Override
    public V remove(K key) {
        V valueToRemove = get(key);
        if (valueToRemove == null) {
            return null;
        }

        root = remove(root, key);
        size --;
        return valueToRemove;
    }

    @Override
    public V remove(K key, V value) {
        V currentV = get(key);
        if (currentV == null || !currentV.equals(value)) {
            return null;
        }

        return remove(key);
    }

    private Node<K, V> remove(Node<K, V> node, K key) {
        if (node == null) {
            return null;
        }

        int cmp = key.compareTo(node.key);

        if (cmp < 0) {
            node.left = remove(node.left, key);
        } else if (cmp > 0) {
            node.right = remove(node.right, key);
        } else {
            if (node.right == null) {
                return node.left;
            }
            if (node.left == null) {
                return node.right;
            }

            Node<K, V> temp = node;
            node = min(temp.right);

            node.right = removeMin(temp.right);
            node.left = temp.left;
        }
        return node;
    }

    private Node<K, V> min(Node<K, V> node) {
        if (node.left == null) {
            return node;
        }
        return min(node.left);
    }

    private Node<K, V> removeMin(Node<K, V> node) {
        if (node.left == null) {
            return node.right;
        }

        node.left = removeMin(node.left);

        int leftSize = (node.left == null) ? 0 : node.left.size;
        int rightSize = (node.right == null) ? 0 : node.right.size;
        node.size = 1 + leftSize + rightSize;

        return node;
    }

    @Override
    public void clear() {
        clear(root);
    }

    private void clear(Node<K, V> node) {
        this.root = null;
        this.size = 0;
    }
}

