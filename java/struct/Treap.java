package struct;

import java.util.Random;

public class Treap {
    private final Random randomSeed = new Random();
    private Node root;

    public class Node {
        private final int key;
        private final int priority;
        Node left;
        Node right;

        public Node(int key) {
            this.key = key;
            this.priority = randomSeed.nextInt();
        }

        public Node(int key, int priority) {
            this.key = key;
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
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

    public TreapPair split(Node t, int key) {
        if (t == null) {
            return new TreapPair(null, null);
        }

        TreapPair tp;

        if (key > t.key) {
            tp = split(t.right, key);
            t.right = tp.left;
            tp.left = t;
        } else {
            tp = split(t.left, key);
            t.left = tp.right;
            tp.right = t;
        }
        return tp;
    }

    public void leftMerge(int key, int priority) {
        root = merge(new Node(key, priority), root);
    }

    private Node merge(Node t1, Node t2) {
        if (t2 == null) {
            return t1;
        } else if (t1 == null) {
            return t2;
        } else if (t1.priority > t2.priority) {
            t1.right = merge(t1.right, t2);
            return t1;
        } else {
            t2.left = merge(t1, t2.left);
            return t2;
        }
    }

    public void insert(int key) {
        root = insert(root, key);
    }

    private Node insert(Node t, int key) {
        TreapPair tp = split(t, key);
        tp.left = merge(tp.left, new Node(key));
        return merge(tp.left, tp.right);
    }

    public void remove(int key) {
        root = remove(root, key);
    }

        private Node remove(Node t, int key) {
        if (key == t.key) {
            return merge(t.left, t.right);
        } else if (key < t.key) {
            t.left = remove(t.left, key);
        } else {
            t.right = remove(t.right, key);
        }
        return t;
    }

    public MtfNodeSearchResult mtfNodeSearch(int key) {
        Node cur = root;
        int result = 0;

        while (cur != null) {
            if (key == cur.key) {
                result += calcSize(cur.left);
                break;
            } else if (key < cur.key) {
                cur = cur.left;
            } else {
                result += calcSize(cur.left) + 1;
                cur = cur.right;
            }
        }
        return new MtfNodeSearchResult(cur, result);
    }

    private int calcSize(Node node) {
        if (node == null) {
            return 0;
        }
        return (calcSize(node.left) + 1 + calcSize(node.right));
    }

    public class MtfNodeSearchResult {
        private final Node node;
        private final int result;

        MtfNodeSearchResult(Node node, int result) {
            this.node = node;
            this.result = result;
        }

        public Node getNode() {
            return node;
        }

        public int getResult() {
            return result;
        }
    }

    public class MtfNodeSearchByteResult {
        private final Node node;
        private final byte result;

        MtfNodeSearchByteResult(Node node, byte result) {
            this.node = node;
            this.result = result;
        }

        public Node getNode() {
            return node;
        }

        public int getResult() {
            return result;
        }
    }
}
