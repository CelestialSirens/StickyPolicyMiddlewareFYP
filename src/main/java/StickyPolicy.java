
import org.json.JSONObject;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class StickyPolicy {

        private final boolean allowRead;
        private final long expiryEpoch;

        private StickyPolicy(Builder builder) {
            this.allowRead = builder.allowRead;
            this.expiryEpoch = builder.expiryEpoch;
        }
        public boolean canRead() {return allowRead;}
        public boolean isExpired() {
            if (expiryEpoch == 0) return false;
            try {
                long now = TimeCheck.UTCTime();
                return now > expiryEpoch;
            } catch(Exception e){
                 System.out.println("[Phoebe]: Could not reach expiry time server, will not send file. Try again later ensure BOTH users are connected to the internet.");
                 return true;
            }
        }
        public JSONObject toJSON() {
            return new JSONObject()
                    .put("allowRead", allowRead)
                    .put("expiryEpoch", expiryEpoch);
        }

        public static StickyPolicy fromJSON(JSONObject json) {
            return new Builder()
            .allowRead(json.getBoolean("allowRead"))
            .expiryEpoch(json.getLong("expiryEpoch"))
            .build();
        }

        public static class Builder {
            private boolean allowRead =true;
            private long expiryEpoch =0;

            public Builder allowRead(boolean v){
                this.allowRead = v;
                return this;
            }
            public Builder expiryEpoch(long v){
                this.expiryEpoch =v;
                return this;
            }
            public Builder expiryFromInput(String input){
                if (input == null || input.trim().isEmpty()){
                    this.expiryEpoch = 0;
                    return this;
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("DD/MM/YYYY HH:mm");
                LocalDateTime dateTime = LocalDateTime.parse(input.trim(), formatter);
                this.expiryEpoch = dateTime.toEpochSecond(ZoneOffset.UTC);
                return this;
            }
            public StickyPolicy build() {
                return new StickyPolicy(this);
            }
            }
}

