package struct;

import java.util.Random;

public class TreapIndexed {
    private final Random randomSeed = new Random();
    private Node root;

    public class Node {
        private final int value;
        private final int priority;
        int size = 1;

        Node left;
        Node right;

        public Node(int value) {
            this.value = value;
            this.priority = randomSeed.nextInt();
        }

        public Node(int value, int priority) {
            this.value = value;
            this.priority = priority;
        }

        private void updSize() {
            size = 1 + getSize(left) + getSize(right);
        }

        public int getValue() {
            return value;
        }

        public int getPriority() {
            return priority;
        }

        public int getSize(Node t) {
            return t != null ? t.size : 0;
        }
    }

    private class TreapPair {
        private Node left;
        private Node right;

        TreapPair(Node left, Node right) {
            this.left = left;
            this.right = right;
        }
    }

    public TreapPair split(Node t, int k) {
        if (t == null) {
            return new TreapPair(null, null);
        }

        TreapPair tp;

        if (t.getSize(t.left) >= k) {
            tp = split(t.left, k);
            t.left = tp.right;
            tp.right = t;
        } else {
            tp = split(t.right, k - t.getSize(t.left) - 1);
            t.right = tp.left;
            tp.left = t;
        }
        t.updSize();
        return tp;
    }

    public void leftMerge(int value, int priority) {
        root = merge(new Node(value, priority), root);
    }

    private Node merge(Node t1, Node t2) {
        if (t2 == null) {
            return t1;
        } else if (t1 == null) {
            return t2;
        } else if (t1.priority > t2.priority) {
            t1.right = merge(t1.right, t2);
            t1.updSize();
            return t1;
        } else {
            t2.left = merge(t1, t2.left);
            t2.updSize();
            return t2;
        }
    }

    public void insert(int index, int value) {
        root = insert(root, index, value);
    }

    private Node insert(Node t, int index, int value) {
        TreapPair tp = split(t, index);
        return merge(merge(tp.left, new Node(value)), tp.right);
    }

    public void remove(int index) {
        root = remove(root, index);
    }

    private Node remove(Node t, int index) {
        TreapPair tp = split(t, index);
        return merge(tp.left, split(tp.right, index + 1 - t.getSize(tp.left)).right);
    }

    public Node get(int index) {
        Node cur = root;
        int curIndex;

        while (cur != null) {
            curIndex = cur.getSize(cur.left);
            if (index == curIndex) {
                return cur;
            } else if (index < curIndex) {
                cur = cur.left;
            } else {
                index -= curIndex + 1;
                cur = cur.right;
            }
        }

        return null;
    }
}
