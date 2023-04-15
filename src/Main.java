import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class Main {
    public static ArrayList <String> lines = new ArrayList<>();
    public static void main(String arg[]) throws Exception {

        HashMap<String,String > specialCharMap = createSpecialCharacterMassageList();
        File [] allGraphicFolders =getListOfFile(ReadPropertiesFile.getValue("GRAPHIC_FILE_PATH"));
        //System.out.println(allGraphicFolders.length + " : "+ allGraphicFolders[0].toString());

        for (int l=0; l<allGraphicFolders.length; l++) {
            lines.clear();
            String fileName = null;
            String grpahicIDFolder = allGraphicFolders[l].toString();
            String graphicID = allGraphicFolders[l].getName();
            System.out.println("Processing graphic : " + graphicID);
            File[] allFiles = getListOfFile(grpahicIDFolder);
            String graphicImageFileName = null;
            for (File f : allFiles){
                if (f.getName().contains(".gif")) {
                    graphicImageFileName = f.getName();
                    break;
                }
                else if (f.getName().contains(".jpg")){
                    graphicImageFileName = f.getName();
                    break;
                }
            }
            File file = new File(grpahicIDFolder+"\\"+graphicImageFileName);
            // File file = new File("C:\\Users\\rgupta\\Documents\\OCR\\95904\\CMLresponsemntrngalgo.gif");

            //GET THE BASE64 ENCODING FOR THE GRAPHIC .GIF FILE
            String s = Base64StringConversion.fileToBase64StringConversion(file);

            //CALL THE GOOGLE VISION API FOR THE GRAPHIC AND STORE THE REPONSE
            callAPI(s);

            //FROM THE RESPONSE GET THE COUNT OF BLOCKS IN THE RESPONSE
            ArrayList<ArrayList<Map<String, Object>>> blockList = GetResponseForWebService.response.jsonPath().get("responses.fullTextAnnotation.pages.blocks[0]");

            //NOW LOOP FOR EACH BLOCK
            for (int i = 0; i < blockList.get(0).size(); i++) {
                String responseValue = "";
                //GET THE NUMBER OF PARAGRAPH LIST IN EACH BLOCK
                List<String> paragraphList = GetResponseForWebService.response.jsonPath().getList("responses.fullTextAnnotation.pages.blocks[0][0][" + i + "].paragraphs.words");
                //NEXT AGAIN LOOP TO READ ALL PARAGRAPH IN EACH BLOCK
                for (int j = 0; j < paragraphList.size(); j++) {
                    //WITHIN EACH PARAGRAPH GET THE COUNT OF WORDS/SYMBOLS PRESENT, NOTE - IF THERE ARE 3 WORDS IN THAT LINE/PARAGRAPH THE SIZE OF
                    // SYMBOL LIST IS 3
                    List<String> symbolList = GetResponseForWebService.response.jsonPath().getList("responses.fullTextAnnotation.pages.blocks[0][0][" + i + "].paragraphs.words[" + j + "].symbols");
                    // NOW WITHIN EACH SYMBOL LIST THERE ARE LETTERS , SO LOOP FOR EACH WORD/SYMBOL LIST AND READ EACH LETTER INSIDE THE LOOP
                    for (int k = 0; k < symbolList.size(); k++) {
                        String val = null;
                        boolean found = false;
                        String character = GetResponseForWebService.response.path("responses.fullTextAnnotation.pages.blocks[0][0][" + i + "].paragraphs.words[" + j + "].symbols[" + k + "].text")
                                .toString()
                                .replace("[", "")
                                .replace("]", "")
                                .replace(" ", "");
                        if (character.equalsIgnoreCase(",")) {
                            // Found comma as the character , then do nothing
                        }
                        else
                            character=character.replace(",",""); // the api response have commas unnecessarily added which we need to get rid of
                        for (String  key : specialCharMap.keySet()){
                            if (character.equalsIgnoreCase(key)){
                                 val= specialCharMap.get(key);
                               found =true;
                            }
                        }

                        if (!found)
                             responseValue = responseValue + character + " "; // AFTER EVERY WORDS ADD A SPACE
                        else
                            responseValue = specialCharacterMassage(character,val,responseValue);
                    }

                }
                System.out.println(responseValue);
                lines.add(responseValue); //PUT THE LINES READ FROM API TO A FILE.
            }
            fileName =  grpahicIDFolder+"\\"+graphicID+"_OCRText.txt";
            new File(fileName).delete();
            createFile(fileName, lines);
            //createFile("C:\\Users\\rgupta\\Documents\\OCR\\95904\\95904_OCRtext.txt",lines);
        }
    }

    public static void callAPI(String base64) throws IOException {
        String method = "POST";
        String url = ReadPropertiesFile.getValue("API_URL");
        String parameter = ReadPropertiesFile.getValue("API_KEY");
        url = url + "?" + parameter;
        System.out.println(url);
        Map<String, String> headerMap = new HashMap<String, String>();
        String ContentType = "application/json";
        String body = "\n" +
                "{\n" +
                "  \"requests\":[\n" +
                "    {\n" +
                "      \"image\":{\n" +
                "        \"content\":\"" + base64 + "\"\n" +
                "      },\n" +
                "      \"features\":[\n" +
                "        {\n" +
                "          \"type\":\"DOCUMENT_TEXT_DETECTION\",\n" +
                "          \"maxResults\":100\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        GetResponseForWebService.run(method, ContentType, body, headerMap, url, 200);
    }

    public static void createFile(String filePath, ArrayList<String> lines){
        try {
            Files.write(Paths.get(filePath),
                    lines,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("ERROR While writing to file.");
            e.printStackTrace();
        }
    }
    public static File[] getListOfFile(String directory){
        File dir = new File(directory);
        File [] allFiles = new File[0];
        if (dir.isDirectory()){
            allFiles = dir.listFiles(path -> true);

        }
        return allFiles;
    }

    public static HashMap<String,String> createSpecialCharacterMassageList() throws IOException {
        String specialCharacter = ReadPropertiesFile.getValue("SPECIAL_CHARACTERS");
        String[] specialCharList = specialCharacter.split(";");
        HashMap<String,String> specialCharMap = new HashMap<>();
        for (String  s : specialCharList){
            String key = s.split("\\|")[0];
            //System.out.println(s);
            String value = s.split("\\|")[1];
            specialCharMap.put(key,value);
          //  System.out.println(key + " : "+value);
        }
        return  specialCharMap;
    }

    public static String specialCharacterMassage(String character, String operation, String str){
        operation = operation.toUpperCase();
      //  System.out.println(str + ":"+ character);


        StringBuilder temp = new StringBuilder(str);
        int lastChar = str.length()-1;
      //  System.out.println("lastChar is ="+lastChar);
        try{
        switch (operation){
            case "BEFORE":
                   if(temp.charAt(lastChar) ==' '){
                        str=temp.deleteCharAt(lastChar).toString();
                       // System.out.println("String is BEFORE "+str );
                    }
                    str=str +character+" ";
                   break;
            case "AFTER":
                    str=str +character;
                //System.out.println("String is AFTER "+str);
                    break;
            case "BOTH":
                 if(temp.charAt(lastChar) == ' ')
                    str=temp.deleteCharAt(lastChar).toString();
                 str=str +character;
               // System.out.println("String is BOTH"+str);
                 break;
            }
        }catch (Exception e){
            if (operation.equalsIgnoreCase("BEFORE"))
                str=str +character+" ";
            else
                str=str +character;
        }


                return str;
        }

}
