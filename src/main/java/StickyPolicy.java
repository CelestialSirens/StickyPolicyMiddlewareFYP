
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.json.JSONObject;

public class StickyPolicy {

        private final boolean allowRead;
        private final long expiryEpoch;
        private final boolean allowDownload;

        private StickyPolicy(Builder builder) {
            this.allowRead = builder.allowRead;
            this.expiryEpoch = builder.expiryEpoch;
            this.allowDownload = builder.allowDownload;
        }
        public boolean canRead() {return allowRead;}
        public boolean canDownload() {return allowDownload;}
        public boolean isExpired() {
            System.out.println("[Testing]: Checking expiry, value is " + this.expiryEpoch);
            if (this.expiryEpoch == 0) {return false;}
            try {
                long now = TimeCheck.UTCTime();
                return now > this.expiryEpoch;
            } catch(Exception e){
                 System.out.println("[Phoebe]: Could not reach expiry time server, will not send file. Try again later ensure BOTH users are connected to the internet.");
                 return true;
            }
        }
        public JSONObject toJSON() {
            return new JSONObject()
                    .put("allowRead", allowRead)
                    .put("expiryEpoch", expiryEpoch)
                    .put("allowDownload",allowDownload);
        }

        public static StickyPolicy fromJSON(JSONObject json) {
            return new Builder()
            .allowRead(json.optBoolean("allowRead",true))
            .expiryEpoch(json.optLong("expiryEpoch", 0))
            .allowDownload(json.optBoolean("allowDownload",true))
            .build();
        }

        public static class Builder {
            private boolean allowRead = true;
            private long expiryEpoch = 0;
            private boolean allowDownload = true;
            private boolean parseFailed = false;

            public Builder allowRead(boolean v){
                this.allowRead = v;
                return this;
            }
            public Builder expiryEpoch(long v){
                this.expiryEpoch =v;
                return this;
            }
            public Builder allowDownload(boolean v){
                this.allowDownload =v;
                return this;
            }
            public boolean hasFailed(){ return parseFailed;}
            public Builder expiryFromInput(String input){
                if (input == null || input.trim().isEmpty()){
                    this.expiryEpoch = 0;
                    return this;
                }
                try{
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                         LocalDateTime dateTime = LocalDateTime.parse(input.trim(), formatter);
                         this.expiryEpoch = dateTime.toInstant(ZoneOffset.UTC).getEpochSecond();
                         long currentUTC = TimeCheck.UTCTime();
                         if (this.expiryEpoch <= currentUTC ){
                            System.out.println("[Phoebe]: Invalid time given.");
                            this.parseFailed = true;
                         }
                } catch (Exception e) {
                    this.parseFailed = true;
                }
                return this;
            }

            public StickyPolicy build() {
                return new StickyPolicy(this);
            }
            }
   // Water mark & time stuff
public class TimeCheck {
    public static long UTCTime() throws Exception{
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://timeapi.io/api/time/current/zone?timeZone=UTC"))
        .GET()
        .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        
        String dateTimeString = json.getString("dateTime");
        return java.time.LocalDateTime.parse(dateTimeString).toInstant(java.time.ZoneOffset.UTC).getEpochSecond();

    }
}



public class Watermark {

    public static String generateWatermark(String sender, String receiever, long timestamp){
        try{
            String input = sender + receiever + timestamp;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02XX", b));
            }
            return hex.substring(0,8);
        } catch (Exception e){
            return "000000000";
        }
    }
}
}


