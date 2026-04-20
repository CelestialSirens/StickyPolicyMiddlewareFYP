import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import org.json.JSONObject;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;



public class InboxFX {
    public static void showImage(String sender, String reciever, String base64Data, String fileName){
  
        try {
        //            \\ __^^^^__ // 
    Platform.startup    (() -> {});     // If you see this . Hi :) hes funny looking right      .    Since this is the only visual class 
                      //{  ___  }\\                                                                 its only right to have ASCII art .                                                                
        } catch (IllegalStateException e){
        }
        Platform.runLater(() -> {

            try {
                byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                
                if (image.isError()) {
                    showError("Could not load image: " + fileName);
                    return;
                }
                String watermarkText = "Phoebe~/" + StickyPolicy.Watermark.generateWatermark(sender, reciever, Instant.now().getEpochSecond());
                ImageView imageView = new ImageView(image);
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(Math.min(image.getWidth(), 800));
                imageView.setFitHeight(Math.min(image.getHeight(), 600));
                
                Canvas watermarkCanvas = new Canvas(
                    imageView.getFitWidth() > 0 ? imageView.getFitWidth() : image.getWidth(),
                    imageView.getFitHeight() > 0 ? imageView.getFitHeight() : image.getWidth()  
                );
                drawWatermark(watermarkCanvas, watermarkText);
                StackPane imageStack = new StackPane(imageView, watermarkCanvas);
                imageStack.setStyle("-fx-background-color: pink;");

                Label infoLabel = buildInfoBar("From: " + sender + " | "+ fileName + " | " + watermarkText);
                
                BorderPane root = new BorderPane();
                root.setCenter(imageStack);
                root.setBottom(infoLabel);
                root.setStyle("-fx-background-color: cyan;");

                Stage stage = new Stage();
                stage.setTitle("Image from " + sender + " -- " + fileName); // + sent at + expires at     < -- needs adding still
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.show();
            } catch (Exception e) { showError("Failed to open image: " + e.getMessage());
            }
        });
       
    }    
    private static void drawWatermark(Canvas canvas, String watermarkText) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.save();
        gc.translate(canvas.getWidth() / 2 , canvas.getHeight() / 2);
        gc.rotate(-30);
        gc.translate(-canvas.getWidth() / 2, - canvas.getHeight() / 2);
        gc.setFont(Font.font("Serif", FontWeight.BOLD, 36));
        gc.setFill(Color.color(1, 1, 1,0.15));
        double textWidth = watermarkText.length() * 22;
        double textHeight = 36;
        for (double y = -canvas.getHeight(); y < canvas.getHeight() * 2; y += textHeight + 60){
            for (double x = -canvas.getHeight(); x < canvas.getWidth() *2; x += textWidth + 40) {
                gc.fillText(watermarkText, x,y);
            }
        }
        gc.restore();
       
    }

    private static Label buildInfoBar(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.WHEAT);
        label.setFont(Font.font("Serif", 11));
        label.setPadding(new Insets(4,8,4,8));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-background-color: black;");
        return label;
    }

    private static void showError(String message) {
       Platform.runLater(() -> {
            Stage stage = new Stage();
            Label label = new Label(message);
            label.setPadding(new Insets(20));
            stage.setScene(new Scene(new StackPane(label), 400, 100));
            stage.setTitle("Phoebe Error");
            stage.show();

       }); 
    }
    public static class FilePicker {

        public static File pickImage() {
            return pick("Select Image", new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        }
        
        private static File pick(String title, FileChooser.ExtensionFilter ...filters){
            CompletableFuture<File> future = new CompletableFuture<>();
            try {
                Platform.startup(() -> {});
            } catch (Exception e) {
            }
            Platform.runLater(() -> {
                try {
                    FileChooser chooser = new FileChooser();
                    chooser.setTitle(title);
                    chooser.getExtensionFilters().addAll(filters);
                    chooser.setInitialDirectory(new File(System.getProperty("user.home")));
                    File chosen = chooser.showOpenDialog(new Stage());
                    future.complete(chosen);
                } catch (Exception e) {
                    future.complete(null);
                }
        });
        try {
            return future.get();
        } catch (Exception e) {
            return null;
        }
    }   
    
}

public static void downloadImage(String sender, String reciever, String base64Data, String fileName) {
    try {
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);
        String downloadsPath = System.getProperty("user.home") + File.separator + "Downloads";
        File outputFile = new File(downloadsPath + File.separator + fileName);
        java.nio.file.Files.write(outputFile.toPath(), imageBytes);

        long timestamp = Instant.now().getEpochSecond();
        String watermark = "Phoebe~/" + StickyPolicy.Watermark.generateWatermark(sender, reciever, timestamp);

        JSONObject metadata = new JSONObject()
            .put("sender", sender)
            .put("reciever", reciever)
            .put("timestamp", timestamp)
            .put("originalFileName", fileName)
            .put("watermark", watermark);

        File metaFile = new File(downloadsPath + File.separator + fileName + ".phoebe");
        java.nio.file.Files.writeString(metaFile.toPath(), metadata.toString(4));

            System.out.println("[Phoebe]: Image Saved to " + outputFile.getAbsolutePath());
            System.out.println("[Phoebe]: Metadata Saved to " + metaFile.getAbsolutePath());
            System.out.println("[Phoebe]: Watermark: " + watermark);
        } catch (Exception e) {
            System.out.println("[Phoebe]: Download failed: " + e.getMessage());
        }
}
}

    
