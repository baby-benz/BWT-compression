package helper.reader;

import java.io.*;
import java.util.Properties;

public class PropertyFileReader {
    public static String readStrProp(String name) {
        return readProp(name);
    }

    public static int readIntProp(String name) {
        return Integer.parseInt(readProp(name));
    }

    private static String readProp(String name) {
        InputStream in = PropertyFileReader.class.getResourceAsStream("/application.properties");

        Properties appProps = new Properties();

        try {
            appProps.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return appProps.getProperty(name);
    }
}
