package utility;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;


/**
 * communicates with backend API and handles requests to server
 *
 * @author Maxwell Howard
 */
public class APIService {
    private static final String API_URL = "https://bitewise-api.onrender.com/api/users";
    private final HttpClient client;

    public APIService() {
        this.client = HttpClient.newHttpClient();
    }

    /**
     * adds new user with provided email and password
     * @param email user email address
     * @param password user password
     * @return user ID
     * @throws Exception if email is used by another user, or if there are issues with the server
     */
    public int signUp(String email, String password) throws Exception {
        JSONObject json = new JSONObject();
        json.put("email", email);
        json.put("password", password);

        // create and send request
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json.toString())).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // check response
        if (response.statusCode() == 201) {
            JSONObject obj = new JSONObject(response.body());
            return obj.getJSONObject("user").getInt("id");
        }
        else if (response.statusCode() == 409) {
            throw new Exception("User already exists");
        }
        else {
            throw new Exception("Sign up failed");
        }
    }

    /**
     * verifies login with provided email and password
     * @param email user email address
     * @param password user password
     * @return user ID
     * @throws Exception if email or password is invalid, or there are issues with the server
     */
    public int signin(String email, String password) throws Exception {
        JSONObject json = new JSONObject();
        json.put("email", email);
        json.put("password", password);

        // create and send request
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://bitewise-api.onrender.com/api/login")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json.toString())).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // check response
        if (response.statusCode() == 200) {
            JSONObject obj = new JSONObject(response.body());
            return obj.getJSONObject("user").getInt("id");
        }
        else if (response.statusCode() == 401) {
            throw new Exception("Invalid email or password");
        }
        else {
            throw new Exception("Sign in failed");
        }
    }

    /**
     * updates password with provided email and new password
     * @param email user email address
     * @param newPassword new user password
     * @throws Exception if email is invalid, or there are issues with the server
     */
    public void changePassword(String email, String newPassword) throws Exception {
        JSONObject json = new JSONObject();
        json.put("email", email);
        json.put("newPassword", newPassword);

        // create and send request
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://bitewise-api.onrender.com/api/change-password")).header("Content-Type", "application/json").PUT(HttpRequest.BodyPublishers.ofString(json.toString())).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // check response
        if (response.statusCode() == 200) {
            return;
        }
        else if (response.statusCode() == 404) {
            throw new Exception("Invalid email");
        }
        else {
            throw new Exception("Change password failed");
        }
    }

}
