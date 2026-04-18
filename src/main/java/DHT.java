import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

    // file to contain all the DHT routing, got too messy in main
public class DHT {

    private final HashMap<String, PeerInfo> table = new HashMap<>();
    private long Time() throws Exception{
        return StickyPolicy.TimeCheck.UTCTime();
    }
    private String lastRegisteredName;
    public void register(String username, String ip, int port) throws Exception{
        if (table.containsKey(username)){
            PeerInfo exists = table.get(username);
            if (exists.ip.equals(ip) && exists.port == port){
                table.put(username, new PeerInfo(ip,port, StickyPolicy.TimeCheck.UTCTime()));
                System.out.println("[Phoebe]: Updated entry for " + username);   
                lastRegisteredName = username; 
                return;
            }   
        }
            String finalName = username;
            if (table.containsKey(username)){
                int counter = 1;
                while (table.containsKey(username + "#" + counter)){  // Idea! Instead of the counter, use the timestamp formatter to give a Name - '' < time of when it was added first. :3
                counter++;
                }
            finalName = username + "#" + counter;
            System.out.println("[Phoebe]: Username taken, registed as" + finalName);         
            }
            table.put(finalName, new PeerInfo(ip, port, StickyPolicy.TimeCheck.UTCTime()));
            lastRegisteredName = finalName;
            System.out.println("[Phoebe]: Registered, " + finalName);
        }
        public String getLastRegisteredName(){
            return lastRegisteredName;
        }

    public PeerInfo lookup(String username) throws Exception{
        if (!table.containsKey(username)){
            throw new Exception("[Phoebe]: No username found " + username);
        }
        return table.get(username);
    }

    public void remove (String username){
        table.remove(username);
        System.out.println("[Phoebe]: Removed " + username);
    }

    public void merge (DHT other){
        for (Map.Entry<String, PeerInfo> en : other.table.entrySet()) {
            String username = en.getKey();
            PeerInfo incoming = en.getValue();
            PeerInfo current = table.get(username);
            if (current == null || incoming.timestamp > current.timestamp) {
                table.put(username, incoming);
            }
        }
    }

    public JSONObject toJson(){
        JSONObject json = new JSONObject();
        for (Map.Entry<String, PeerInfo> en : table.entrySet()) {
            json.put(en.getKey(), en.getValue().toJson());
        }
        return json;
    }    
    public static DHT fromJson(JSONObject json) throws Exception{
        DHT dht = new DHT();
        for(String username : json.keySet()){
            JSONObject peerJson = json.getJSONObject(username);
            dht.table.put(username, PeerInfo.fromJSON(peerJson));
        }
        return dht;
    }

    public HashMap<String, PeerInfo> getTable(){
        return table;
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
        private JSONObject toJson() {
           return new JSONObject()
           .put("IP", ip)
           .put("Port", port)
           .put("Timestamp", timestamp);
        }
        private static PeerInfo fromJSON(JSONObject json) {
            return new PeerInfo(
                json.getString("IP"),
                json.getInt("Port"),
                json.getLong("Timestamp")
            );
        }
    }
}
