package helper.writer;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.OutputStream;

public final class BitOutputStream implements AutoCloseable {
    private final OutputStream output;
    private final List<Integer> fileToWrite = new ArrayList<>();
    private int currentByte;
    private int numBitsInCurrentByte;

    public BitOutputStream(OutputStream out) {
        if (out == null) {
            throw new NullPointerException("Argument is null");
        }
        output = out;
        currentByte = 0;
        numBitsInCurrentByte = 0;
    }

    public void write(int b) throws IOException {
        if (!(b == 0 || b == 1)) {
            throw new IllegalArgumentException("Argument must be 0 or 1");
        }
        currentByte = currentByte << 1 | b;
        numBitsInCurrentByte++;
        if (numBitsInCurrentByte == 8) {
            fileToWrite.add(currentByte);
            numBitsInCurrentByte = 0;
        }
    }

    private void insertUnusedBitsNum() {
        int bitsToSave = fileToWrite.get(0) & 0x1F; // вырезаем последние 5 бит с полезной информацией из первого байта
        fileToWrite.set(0, ((8 - numBitsInCurrentByte) << 5) | bitsToSave); // склеиваем количество неиспользуемых бит
                                                                            // и вырезанные биты с помощью побитового
                                                                            // ИЛИ и заменяем получившимся байтом первый
                                                                            // байт файла
    }

    private void writeToFile() throws IOException {
        for (Integer integer : fileToWrite) {
            output.write(integer);
        }
    }

    @Override
    public void close() throws IOException {
        insertUnusedBitsNum();
        while (numBitsInCurrentByte != 0) {
            write(0);
        }
        writeToFile();
        output.close();
    }

    public int getWrittenFileSize() {
        return fileToWrite.size();
    }
}
