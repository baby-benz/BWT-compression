package algo.bwst;

import helper.Constants;
import struct.DivSufSortSuffixArray;

// Биекционная версия алгоритма Барроуза-Уиллера
// Основное преимущество в отсутствии необходимости в хранении индекса для осуществления обратного преобразования
// Недостаток - работа алгоритма приблизительно на 10% дольше, чем стандартная реализация с использованием суффиксного массива
// Прямое преобразование - порт кода https://code.google.com/p/mk-bwts/
public final class BWST {
    private static final int MAX_BLOCK_SIZE = 1024 * 1024 * 1024; // 1ГБ

    private static final DivSufSortSuffixArray SA_ALGO = new DivSufSortSuffixArray();

    private static class BlockSizeException extends IllegalArgumentException {
        BlockSizeException(int count) {
            super("Максимальный размер BWTS блока " + MAX_BLOCK_SIZE + ", получено: " + count);
        }
    }

    public static byte[] encode(byte[] input) {
        final int count = input.length;

        if (count < 2) {
            return input;
        }

        if (count > MAX_BLOCK_SIZE) {
            throw new BlockSizeException(count);
        }

        final byte[] output = new byte[count];
        final int[] sa = new int[count];
        final int[] isa = new int[count];
        final int srcIdx = 0;
        final int dstIdx = 0;

        SA_ALGO.computeSuffixArray(input, sa, srcIdx, count);

        for (int i = 0; i < count; i++) {
            isa[sa[i]] = i;
        }

        int min = isa[0];
        int idxMin = 0;

        for (int i = 1; ((i < count) && (min > 0)); i++) {
            if (isa[i] >= min) {
                continue;
            }

            int refRank = moveLyndonWordHead(sa, isa, input, count, idxMin, i - idxMin, min);

            for (int j = i - 1; j > idxMin; j--) { // Двигаемся по новому слову Линдона с конца в начало
                int testRank = isa[j];
                int startRank = testRank;

                while (testRank < count - 1) {
                    int nextRankStart = sa[testRank + 1];

                    if ((j > nextRankStart) || (input[srcIdx + j] != input[srcIdx + nextRankStart])
                            || (refRank < isa[nextRankStart + 1])) {
                        break;
                    }

                    sa[testRank] = nextRankStart;
                    isa[nextRankStart] = testRank;
                    testRank++;
                }

                sa[testRank] = j;
                isa[j] = testRank;
                refRank = testRank;

                if (startRank == testRank) {
                    break;
                }
            }

            min = isa[i];
            idxMin = i;
        }

        min = count;
        final int srcIdx2 = srcIdx - 1;

        for (int i = 0; i < count; i++) {
            if (isa[i] >= min) {
                output[dstIdx + isa[i]] = input[srcIdx2 + i];
                continue;
            }

            if (min < count) {
                output[dstIdx + min] = input[srcIdx2 + i];
            }

            min = isa[i];
        }

        output[dstIdx] = input[srcIdx2 + count];
        return output;
    }

    private static int moveLyndonWordHead(int[] sa, int[] isa, byte[] data, int count, int start, int size, int rank) {
        final int end = start + size;

        while (rank + 1 < count) {
            final int nextStart0 = sa[rank + 1];

            if (nextStart0 <= end) {
                break;
            }

            int nextStart = nextStart0;
            int k = 0;

            while ((k < size) && (nextStart < count) && (data[start + k] == data[nextStart])) {
                k++;
                nextStart++;
            }

            if ((k == size) && (rank < isa[nextStart])) {
                break;
            }

            if ((k < size) && (nextStart < count) && ((data[start + k] & 0xFF) < (data[nextStart] & 0xFF))) {
                break;
            }

            sa[rank] = nextStart0;
            isa[nextStart0] = rank;
            rank++;
        }

        sa[rank] = start;
        isa[start] = rank;
        return rank;
    }

    public static byte[] decode(byte[] input) {
        final int count = input.length;

        if (count < 2) {
            return input;
        }

        if (count > MAX_BLOCK_SIZE) {
            throw new BlockSizeException(count);
        }

        final byte[] output = new byte[count];
        final int[] buckets = new int[Constants.ALPHABET_SIZE];
        final int[] lf = new int[count];
        final int srcIdx = 0;
        final int dstIdx = 0;

        for (int i = 0; i < count; i++) {
            buckets[input[srcIdx + i] & 0xFF]++;
        }

        for (int i = 0, sum = 0; i < Constants.ALPHABET_SIZE; i++) { // Заполняем гистограмму
            sum += buckets[i];
            buckets[i] = sum - buckets[i];
        }

        for (int i = 0; i < count; i++) {
            lf[i] = buckets[input[srcIdx + i] & 0xFF]++;
        }

        for (int i = 0, j = dstIdx + count - 1; j >= dstIdx; i++) { // Осуществляем обратное преобразование
            if (lf[i] < 0) {
                continue;
            }

            int p = i;

            do {
                output[j] = input[srcIdx + p];
                j--;
                final int t = lf[p];
                lf[p] = -1;
                p = t;
            } while (lf[p] >= 0);
        }

        return output;
    }
}
