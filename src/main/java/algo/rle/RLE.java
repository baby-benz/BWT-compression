package algo.rle;

import java.util.List;
import java.util.ArrayList;

public final class RLE {
    private final static int MAX_SEQUENCE_SIZE = 255 + 2;
    private static int currentSequenceSize = 0;
    private static int previousByte = -1;

    private static final List<Integer> output = new ArrayList<>();

    public static int[] encode(int[] input) {
        for (int cur : input) {
            if (cur == previousByte) {
                currentSequenceSize++;
            } else {
                flushCurrentSequence();
                output.add(cur);
                currentSequenceSize = 1;
            }
            if (currentSequenceSize == MAX_SEQUENCE_SIZE) {
                flushCurrentSequence();
                currentSequenceSize = 1;
            }
            previousByte = cur;
        }

        return output.stream().mapToInt(i -> i).toArray();
    }

    private static void flushCurrentSequence() {
        if (currentSequenceSize < 2) {
            return;
        }

        output.add(currentSequenceSize);
        output.add(previousByte);
        currentSequenceSize = 0;
    }
}
