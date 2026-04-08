import org.json.JSONObject;
import org.json.JSONArray;
import java.util.HashMap;
import java.util.Map;

    // file to contain all the DHT routing, got too messy in main
public class DHT {

    private final HashMap<String, PeerInfo> table = new HashMap<>();
    private long Time() throws Exception{
        return TimeCheck.UTCTime();
    }

    public void register(String username, String ip, int port) throws Exception{
        if (table.containsKey(username)){
            PeerInfo exists = table.get(username);
            if (exists.ip.equals(ip)){
                table.put(username, new PeerInfo(ip,port, TimeCheck.UTCTime()));
                System.out.println("[Phoebe]: Updated entry for " + username);
            } else {
                throw new Exception("[Phoebe]: Username " + username + "is already taken");
            }   
        } else {
            table.put(username, new PeerInfo(ip,port, TimeCheck.UTCTime()));
            System.out.println("[Phoebe]: Registed " + username);
        }
    }
    public PeerInfo lookup(String username) throws Exception{
        if (!table.containsKey(username)){
            throw new Exception("[Phoebe]: No username found" + username);
        }
        return table.get(username);
    }









    public static class PeerInfo{
        public final String ip;
        public final int port;
        public final long timestamp;

        public PeerInfo(String ip, int port, long timestamp){
            this.ip = ip;
            this.port = port;
            this.timestamp = timestamp;
        }
    }
}
