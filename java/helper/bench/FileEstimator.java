package helper.bench;

import helper.Constants;

import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class FileEstimator {
    final static Path TEST_FOLDER_NAME = Paths.get("C:\\Users\\amazi\\IdeaProjects\\BWT_compression\\test_files");

    private static int[] calcFreqs(byte[] input) {
        final int[] FREQS = new int[Constants.ALPHABET_SIZE];
        for (byte b : input) {
            FREQS[b & 0xFF]++;
        }
        return FREQS;
    }

    private static int[][] calcDuoFreqs(byte[] input) {
        final int[][] DUO_FREQS = new int[Constants.ALPHABET_SIZE][];
        for (int i = 0; i < input.length - 1; i++) {
            DUO_FREQS[input[i + 1] & 0xFF][input[i] & 0xFF]++;
        }
        return DUO_FREQS;
    }

    private static int[][][] calcTrioFreqs(byte[] input) {
        final int[][][] TRIO_FREQS = new int[Constants.ALPHABET_SIZE][][];
        for (int i = 0; i < input.length - 2; i++) {
            TRIO_FREQS[input[i + 2] & 0xFF][input[i + 1] & 0xFF][input[i] & 0xFF]++;
        }
        return TRIO_FREQS;
    }

    public static double calcEntropy(byte[] input) {
        final int[] FREQS = calcFreqs(input);

        double entropy = 0;
        double curProb;

        for (int freq : FREQS) {
            if (freq > 0) {
                curProb = (double) freq / input.length;
                entropy += curProb * log2(curProb);
            }
        }

        return -1 * entropy;
    }

    public static double calcRelEntropyForOne(byte[] input) {
        final int[] FREQS = calcFreqs(input);
        final int[][] DUO_FREQS = calcDuoFreqs(input);

        double entropy = 0;
        int freq, jointFreq;

        for (int i = 0; i < DUO_FREQS.length; i++) {
            for (int j = 0; j < DUO_FREQS.length; j++) {
                jointFreq = DUO_FREQS[i][j];
                if (jointFreq > 0) {
                    freq = FREQS[i];
                    if (jointFreq != freq) {
                        entropy += jointFreq * 1.0 / input.length * log2(jointFreq * 1.0 / freq);
                    }
                }
            }
        }

        return -1 * entropy;
    }

    public static double calcRelEntropyForTwo(byte[] input) {
        final int[][] DUO_FREQS = calcDuoFreqs(input);
        final int[][][] TRIO_FREQS = calcTrioFreqs(input);

        double entropy = 0;
        int freq, jointFreq;

        for (int i = 0; i < TRIO_FREQS.length; i++) {
            for (int j = 0; j < TRIO_FREQS.length; j++) {
                for (int k = 0; k < TRIO_FREQS.length; k++) {
                    jointFreq = TRIO_FREQS[i][j][k];
                    if (jointFreq > 0) {
                        freq = DUO_FREQS[i][j];
                        if (jointFreq != freq) {
                            entropy += 1.0 * jointFreq / input.length * log2(1.0 * jointFreq / freq);
                        }
                    }
                }
            }
        }

        return -1 * entropy;
    }

    public static double log2(double x) {
        return (Math.log(x) / Math.log(2) + 1e-10);
    }

    public static void main(String[] args) throws IOException {
        List<Path> testFiles = Files.walk(TEST_FOLDER_NAME).filter(Files::isRegularFile).collect(Collectors.toList());
        byte[] file;
        String fileName;
        for (Path filePath : testFiles) {
            file = Files.readAllBytes(filePath);
            fileName = filePath.getFileName().toString();
            System.out.println("H(X) for " + fileName + " is " + calcEntropy(file));
            System.out.println("H(X|X) for " + fileName + " is " + calcRelEntropyForOne(file));
            System.out.println("H(X|XX) for " + fileName + " is " + calcRelEntropyForTwo(file));
            System.out.println("------------------------------------------");
        }
    }
}
