
import java.util.Base64;
import java.util.HashSet;
import java.io.File;
import java.io.IOException;
//import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;


public class FileConversions {
    // move all the img - b64 stuff into here 
    // and the b64 - img 
    // Only allowing PNG, JPG & JPEG atm                *** NO VIDEO STUFF ***

    // << -- File checking -- >>  
    private static final HashSet<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>(
        Arrays.asList("png","jpg","jpeg")
    );
    // Only allowing PDF, TXT atm.   23/02/26
    private static final HashSet<String> ALLOWED_FILE_EXTENSIONS = new HashSet<>(
        Arrays.asList("pdf","txt")
    );
    
    public static String getExtension(String filePath){
        String fileName = Paths.get(filePath).getFileName().toString();
        int dotIndex = fileName.lastIndexOf(".");     // <--- reads only after the . in file
        if (dotIndex == -1) return "";
        return fileName.substring(dotIndex + 1).toLowerCase();
    }

    public static String getExtensionType(String filePath) throws IOException{
        String extType = getExtension(filePath);
        if (ALLOWED_IMAGE_EXTENSIONS.contains(extType)) return "Image";
        if (ALLOWED_FILE_EXTENSIONS.contains(extType)) return "File";        
        throw new IOException("File type not premitted:" + extType);
    }

    // << -- Image conversion code -- >>

    public static String imageToB64 (String filePath) throws IOException{
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static void B64ToImage (String sender, String base64, String fileName) throws IOException{
        String dir = "Phoebe_recieved/" + sender;
        new File(dir).mkdirs();
        byte[] bytes = Base64.getDecoder().decode(base64);
        Files.write(Paths.get(dir + "/" + fileName),bytes);
        System.out.println("[FileConverter]: Image saved to " + dir + "/" + fileName);
    }

    // << -- File conversion code -->>

    public static String fileToB64 (String filePath) throws IOException{
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static void B64ToFile (String sender, String base64, String fileName) throws IOException{
        String dir = "Phoebe_recieved/" + sender;
        new File(dir).mkdirs();
        byte[] bytes = Base64.getDecoder().decode(base64);
        Files.write(Paths.get(dir + "/" + fileName), bytes);
        System.out.println("[FileConverter]: File saved to" + dir + "/" + fileName);
    }
}