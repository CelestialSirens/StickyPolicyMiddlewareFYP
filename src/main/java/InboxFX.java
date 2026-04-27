import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.json.JSONObject;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;






public class InboxFX {
    public static String showImage(String sender, String reciever, String base64Data, String fileName, StickyPolicy policy){
  
        try {
        //            \\ __^^^^__ // 
    Platform.startup    (() -> {});     // If you see this . Hi :) hes funny looking right      .    Since this is the only visual class 
                      //{  ___  }\\                                                                 its only right to have ASCII art .                                                                
        } catch (IllegalStateException e){
        }
        String watermarkText = "Phoebe~/" + StickyPolicy.Watermark.generateWatermark(sender, reciever, Instant.now().getEpochSecond());
        Platform.runLater(() -> {

            try {
                byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                
                if (image.isError()) {
                    showError("Could not load image: " + fileName);
                    return;
                }
                
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
                imageStack.setStyle("-fx-background-color: black;");

                Label infoLabel = buildInfoBar("From: " + sender + " | "+ fileName + " | " + watermarkText);
                
                BorderPane root = new BorderPane();
                root.setCenter(imageStack);
                root.setBottom(infoLabel);
                root.setStyle("-fx-background-color: cyan;");

                Stage stage = new Stage();
                stage.setTitle("Image from " + sender + " -- " + fileName); // + sent at + expires at     < -- needs adding still
                stage.setScene(new Scene(root));
                stage.setResizable(true);
                stage.show();

                Timeline expiryChecker = new Timeline(
                    new KeyFrame(Duration.seconds(1), e -> {
                        if (policy.isExpired()) {
                            Platform.runLater(stage::close);
                        }
                    })
                );
                expiryChecker.setCycleCount(Timeline.INDEFINITE);
                expiryChecker.play();
                stage.setOnHidden(e -> expiryChecker.stop());

                
            } catch (Exception e) { 
                showError("Failed to open image: " + e.getMessage());
            }
        });
       return watermarkText;
    }    

    public static void showFile(String sender, String reciever, String base64Data, String fileName, StickyPolicy policy){
            try{
                Platform.startup(() -> {});
            } catch(IllegalStateException e){
            } Platform.runLater(() -> {
                try{
                    byte[] fileBytes = Base64.getDecoder().decode(base64Data);
                    String watermarkText = "Phoebe" + StickyPolicy.Watermark.generateWatermark(sender, reciever, Instant.now().getEpochSecond());
                    String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')+ 1).toLowerCase() : "";
                    switch (ext) {
                        case "pdf" -> showPDF(sender, fileName, fileBytes, watermarkText, policy);
                        default -> showError("Unsupported File Type:" + ext);
                    }
                }
                catch(Exception e){  showError("Failed to open File:" + e.getMessage());
                }
            });
    }
    public static void showPDF(String sender, String fileName, byte[] fileBytes, String watermarkText, StickyPolicy policy){
            try{
                int[] currentPage = {0};
                PDDocument document = Loader.loadPDF(fileBytes);
                PDFRenderer renderer = new PDFRenderer(document);
                int pageCount = document.getNumberOfPages();
                List<javafx.scene.image.Image> pages = new ArrayList<>();
                for (int i = 0; i < pageCount; i++){
                    BufferedImage buffered = renderer.renderImageWithDPI(i,150);
                    pages.add(bufferedToFX(buffered));
                }
                document.close();

                ImageView pageView = new ImageView(pages.get(0));
                pageView.setPreserveRatio(true);
                pageView.setFitWidth(800);

                Canvas watermarkCanvas = new Canvas(800, pageView.getFitHeight() > 0 ? pageView.getFitHeight() : 1000);
                drawWatermark(watermarkCanvas, watermarkText);

                StackPane pageStack = new StackPane(pageView, watermarkCanvas);
                pageStack.setStyle("-fx-background-color: black;");

                ScrollPane scrollPane = new ScrollPane(pageStack);
                scrollPane.setFitToWidth(true);
                scrollPane.setStyle("-fx-background-color: red;");

                Button prButton = new Button("< Prev");
                Button nxButton = new Button("Next >");
                Label pageLabel = new Label("Page 1 of " + pageCount);
                pageLabel.setTextFill(Color.ALICEBLUE);

                prButton.setDisable(true);
                if (pageCount == 1) nxButton.setDisable(true); 

                prButton.setOnAction(e -> {
                    pageView.setImage(pages.get(currentPage[0]));
                    drawWatermark(watermarkCanvas, watermarkText);
                    pageLabel.setText("Page " + (currentPage[0] + 1) + " of " + pageCount);
                    prButton.setDisable(currentPage[0] == 0);
                    nxButton.setDisable(false);    
                });
                nxButton.setOnAction(e -> {
                    currentPage[0]++;
                    pageView.setImage(pages.get(currentPage[0]));
                    drawWatermark(watermarkCanvas,watermarkText);
                    pageLabel.setText("Page " + (currentPage[0] + 1) + " of " + pageCount);
                    nxButton.setDisable(currentPage[0] == pageCount - 1);
                    prButton.setDisable(false);
                });

                HBox navBar = new HBox(10, prButton, pageLabel, nxButton);
                navBar.setAlignment(Pos.CENTER);
                navBar.setPadding(new Insets(6));
                navBar.setStyle("-fx-background-color: blue;");

                Label infoLabel = buildInfoBar("From: " + sender + "  /  " + fileName + " / " + watermarkText);
                BorderPane root = new BorderPane();
                root.setCenter(scrollPane);
                root.setTop(navBar);
                root.setBottom(infoLabel);

                Stage stage = new Stage();
                stage.setTitle("PDF from " + sender + "--" + fileName);
                stage.setScene(new Scene(root, 860,800));
                stage.show();

                Timeline expiryChecker = new Timeline(
                    new KeyFrame(Duration.seconds(1), e -> {
                        if (policy.isExpired()) {
                            Platform.runLater(stage::close);
                        }
                    })
                );
                expiryChecker.setCycleCount(Timeline.INDEFINITE);
                expiryChecker.play();
                stage.setOnHidden(e -> expiryChecker.stop());

            } catch (Exception e) { showError("Failed to render PDF: " + e.getMessage()); }
    }
    public static javafx.scene.image.Image bufferedToFX(BufferedImage buffered) {
        return SwingFXUtils.toFXImage(buffered, null);
    }

