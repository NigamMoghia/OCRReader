import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class Base64StringConversion {

    public static String fileToBase64StringConversion(File inputFile) throws IOException {
        byte[] fileContent = FileUtils.readFileToByteArray(inputFile);
        String encodedString = Base64
                .getEncoder()
                .encodeToString(fileContent);
        return encodedString;
    }
}
