import algo.bwst.BWST;
import algo.mtf.MTF;
import algo.zrle.ZRLE;
import helper.Constants;
import helper.compressor.Compressor;
import helper.compressor.Decompressor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 1) {
            Path filePath = Path.of(args[0]);

            encodeOrDecode(filePath);
        } else if (args.length == 2) {
            Path inFilePath = Path.of(args[0]);
            Path outFilePath = Path.of(args[1]);

            encodeOrDecode(inFilePath, outFilePath);
        } else {
            printHelp();
        }
    }

    private static void printHelp() {
        System.out.println("Run format: java -jar bwt.jar input(.bwt) (output)");
        System.out.println();
        System.out.println("EG. java -jar bwt.jar input");
        System.out.println("Will produce a compressed \"input.bwt\" file in the current directory. The uncompressed file will be saved");
        System.out.println();
        System.out.println("EG. java -jar bwt.jar input.bwt");
        System.out.println("Will produce an uncompressed \"input\" file in the current directory. The compressed file will be deleted");
        System.out.println();
        System.out.println("EG. java -jar bwt.jar input output");
        System.out.println("Will produce a compressed \"output.bwt\" file in the current directory. The uncompressed file will be saved");
        System.out.println();
        System.out.println("EG. java -jar bwt.jar input.bwt output");
        System.out.println("Will produce an uncompressed \"output\" file in the current directory. The compressed file will be deleted");
        System.out.println();
        System.out.println("Full paths are also supported. Pass it in standard Windows format");
        System.out.println();
        System.out.println("NOTE: the current version of the program overwrites the file without warning if a file with the same name already exists in the directory");
        System.out.print("Be careful");
    }

    private static void encodeOrDecode(Path filePath) throws IOException {
        String fileName = filePath.getFileName().toString();
        int dotIdx = fileName.lastIndexOf('.');

        if (dotIdx < 0) {
            Path encodedFilePath = filePath.resolveSibling(fileName + Constants.ENCODED_FILE_EXT);
            packFile(filePath, encodedFilePath);
        } else {
            String fileExtension = fileName.substring(dotIdx);

            if (fileExtension.equals(Constants.ENCODED_FILE_EXT)) {
                Path decodedFilePath = filePath.resolveSibling(fileName.substring(0, fileName.lastIndexOf('.')));
                unpackFile(filePath, decodedFilePath);
            } else {
                Path encodedFilePath = filePath.resolveSibling(fileName + Constants.ENCODED_FILE_EXT);
                packFile(filePath, encodedFilePath);
            }
        }
    }

    private static void encodeOrDecode(Path inFilePath, Path outFilePath) throws IOException {
        String inFileName = inFilePath.getFileName().toString();
        int dotIdx = inFileName.lastIndexOf('.');

        if (dotIdx < 0) {
            Path encodedFilePath = outFilePath.resolveSibling(outFilePath.getFileName() + Constants.ENCODED_FILE_EXT);
            packFile(inFilePath, encodedFilePath);
        } else {
            String fileExtension = inFileName.substring(dotIdx);

            if (fileExtension.equals(Constants.ENCODED_FILE_EXT)) {
                unpackFile(inFilePath, outFilePath);
            } else {
                Path encodedFilePath = outFilePath.resolveSibling(outFilePath.getFileName() + Constants.ENCODED_FILE_EXT);
                packFile(inFilePath, encodedFilePath);
            }
        }
    }

    private static void packFile(Path fileToEncode, Path encodedFile) throws IOException {
        System.out.println("Encoding " + fileToEncode);

        Instant start = Instant.now();

        byte[] file = Files.readAllBytes(fileToEncode);

        byte[] bwt = BWST.encode(file);

        int[] mtf = MTF.encode(bwt);

        int[] zrle = ZRLE.encode(mtf);

        int encodedFileSize = Compressor.compressAndWrite(encodedFile, zrle);

        Instant end = Instant.now();

        System.out.println("Successfully encoded!");
        System.out.println("Output file: " + encodedFile);
        System.out.println("Encoding time: " + Duration.between(start, end).toMillis() + " ms");
        System.out.println("Input size: " + file.length + " bytes");
        System.out.println("Output size: " + encodedFileSize + " bytes");
        System.out.print("Compression ratio: " + (float) encodedFileSize / file.length);
    }

    private static void unpackFile(Path fileToDecode, Path decodedFile) throws IOException {
        System.out.println("Decoding " + fileToDecode);

        Instant start = Instant.now();

        int[] decodeHuff = Decompressor.readAndDecompress(fileToDecode);

        int[] decodeZrle = ZRLE.decode(decodeHuff);

        byte[] mtfDecode = MTF.decode(decodeZrle);

        byte[] bwtDecode = BWST.decode(mtfDecode);

        Files.write(decodedFile, bwtDecode);

        Files.delete(fileToDecode);

        Instant end = Instant.now();

        System.out.println("Successfully decoded!");
        System.out.println("Output file: " + decodedFile);
        System.out.println("Decoding time: " + Duration.between(start, end).toMillis() + " ms");
        System.out.println("Input size: " + Decompressor.fileSize + " bytes");
        System.out.print("Output size: " + bwtDecode.length + " bytes");
    }
}
