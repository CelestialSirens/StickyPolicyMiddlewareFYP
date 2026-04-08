import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;


public class TimeCheck {
    public static long UTCTime() throws Exception{
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://timeapi.io/api/time/current/zone?timeZone=UTC"))
        .GET()
        .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        
        String dateTime = json.getString("dateTime");
        String trimmed = dateTime.substring(0,19);
        LocalDateTime LDT = LocalDateTime.parse(trimmed);
        return LDT.toEpochSecond(ZoneOffset.UTC);

    }

}
