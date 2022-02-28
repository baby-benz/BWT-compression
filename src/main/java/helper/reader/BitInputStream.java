package helper.reader;

import java.io.IOException;
import java.io.InputStream;

public final class BitInputStream implements AutoCloseable {
    private final InputStream input;
    private int bytesRead = 0;
    private int nextBits;
    private int numBitsRemaining;
    private boolean isEndOfStream;

    public BitInputStream(InputStream in) {
        if (in == null) {
            throw new NullPointerException("Argument is null");
        }
        input = in;
        nextBits = 0;
        numBitsRemaining = 0;
        isEndOfStream = false;
    }

    public int read() throws IOException {
        if (isEndOfStream) {
            return -1;
        }
        if (numBitsRemaining == 0) {
            nextBits = input.read();
            if (nextBits == -1) {
                isEndOfStream = true;
                return -1;
            }
            numBitsRemaining = 8;
            bytesRead++;
        }
        numBitsRemaining--;
        return (nextBits >>> numBitsRemaining) & 1;
    }

    public int readNBits(int bitsToRead) throws IOException {
        if (isEndOfStream) {
            return -1;
        }
        int endBitsToLeft = (int) Math.pow(2, bitsToRead) - 1;
        if (numBitsRemaining == 0 || bitsToRead > numBitsRemaining) {
            nextBits = (nextBits & endBitsToLeft) << 8 | input.read();
            if (nextBits == -1) {
                isEndOfStream = true;
                return -1;
            }
            numBitsRemaining += 8;
            bytesRead++;
        }
        numBitsRemaining = numBitsRemaining - bitsToRead;
        return (nextBits >>> numBitsRemaining) & endBitsToLeft;
    }

    @Override
    public void close() throws IOException {
        input.close();
    }

    public int getReadFileSize() {
        return bytesRead;
    }
}
