package helper;

import helper.reader.PropertyFileReader;

public final class Constants {
    public final static int ALPHABET_SIZE = PropertyFileReader.readIntProp("utf-8.alphabet-size");
    public final static String ENCODED_FILE_EXT = PropertyFileReader.readStrProp("coder.file-ext");
}
