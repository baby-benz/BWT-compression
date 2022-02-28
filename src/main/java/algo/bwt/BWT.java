package algo.bwt;

import helper.Constants;
import struct.SuffixArray;

public final class BWT {
    public static int encode(byte[] input, byte[] output) {
        int[] A = new int[input.length];
        int i, pidx;

        if (output == null) {
            return -1;
        }

        if (input.length <= 1) {
            if (input.length == 1) {
                output[0] = input[0];
            }
            return input.length;
        }

        pidx = SuffixArray.SA_IS(new SuffixArray.ByteArray(input, 0), A, 0, input.length, Constants.ALPHABET_SIZE, true);
        output[0] = input[input.length - 1];

        for (i = 0; i < pidx; ++i) {
            output[i + 1] = (byte) (A[i] & 0xFF);
        }

        for (i += 1; i < input.length; ++i) {
            output[i] = (byte) (A[i] & 0xFF);
        }

        return pidx + 1;
    }

    public static byte[] decode(byte[] input, int idx) {
        int[] freqs = new int[Constants.ALPHABET_SIZE];
        int[] A = new int[input.length];
        byte[] output = new byte[input.length];

        int i, p;
        int c, len, half;

        if (input.length <= 1) {
            return input;
        }

        for (i = 0; i < input.length; ++i) {
            ++freqs[input[i] & 0xFF];
        }

        for (c = 0, i = 0; c < Constants.ALPHABET_SIZE; ++c) {
            p = freqs[c];
            freqs[c] = i;
            i += p;
        }

        for (i = 0; i < idx; ++i) {
            A[freqs[input[i] & 0xFF]++] = i;
        }

        for (; i < input.length; ++i) {
            A[freqs[input[i] & 0xFF]++] = i + 1;
        }

        for (i = 0, p = idx; i < input.length; ++i) {
            for (c = 0, len = Constants.ALPHABET_SIZE, half = len >> 1;
                 0 < len;
                 len = half, half >>= 1) {
                if (freqs[c + half] < p) {
                    c += half + 1;
                    half -= (len & 1) ^ 1;
                }
            }
            output[i] = (byte) c;
            p = A[p - 1];
        }

        return output;
    }
}


