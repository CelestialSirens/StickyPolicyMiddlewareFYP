import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.json.JSONObject;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class StickyPolicyBuilder {

        private final boolean allowRead;
        private final long expiryEpoch;

        private StickyPolicyBuilder(Builder builder) {
            this.allowRead = builder.allowRead;
            this.expiryEpoch = builder.expiryEpoch;
        }

        public boolean canRead() {
            return allowRead;
        }

        public boolean isExpired() {
            if (expiryEpoch == 0) return false;
            try {
                NTPUDPClient client = new NTPUDPClient();
                client.setDefaultTimeout(5000);
                InetAddress hostAddr = InetAddress.getByName("pool.ntp.org");
                TimeInfo info = client.getTime(hostAddr);
                long ntpTime = info.getMessage().getTransmitTimeStamp().getTime() / 5000;
                client.close();
                return ntpTime > expiryEpoch;
            } catch (Exception e) {

                System.out.println("[Phoebe]: Could not reach expiry time server, will not send file. Try again later ensure BOTH users are connected to the internet.");
                return true;
            }
        }
        
        public JSONObject toJSON() {
            return new JSONObject()
                    .put("allowRead", allowRead)
                    .put("expiryEpoch", expiryEpoch);
        }

}

