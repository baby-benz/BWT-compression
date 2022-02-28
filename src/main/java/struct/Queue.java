package struct;

import helper.Constants;

public class Queue {
    int front, rear;
    int capacity;
    QueueNode[] array;

    Queue(int capacity) {
        this.front = this.rear = -1;
        this.capacity = capacity;
        this.array = new QueueNode[this.capacity];
    }

    private static boolean isSizeOne(Queue queue) {
        return queue.front == queue.rear && queue.front != -1;
    }

    private static boolean isEmpty(Queue queue) {
        return queue.front == -1;
    }

    private static boolean isFull(Queue queue) {
        return queue.rear == queue.capacity - 1;
    }

    private static void enQueue(Queue queue, QueueNode item) {
        if (isFull(queue)) {
            return;
        }
        queue.array[++queue.rear] = item;
        if (queue.front == -1) {
            ++queue.front;
        }
    }

    private static QueueNode deQueue(Queue queue) {
        if (isEmpty(queue)) {
            return null;
        }
        QueueNode temp = queue.array[queue.front];
        if (queue.front == queue.rear) {
            queue.front = queue.rear = -1;
        } else {
            ++queue.front;
        }
        return temp;
    }

    private static QueueNode getFront(Queue queue) {
        if (isEmpty(queue)) {
            return null;
        }
        return queue.array[queue.front];
    }

    private static QueueNode findMin(Queue firstQueue, Queue secondQueue) {
        if (isEmpty(firstQueue)) {
            return deQueue(secondQueue);
        }

        if (isEmpty(secondQueue)) {
            return deQueue(firstQueue);
        }

        if (getFront(firstQueue).freq < getFront(secondQueue).freq) {
            return deQueue(firstQueue);
        }

        return deQueue(secondQueue);
    }

    public static QueueNode buildHuffmanTree(int[] data, int[] freqs) {
        QueueNode left, right, top;

        Queue firstQueue = new Queue(Constants.ALPHABET_SIZE);
        Queue secondQueue = new Queue(Constants.ALPHABET_SIZE);

        for (int i = 0; i < freqs.length; ++i) {
            if (freqs[i] > 0) {
                enQueue(firstQueue, new QueueNode(data[i], freqs[i]));
            }
        }

        while (!(isEmpty(firstQueue) && isSizeOne(secondQueue))) {
            left = findMin(firstQueue, secondQueue);
            right = findMin(firstQueue, secondQueue);

            top = new QueueNode('$', left.freq + right.freq);
            top.left = left;
            top.right = right;
            enQueue(secondQueue, top);
        }

        return deQueue(secondQueue);
    }

    public static QueueNode buildRelHuffmanTree(int[] data, int[] freqs) {
        QueueNode left, right, top;

        Queue firstQueue = new Queue(Constants.ALPHABET_SIZE*Constants.ALPHABET_SIZE*Constants.ALPHABET_SIZE);
        Queue secondQueue = new Queue(Constants.ALPHABET_SIZE*Constants.ALPHABET_SIZE*Constants.ALPHABET_SIZE);

        for (int i = 0; i < freqs.length; ++i) {
            if (freqs[i] > 0) {
                enQueue(firstQueue, new QueueNode(data[i], freqs[i]));
            }
        }

        while (!(isEmpty(firstQueue) && isSizeOne(secondQueue))) {
            left = findMin(firstQueue, secondQueue);
            right = findMin(firstQueue, secondQueue);

            top = new QueueNode('$', left.freq + right.freq);
            top.left = left;
            top.right = right;
            enQueue(secondQueue, top);
        }

        return deQueue(secondQueue);
    }

    public static class QueueNode {
        int data;
        int freq;
        QueueNode left = null, right = null;

        QueueNode(int data, int freq) {
            this.data = data;
            this.freq = freq;
        }

        public int getData() {
            return data;
        }

        public QueueNode getLeft() {
            return left;
        }

        public QueueNode getRight() {
            return right;
        }
    }
}
 
