package helper.compressor;

import algo.huffman.Huffman;
import helper.Constants;
import helper.writer.BitOutputStream;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class Compressor {
    public static int compressAndWrite(Path outFile, int[] encodedFile) throws IOException {
        try (BitOutputStream out = new BitOutputStream(new BufferedOutputStream(new FileOutputStream(outFile.toFile())))) {
            TreeMap<Integer, TreeSet<Integer>> canonTree = Huffman.encode(encodedFile); // строим каноничное дерево Хаффмана
            add3BitsPadding(out); // резервируем место под запись количества неиспользованных бит в конце файла
            writeCodeLengths(out, canonTree); // записываем кодовую таблицу в виде длин кодовых слов Хаффмана
            compress(out, encodedFile, canonTree);
            return out.getWrittenFileSize();
        }
    }

    public static void compressAndWrite(Path outFile, byte[] encodedFile) throws IOException {
        try (BitOutputStream out = new BitOutputStream(new BufferedOutputStream(new FileOutputStream(outFile.toFile())))) {
            TreeMap<Integer, TreeSet<Integer>> canonTree = Huffman.encode(encodedFile);
            add3BitsPadding(out);
            writeCodeLengths(out, canonTree);
            compress(out, encodedFile, canonTree);
        }
    }

    private static void add3BitsPadding(BitOutputStream out) throws IOException {
        for (int i = 0; i < 3; i++) {
            out.write(0);
        }
    }

    private static void writeCodeLengths(BitOutputStream out, TreeMap<Integer, TreeSet<Integer>> canonCode) throws IOException {
        int[] alphabet = new int[Constants.ALPHABET_SIZE];
        TreeSet<Integer> vals;
        for (Integer key : canonCode.keySet()) {
            vals = canonCode.get(key);
            for (Integer val : vals) {
                alphabet[val] = key;
            }
        }

        int maxCodeLength = Integer.toBinaryString(canonCode.lastKey()).length();
        for (char num : Integer.toBinaryString(maxCodeLength).toCharArray()) {
            out.write(Character.getNumericValue(num));
        } // записываем максимальную длину кодового слова

        String binLen;
        for (int i = 0; i < Constants.ALPHABET_SIZE; i++) {
            if (alphabet[i] > 0) {
                binLen = Integer.toBinaryString(alphabet[i]);
                for (int j = 0; j < maxCodeLength - binLen.length(); j++) {
                    out.write(0);
                }
                for (char num : Integer.toBinaryString(alphabet[i]).toCharArray()) {
                    out.write(Character.getNumericValue(num));
                }
            } else {
                for (int j = 0; j < maxCodeLength; j++) {
                    out.write(0);
                }
            }
        }
    }

    private static void compress(BitOutputStream out, int[] encodedFile, TreeMap<Integer, TreeSet<Integer>> canonCode) throws IOException {
        Map<Integer, String> lookupTable = Huffman.buildCanonicalCodes(canonCode);

        String curCode;
        for (int cur : encodedFile) {
            curCode = lookupTable.get(cur);
            for (char num : curCode.toCharArray()) {
                out.write(Character.getNumericValue(num));
            }
        }
    }

    private static void compress(BitOutputStream out, byte[] encodedFile, TreeMap<Integer, TreeSet<Integer>> canonCode) throws IOException {
        Map<Integer, String> lookupTable = Huffman.buildCanonicalCodes(canonCode);

        String curCode;
        for (int cur : encodedFile) {
            curCode = lookupTable.get(cur & 0xFF);
            for (char num : curCode.toCharArray()) {
                out.write(Character.getNumericValue(num));
            }
        }
    }
}
