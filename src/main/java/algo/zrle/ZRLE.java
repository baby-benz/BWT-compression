package algo.zrle;

import java.util.List;
import java.util.ArrayList;

/*
 * Кодирование длин серий нулей - простой алгоритм кодирования тесно связанный с кодированием длин серий.
 * Основное отличие - алгоритм кодирует только серии из подряд идущих нулей. Также длина кодируется другим способом
 * (каждый разряд в отдельном байте)
 * Алгоритм хорошо подходит для постобработки после применения BWT+MTF
*/
public final class ZRLE {
    private static final int[] LOG2 = {
            0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4,
            4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5,
            5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
            5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8
    };

    public static int[] encode(int[] input) {
        final int count = input.length;

        List<Integer> output = new ArrayList<>();
        int srcIdx = 0;
        int dstIdx = 0;

        while (srcIdx < count) {
            if (input[srcIdx] == 0) {
                int runLength = 1;

                while ((srcIdx + runLength < count) && (input[srcIdx + runLength] == input[srcIdx])) {
                    runLength++;
                }

                srcIdx += runLength;

                // Кодируем длины
                runLength++;
                int log2 = (runLength <= 256) ? LOG2[runLength - 1] : 31 - Integer.numberOfLeadingZeros(runLength);

                if (dstIdx >= count - log2) {
                    break;
                }

                while (log2 > 0) { // Каждый бит записывается в байт, кроме самого значимого
                    log2--;
                    output.add(dstIdx++, (runLength >> log2) & 1);
                }

                continue;
            }

            final int val = input[srcIdx];

            if (val >= 254) {
                if (dstIdx >= count - 1) {
                    break;
                }

                output.add(dstIdx, 255);
                output.add(dstIdx + 1, val - 254);
                dstIdx += 2;
            } else {
                if (dstIdx >= count) {
                    break;
                }

                output.add(dstIdx, val + 1);
                dstIdx++;
            }

            srcIdx++;
        }

        return output.stream().mapToInt(i -> i).toArray();
    }

    public static byte[] encodeBytes(byte[] input) {
        final int count = input.length;

        List<Byte> output = new ArrayList<>();
        int srcIdx = 0;
        int dstIdx = 0;

        while (srcIdx < count) {
            if (input[srcIdx] == 0) {
                int runLength = 1;

                while ((srcIdx + runLength < count) && (input[srcIdx + runLength] == input[srcIdx]))
                    runLength++;

                srcIdx += runLength;

                // Кодируем длины
                runLength++;
                int log2 = (runLength <= 256) ? LOG2[runLength - 1] : 31 - Integer.numberOfLeadingZeros(runLength);

                if (dstIdx >= count - log2) {
                    break;
                }

                while (log2 > 0) { // Каждый бит записывается в байт, кроме самого значимого
                    log2--;
                    output.add(dstIdx++, (byte) ((runLength >> log2) & 1));
                }

                continue;
            }

            final int val = input[srcIdx] & 0xFF;

            if (val >= 0xFE) {
                if (dstIdx >= count - 1) {
                    break;
                }

                output.add(dstIdx, (byte) 0xFF);
                output.add(dstIdx + 1, (byte) (val - 0xFE));
                dstIdx += 2;
            } else {
                if (dstIdx >= count) {
                    break;
                }

                output.add(dstIdx, (byte) (val + 1));
                dstIdx++;
            }

            srcIdx++;
        }

        byte[] primitiveByteOutput = new byte[output.size()];
        for (int i = 0; i < output.size(); i++) {
            primitiveByteOutput[i] = output.get(i);
        }
        return primitiveByteOutput;
    }

    public byte[] decode(byte[] input) {
        if (input.length == 0) {
            return input;
        }

        final int count = input.length;
        int srcIdx = 0;
        int dstIdx = 0;
        List<Byte> output = new ArrayList<>();
        int runLength = 1;

        mainLoop:
        while (true) {
            if (runLength > 1) {
                runLength--;
                input[dstIdx++] = 0;
                continue;
            }

            int val = input[srcIdx] & 0xFF;

            if (val <= 1) {
                // Generate the run length bit by bit (but force MSB)
                runLength = 1;

                do {
                    runLength += (runLength + val);
                    srcIdx++;

                    if (srcIdx >= count)
                        break mainLoop;
                } while ((val = input[srcIdx] & 0xFF) <= 1);

                continue;
            }

            if (val == 0xFF) {
                srcIdx++;

                if (srcIdx >= count) {
                    break;
                }

                output.add(dstIdx, (byte) (0xFE+input[srcIdx]));
            } else {
                output.add(dstIdx, (byte) (val-1));
            }

            srcIdx++;
            dstIdx++;

            if (srcIdx >= count) {
                break;
            }
        }

        final int end = 0;

        while (dstIdx < end) {
            output.add(dstIdx++, (byte) 0);
        }

        byte[] primitiveByteOutput = new byte[output.size()];
        for (int i = 0; i < output.size(); i++) {
            primitiveByteOutput[i] = output.get(i);
        }
        return primitiveByteOutput;
    }

    public static int[] decode(int[] input) {
        if (input.length == 0) {
            return input;
        }

        final int count = input.length;
        int srcIdx = 0;
        int dstIdx = 0;
        final List<Integer> output = new ArrayList<>();
        int runLength = 1;

        mainLoop:
        while (true) {
            if (runLength > 1) {
                runLength--;
                output.add(dstIdx++, 0);
                continue;
            }

            int val = input[srcIdx] & 0xFF;

            if (val <= 1) {
                runLength = 1;

                do {
                    runLength += (runLength + val);
                    srcIdx++;

                    if (srcIdx >= count) {
                        break mainLoop;
                    }
                } while ((val = input[srcIdx] & 0xFF) <= 1);

                continue;
            }

            if (val == 0xFF) {
                srcIdx++;

                if (srcIdx >= count) {
                    break;
                }

                output.add(dstIdx, 0xFE+input[srcIdx]);
            } else {
                output.add(dstIdx, val-1);
            }

            srcIdx++;
            dstIdx++;

            if (srcIdx >= count) {
                break;
            }
        }

        final int end = dstIdx + runLength - 1;

        while (dstIdx < end) {
            output.add(dstIdx++, 0);
        }

        return output.stream().mapToInt(i -> i).toArray();
    }
}
