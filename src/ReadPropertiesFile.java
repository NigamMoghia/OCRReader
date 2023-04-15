import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ReadPropertiesFile {

    private static FileInputStream stream;
    private static Properties propertyFile = new Properties();


    public static String getValue(String key) throws IOException {
        File file = new File(System.getProperty("user.dir")+"\\Config.properties");
        String value ="";
        stream = new FileInputStream(file.getAbsolutePath());
        propertyFile.load(stream);
        String locatorProperty = propertyFile.getProperty(key);
        // System.out.println(locatorProperty.toString());
        value = locatorProperty.split(" = ")[0];
        return value;
    }
}
