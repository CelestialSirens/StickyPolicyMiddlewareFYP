
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Base64;
import javafx.application.Platform;
import javafx.scene.image.Image;



public class InboxFX {
    public static void showImage(String sender, String reciever, String base64Data, String fileName){
       //             \\ __^^^^__ // 
    Platform.startup    (() -> {});     // If you see this . Hi :) hes funny looking right      .    Since this is the only visual class 
                      //{  ___  }\\                                                                  its only right to have ASCII art right. 
        Platform.runLater(() -> {

            try {
                byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                
                if (image.isError()) {
                    showError("Could not load image: " + fileName);
                }
                String watermarkText = "Phoebe~/" + StickyPolicy.Watermark.generateWatermark(sender, reciever, Instant.now().getEpochSecond());

            } catch (Exception e) {
            }
        }
       
    }    

    private static void showError(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
