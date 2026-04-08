import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONObject;


public class TimeCheck {
    public static long UTCTime() throws Exception{
        URL url = new URL("https://worldtimeapi.org/api/timezone/UTC");
        HttpURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder respond = new StringBuilder();
        String input; 
        while ((input = reader.readLine()) != null) {
            respond.append(input);
        }
        reader.close();
        JSONObject json = new JSONObject(respond.toString());
        return json.getLong("unixtime");
    }

}