    private static void drawWatermark(Canvas canvas, String watermarkText) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.save();
        gc.translate(canvas.getWidth() / 2 , canvas.getHeight() / 2);
        gc.rotate(-30);
        gc.translate(-canvas.getWidth() / 2, - canvas.getHeight() / 2);
        gc.setFont(Font.font("Serif", FontWeight.BOLD, 36));
        gc.setFill(Color.color(1, 1, 1,0.25));
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

    public static String showMessage(String sender, String reciever, String content, StickyPolicy policy){
        String watermarkText = "Phoebe~/" + StickyPolicy.Watermark.generateWatermark(sender, reciever, Instant.now().getEpochSecond());
        
        Platform.runLater(() -> {
            try {
                Label messageLabel = new Label(content);
                messageLabel.setWrapText(true);
                messageLabel.setPadding(new Insets(20));
                messageLabel.setFont(Font.font("Serif",14));
                messageLabel.setTextFill(Color.WHITE);
                messageLabel.setStyle("-fx-background-color: black;");
                messageLabel.setMaxWidth(600);

                Label infoLabel = buildInfoBar("From: " + sender + " # " + watermarkText);
                BorderPane root = new BorderPane();
                root.setCenter(messageLabel);
                root.setBottom(infoLabel);
                root.setStyle("-fx-background-color: black;");

                Stage stage = new Stage();
                stage.setTitle("Message from " + sender);
                stage.setScene(new Scene(root, 640, 200));
                stage.show();

                Timeline expiryChecker = new Timeline(
                    new KeyFrame(Duration.seconds(1), e ->{
                        if (policy.isExpired()){
                            Platform.runLater(stage::close);
                        }
                    })
                );
                expiryChecker.setCycleCount(Timeline.INDEFINITE);
                expiryChecker.play();
                stage.setOnHidden(e -> expiryChecker.stop());

             } catch (Exception e) {
                showError("Failed to open message: " + e.getMessage());
             } 
    });
                return watermarkText;
            }

    public static class FilePicker {

        public static File pickImage() {
            return pick("Select Image", new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        }
        public static File pickFile(){
            return pick("Select File", new FileChooser.ExtensionFilter("Files", "*.pdf", "*.txt"));
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

public static String downloadImage(String sender, String reciever, String base64Data, String fileName) {
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
            return watermark;
        } catch (Exception e) {
            System.out.println("[Phoebe]: Download failed: " + e.getMessage());
            return null;
        }
}

public static String downloadFile(String sender, String reciever, String base64Data, String fileName){
    try {
        byte[] fileBytes = Base64.getDecoder().decode(base64Data);
        String downloadsPath = System.getProperty("user.home" + File.separator + "Downloads");
        long timestamp = Instant.now().getEpochSecond();
        String watermark = "Phoebe~/" + StickyPolicy.Watermark.generateWatermark(sender, reciever, timestamp);
        File outputFile = new File(downloadsPath + File.separator + fileName);
        java.nio.file.Files.write(outputFile.toPath(), fileBytes);
    System.out.println("[Phoebe]: Image Saved to " + outputFile.getAbsolutePath());
            System.out.println("[Phoebe]: Watermark: " + watermark);
            return watermark;
        } catch (Exception e) {
            System.out.println("[Phoebe]: Download failed: " + e.getMessage());
            return null;
        }
}

}

    
