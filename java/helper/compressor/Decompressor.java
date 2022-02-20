package helper.compressor;

import algo.huffman.Huffman;
import helper.Constants;
import helper.reader.BitInputStream;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Decompressor {
    public static int fileSize;

    public static int[] readAndDecompress(Path inFile) throws IOException {
        try (BitInputStream in = new BitInputStream(new BufferedInputStream(new FileInputStream(inFile.toFile())))){
            int unusedBits = in.readNBits(3); // считываем первые 3 бита, содержащие количество неиспользованных бит в конце файла
            int maxCodeLen = in.readNBits(3); // считываем следующие 3 бита, содержащие максимальную длину кодового слова
            int[] canonCode = readCode(in, maxCodeLen);
            Map<String, Integer> code = Huffman.buildCanonicalCodes(canonCode);
            int minCodeLen = Arrays.stream(canonCode).filter(val -> val > 0).summaryStatistics().getMin(); // находим минимальную длину кодового слова
            int[] output = decompress(code, minCodeLen, unusedBits, in);
            fileSize = in.getReadFileSize();
            return output;
        }
    }

    public static int[] readCode(BitInputStream in, int maxCodeLength) throws IOException {
        int[] codeLengths = new int[Constants.ALPHABET_SIZE];
        for (int i = 0; i < codeLengths.length; i++) {
            codeLengths[i] = in.readNBits(maxCodeLength);
        }
        return codeLengths;
    }

    private static int[] decompress(Map<String, Integer> code, int minCodeLen, int unusedBits, BitInputStream in) throws IOException {
        List<Integer> result = new ArrayList<>();

        int curBit, startIndexToRead = 0, lastIndexToRead = minCodeLen;
        Integer decoded = null;
        StringBuilder curByte = new StringBuilder();
        StringBuilder nonDecodedBits = new StringBuilder();

        for (int i = 0; i < 3; i++) {
            curByte.append(in.read());  // первые 6 бит занимает header, поэтому считываем сразу 3 бита,
                                        // чтобы в дальнейшем считывать информацию байтами и быть уверенными,
                                        // что в последнем байте будут как биты, не несущие информацию,
                                        // так и символ конца файла
        }

        read:
        while (true) {
            for (int i = 0; i < 8; i++) {
                curBit = in.read();
                if (curBit == -1) {
                    if (unusedBits > 8 - lastIndexToRead) {
                        lastIndexToRead = 8 - unusedBits;
                    }
                    while (curByte.length() - lastIndexToRead >= unusedBits) {
                        while (decoded == null) {
                            if (curByte.length() - lastIndexToRead < unusedBits) {
                                break read;
                            }
                            nonDecodedBits.append(curByte.substring(startIndexToRead, lastIndexToRead));
                            decoded = code.get(nonDecodedBits.toString());
                            startIndexToRead = lastIndexToRead;
                            lastIndexToRead++;
                        }
                        result.add(decoded);
                        nonDecodedBits.setLength(0);
                        decoded = null;
                    }
                    break read;
                }
                curByte.append(curBit);
            }

            decode:
            while (lastIndexToRead != curByte.length() + 1) {
                while (decoded == null) {
                    if (lastIndexToRead == curByte.length() + 1) {
                        break decode;
                    }
                    nonDecodedBits.append(curByte.substring(startIndexToRead, lastIndexToRead));
                    decoded = code.get(nonDecodedBits.toString());
                    startIndexToRead = lastIndexToRead;
                    lastIndexToRead++;
                }
                result.add(decoded);
                nonDecodedBits.setLength(0);
                decoded = null;
                if (curByte.length() - lastIndexToRead >= minCodeLen) {
                    lastIndexToRead += minCodeLen - 1;
                } else {
                    lastIndexToRead += curByte.length() - lastIndexToRead;
                }
            }

            startIndexToRead = 0;
            if (nonDecodedBits.length() > 0) {
                if (nonDecodedBits.length() >= minCodeLen) {
                    lastIndexToRead = 1;
                } else {
                    lastIndexToRead = minCodeLen - nonDecodedBits.length();
                }
            } else {
                lastIndexToRead = minCodeLen;
            }
            curByte.setLength(0);
        }

        return result.stream().mapToInt(i -> i).toArray();
    }
}
