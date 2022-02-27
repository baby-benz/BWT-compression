package algo.mtf;

import helper.Constants;
import struct.Treap;
import struct.TreapIndexed;

import java.util.HashMap;
import java.util.Map;

public final class MTF {
    private static Map<Integer, Integer> getEncodeAlphabet() {
        final Map<Integer, Integer> KEYS = new HashMap<>();

        for (int i = 0; i < Constants.ALPHABET_SIZE; i++) {
            KEYS.put(i, i);
        }

        return KEYS;
    }

    @SuppressWarnings("ConstantConditions")
    public static int[] encode(byte[] input) {
        final Map<Integer, Integer> KEYS = getEncodeAlphabet();
        int[] output = new int[input.length];
        int minkey = 0;

        Treap treap = new Treap();

        KEYS.values().forEach(treap::insert);

        Treap.MtfNodeSearchResult curResult;

        int curInput;

        for (int i = 0; i < input.length; i++) {
            curInput = input[i] & 0xFF;
            curResult = treap.mtfNodeSearch(KEYS.get(curInput));
            output[i] = curResult.getResult();
            treap.remove(KEYS.replace(curInput, --minkey));
            treap.leftMerge(minkey, curResult.getNode().getPriority());
        }

        return output;
    }

    @SuppressWarnings("ConstantConditions")
    public static byte[] encodeAsByte(byte[] input) {
        final Map<Integer, Integer> KEYS = getEncodeAlphabet();
        byte[] output = new byte[input.length];
        int minkey = 0;

        Treap treap = new Treap();

        KEYS.values().forEach(treap::insert);

        Treap.MtfNodeSearchResult curResult;

        int curInput;

        for (int i = 0; i < input.length; i++) {
            curInput = input[i] & 0xFF;
            curResult = treap.mtfNodeSearch(KEYS.get(curInput));
            output[i] = (byte) curResult.getResult();
            treap.remove(KEYS.replace(curInput, --minkey));
            treap.leftMerge(minkey, curResult.getNode().getPriority());
        }

        return output;
    }

    public static byte[] decode(int[] input) {
        byte[] output = new byte[input.length];

        TreapIndexed treap = new TreapIndexed();

        for (int i = 0; i < Constants.ALPHABET_SIZE; i++) {
            treap.insert(i, i);
        }

        TreapIndexed.Node curNode;

        for (int i = 0; i < input.length; i++) {
            curNode = treap.get(input[i]);
            output[i] = (byte) curNode.getValue();
            treap.remove(input[i]);
            treap.leftMerge(curNode.getValue(), curNode.getPriority());
        }

        return output;
    }
}
