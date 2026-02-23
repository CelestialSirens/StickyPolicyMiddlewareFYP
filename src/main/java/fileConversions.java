
import java.util.Base64;
import java.util.HashSet;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;


public class fileConversions {
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
        
    }



}