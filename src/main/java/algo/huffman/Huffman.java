package algo.huffman;

import helper.Constants;

import java.util.*;

import static struct.Queue.QueueNode;
import static struct.Queue.buildHuffmanTree;

public final class Huffman {
    public static TreeMap<Integer, TreeSet<Integer>> encode(int[] input) {
        final int[] FREQS = new int[Constants.ALPHABET_SIZE];

        for (int value : input) FREQS[value]++;

        Pair[] values = new Pair[Constants.ALPHABET_SIZE];

        for (int i = 0; i < Constants.ALPHABET_SIZE; i++) {
            values[i] = new Pair(i, FREQS[i]);
        }

        radixSort(values);

        int[] sortedFreqs = new int[Constants.ALPHABET_SIZE];
        int[] inds = new int[Constants.ALPHABET_SIZE];

        for (int i = 0; i < Constants.ALPHABET_SIZE; i++) {
            sortedFreqs[i] = values[i].getVal();
            inds[i] = values[i].getIndex();
        }

        return CanonicalHuffman.buildCanonicalTree(inds, sortedFreqs);
    }

    public static TreeMap<Integer, TreeSet<Integer>> encode(byte[] input) {
        final int[] FREQS = new int[Constants.ALPHABET_SIZE];

        for (int value : input) FREQS[value & 0xFF]++;

        Pair[] values = new Pair[Constants.ALPHABET_SIZE];

        for (int i = 0; i < Constants.ALPHABET_SIZE; i++) {
            values[i] = new Pair(i, FREQS[i]);
        }

        radixSort(values);

        int[] sortedFreqs = new int[Constants.ALPHABET_SIZE];
        int[] inds = new int[Constants.ALPHABET_SIZE];

        for (int i = 0; i < Constants.ALPHABET_SIZE; i++) {
            sortedFreqs[i] = values[i].getVal();
            inds[i] = values[i].getIndex();
        }

        return CanonicalHuffman.buildCanonicalTree(inds, sortedFreqs);
    }

    public static Map<Integer, String> buildCanonicalCodes(TreeMap<Integer, TreeSet<Integer>> canonicalTree) {
        Map<Integer, String> canonicalCodes = new HashMap<>();

        Integer[] arr = canonicalTree.keySet().toArray(new Integer[0]);

        int cCode = 0, nextLen;
        Iterator<Integer> it;
        StringBuilder resultCode;
        String curCCode;

        for (int i = 0; i < arr.length; i++) {
            it = canonicalTree.get(arr[i]).iterator();

            while (it.hasNext()) {
                curCCode = Integer.toBinaryString(cCode);
                resultCode = new StringBuilder();
                resultCode.append("0".repeat(Math.max(0, arr[i] - curCCode.length())));
                resultCode.append(curCCode);
                canonicalCodes.put(it.next(), resultCode.toString());

                if (it.hasNext() || i == arr.length - 1) {
                    nextLen = arr[i];
                } else {
                    nextLen = arr[i + 1];
                }

                cCode = (cCode + 1) << (nextLen - arr[i]);
            }
        }
        return canonicalCodes;
    }

    public static Map<String, Integer> buildCanonicalCodes(int[] codeLengths) {
        Map<String, Integer> canonicalCodes = new HashMap<>();
        Pair[] values = new Pair[codeLengths.length];

        for (int i = 0, j = 0; i < codeLengths.length; i++) {
            if (codeLengths[i] > 0) {
                values[j] = new Pair(i, codeLengths[i]);
                j++;
            }
        }

        values = Arrays.stream(values).filter(Objects::nonNull).toArray(Pair[]::new);

        radixSort(values);

        int cCode = 0, nextLen;
        StringBuilder resultCode;
        String curCCode;

        for (int i = 0; i < values.length; i++) {
            if (values[i].getVal() > 0) {
                curCCode = Integer.toBinaryString(cCode);
                resultCode = new StringBuilder();
                resultCode.append("0".repeat(Math.max(0, values[i].getVal() - curCCode.length())));
                resultCode.append(curCCode);
                canonicalCodes.put(resultCode.toString(), values[i].getIndex());

                if (i == values.length - 1 || (i + 1 < values.length && values[i].getVal() >= values[i + 1].getVal())) {
                    nextLen = values[i].getVal();
                } else {
                    nextLen = values[i + 1].getVal();
                }

                cCode = (cCode + 1) << (nextLen - values[i].getVal());
            }
        }
        return canonicalCodes;
    }

    private static void radixSort(Pair[] numbers) {
        int maximumNumber = max(numbers);
        int numberOfDigits = numOfDigits(maximumNumber);
        int placeValue = 1;

        while (numberOfDigits-- > 0) {
            countingSort(numbers, placeValue);
            placeValue *= 10;
        }
    }

    private static void countingSort(Pair[] numbers, int placeValue) {
        int range = 10;
        int length = numbers.length;
        int[] frequency = new int[range];
        Pair[] sortedValues = new Pair[length];

        for (Pair number : numbers) {
            int digit = (number.getVal() / placeValue) % range;
            frequency[digit]++;
        }

        for (int i = 1; i < range; i++) {
            frequency[i] += frequency[i - 1];
        }

        for (int i = length - 1; i >= 0; i--) {
            int digit = (numbers[i].getVal() / placeValue) % range;
            sortedValues[frequency[digit] - 1] = new Pair(numbers[i].getIndex(), numbers[i].getVal());
            frequency[digit]--;
        }

        System.arraycopy(sortedValues, 0, numbers, 0, length);
    }

    private static int numOfDigits(int number) {
        return (int) Math.log10(number) + 1;
    }

    private static int max(Pair[] numbers) {
        return Arrays.stream(numbers).map(Pair::getVal).max(Integer::compare).get();
    }

    private static class CanonicalHuffman {
        static TreeMap<Integer, TreeSet<Integer>> data = new TreeMap<>();

        private static void generateCanonicalCodes(QueueNode root, int codeLen) {
            if (root == null) {
                return;
            }

            if (root.getLeft() == null && root.getRight() == null) {
                data.putIfAbsent(codeLen, new TreeSet<>());
                data.get(codeLen).add(root.getData());
                return;
            }

            generateCanonicalCodes(root.getLeft(), codeLen + 1);
            generateCanonicalCodes(root.getRight(), codeLen + 1);
        }

        public static TreeMap<Integer, TreeSet<Integer>> buildCanonicalTree(int[] chars, int[] freqs) {
            QueueNode root = buildHuffmanTree(chars, freqs);
            generateCanonicalCodes(root, 0);
            return data;
        }
    }

    private static class Pair {
        private final int index;
        private final int val;

        Pair(int index, int val) {
            this.index = index;
            this.val = val;
        }

        public int getIndex() {
            return index;
        }

        public int getVal() {
            return val;
        }
    }
}
