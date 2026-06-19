import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.time.Duration;

public class ApiDemo {
    private static final String API_URL = "http://localhost:5000/api";
    private static String jwtToken = "";

    public static void main(String[] args) {
        System.out.println("===================================================");
        System.out.println("     SECUREBANK - Java API Integration Demo");
        System.out.println("===================================================\n");

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // 1. Authenticate with REST API
            System.out.println("[1] Authenticating investigator credentials...");
            String loginJson = "{\"username\":\"analyst_sarah\",\"password\":\"password123\"}";

            HttpRequest loginRequest = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(loginJson))
                    .build();

            HttpResponse<String> loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());

            if (loginResponse.statusCode() == 200) {
                String body = loginResponse.body();
                // Simple parsing to extract the token
                int tokenIndex = body.indexOf("\"token\":\"");
                if (tokenIndex != -1) {
                    int start = tokenIndex + 9;
                    int end = body.indexOf("\"", start);
                    jwtToken = body.substring(start, end);
                    System.out.println("-> Success! Authenticated and received JWT token.\n");
                }
            } else {
                System.out.println("-> Error authenticating: " + loginResponse.statusCode() + " " + loginResponse.body());
                return;
            }

            // 2. Fetch the Active Alert Queue
            if (!jwtToken.isEmpty()) {
                System.out.println("[2] Fetching active flagged transaction queue...");
                HttpRequest alertsRequest = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL + "/alerts"))
                        .header("Authorization", "Bearer " + jwtToken)
                        .header("Content-Type", "application/json")
                        .GET()
                        .build();

                HttpResponse<String> alertsResponse = client.send(alertsRequest, HttpResponse.BodyHandlers.ofString());

                if (alertsResponse.statusCode() == 200) {
                    System.out.println("-> Success! Alerts payload received:");
                    // Print raw JSON response from backend
                    System.out.println(alertsResponse.body());
                } else {
                    System.out.println("-> Error fetching alerts: " + alertsResponse.statusCode());
                }
            }

        } catch (Exception e) {
            System.err.println("-> Exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
