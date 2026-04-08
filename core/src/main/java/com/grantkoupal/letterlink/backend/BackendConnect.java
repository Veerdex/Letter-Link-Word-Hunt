package com.grantkoupal.letterlink.backend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.grantkoupal.letterlink.backend.data.PlayerData;
import com.grantkoupal.letterlink.backend.data.SessionData;

import java.net.URLEncoder;

public class BackendConnect {

    private static final String BASE_URL = "https://letter-link-backend-production.up.railway.app";
    private static final String HEADER_PLAYER_ID = "X-Player-Id";
    private static final String HEADER_PLAYER_TOKEN = "X-Player-Token";

    private interface BaseCallback<T> {
        void onSuccess(T value);
        void onFailure(Throwable t);
    }

    private interface ResponseParser<T> {
        T parse(String jsonText);
    }

    public interface RegisterCallback extends BaseCallback<RegisterResponse> {
    }

    public interface PlayerDataCallback extends BaseCallback<PlayerData> {
    }

    public interface UpdateSettingsCallback extends BaseCallback<UpdateSettingsResponse> {
    }

    public interface UpdateStatsCallback extends BaseCallback<UpdateStatsResponse> {
    }

    public interface UpdateMmrCallback extends BaseCallback<UpdateMmrResponse> {
    }

    public void registerPlayer(String username, RegisterCallback callback) {
        String url = BASE_URL + "/players/register";
        String body = "{\"username\":\"" + escapeJson(username) + "\"}";

        System.out.println("REGISTER URL: " + url);
        System.out.println("REGISTER BODY: " + body);

        HttpRequest request = buildPostRequest(url, body);
        sendRequest("REGISTER", request, callback, new ResponseParser<RegisterResponse>() {
            @Override
            public RegisterResponse parse(String jsonText) {
                return parseRegisterResponse(jsonText);
            }
        });
    }

    public void bootstrapSession(String playerId, PlayerDataCallback callback) {
        String url = BASE_URL + "/players/bootstrap-session";
        String body = "{\"id\":\"" + escapeJson(playerId) + "\"}";

        System.out.println("BOOTSTRAP SESSION URL: " + url);
        System.out.println("BOOTSTRAP SESSION BODY: " + body);

        HttpRequest request = buildPostRequest(url, body);
        sendRequest("BOOTSTRAP SESSION", request, callback, new ResponseParser<PlayerData>() {
            @Override
            public PlayerData parse(String jsonText) {
                return parsePlayerData(jsonText);
            }
        });
    }

    public void getPlayerData(String playerId, PlayerDataCallback callback) {
        String url = BASE_URL + "/players/get?id=" + encode(playerId);

        System.out.println("GET PLAYER URL: " + url);
        debugAuth("GET PLAYER", SessionData.id, SessionData.authToken);

        HttpRequest request = buildAuthenticatedGetRequest(url, SessionData.id, SessionData.authToken);
        sendRequest("GET PLAYER", request, callback, new ResponseParser<PlayerData>() {
            @Override
            public PlayerData parse(String jsonText) {
                return parsePlayerData(jsonText);
            }
        });
    }

    public void getPlayerByUsername(String username, PlayerDataCallback callback) {
        String url = BASE_URL + "/players/by-username?username=" + encode(username);

        System.out.println("GET PLAYER BY USERNAME URL: " + url);
        debugAuth("GET PLAYER BY USERNAME", SessionData.id, SessionData.authToken);

        HttpRequest request = buildAuthenticatedGetRequest(url, SessionData.id, SessionData.authToken);
        sendRequest("GET PLAYER BY USERNAME", request, callback, new ResponseParser<PlayerData>() {
            @Override
            public PlayerData parse(String jsonText) {
                return parsePlayerData(jsonText);
            }
        });
    }

    public void updatePlayerSettings(
        String playerId,
        boolean musicEnabled,
        boolean sfxEnabled,
        boolean vibrationEnabled,
        String theme,
        String currentGamemode,
        int currentBoardWidth,
        int currentBoardHeight,
        UpdateSettingsCallback callback
    ) {
        String url = BASE_URL + "/players/update-settings";
        String body = "{"
            + "\"id\":\"" + escapeJson(playerId) + "\"," 
            + "\"musicEnabled\":" + musicEnabled + ","
            + "\"sfxEnabled\":" + sfxEnabled + ","
            + "\"vibrationEnabled\":" + vibrationEnabled + ","
            + "\"theme\":\"" + escapeJson(theme) + "\","
            + "\"currentGamemode\":\"" + escapeJson(currentGamemode) + "\","
            + "\"currentBoardWidth\":" + currentBoardWidth + ","
            + "\"currentBoardHeight\":" + currentBoardHeight
            + "}";

        System.out.println("UPDATE SETTINGS URL: " + url);
        System.out.println("UPDATE SETTINGS BODY: " + body);
        debugAuth("UPDATE SETTINGS", SessionData.id, SessionData.authToken);

        HttpRequest request = buildAuthenticatedPostRequest(url, body, SessionData.id, SessionData.authToken);
        sendRequest("UPDATE SETTINGS", request, callback, new ResponseParser<UpdateSettingsResponse>() {
            @Override
            public UpdateSettingsResponse parse(String jsonText) {
                return parseUpdateSettingsResponse(jsonText);
            }
        });
    }

    public void updatePlayerStats(
        String playerId,
        int winsToAdd,
        int lossesToAdd,
        UpdateStatsCallback callback
    ) {
        String url = BASE_URL + "/players/update-stats";
        String body = "{"
            + "\"id\":\"" + escapeJson(playerId) + "\","
            + "\"winsToAdd\":" + winsToAdd + ","
            + "\"lossesToAdd\":" + lossesToAdd
            + "}";

        System.out.println("UPDATE STATS URL: " + url);
        System.out.println("UPDATE STATS BODY: " + body);
        debugAuth("UPDATE STATS", SessionData.id, SessionData.authToken);

        HttpRequest request = buildAuthenticatedPostRequest(url, body, SessionData.id, SessionData.authToken);
        sendRequest("UPDATE STATS", request, callback, new ResponseParser<UpdateStatsResponse>() {
            @Override
            public UpdateStatsResponse parse(String jsonText) {
                return parseUpdateStatsResponse(jsonText);
            }
        });
    }

    public void updatePlayerMmr(
        String playerId,
        String mode,
        int mmr,
        UpdateMmrCallback callback
    ) {
        String url = BASE_URL + "/players/update-mmr";
        String body = "{"
            + "\"id\":\"" + escapeJson(playerId) + "\","
            + "\"mode\":\"" + escapeJson(mode) + "\","
            + "\"mmr\":" + mmr
            + "}";

        System.out.println("UPDATE MMR URL: " + url);
        System.out.println("UPDATE MMR BODY: " + body);
        debugAuth("UPDATE MMR", SessionData.id, SessionData.authToken);

        HttpRequest request = buildAuthenticatedPostRequest(url, body, SessionData.id, SessionData.authToken);
        sendRequest("UPDATE MMR", request, callback, new ResponseParser<UpdateMmrResponse>() {
            @Override
            public UpdateMmrResponse parse(String jsonText) {
                return parseUpdateMmrResponse(jsonText);
            }
        });
    }

    private HttpRequest buildGetRequest(String url) {
        return new HttpRequestBuilder()
            .newRequest()
            .method(HttpMethods.GET)
            .url(url)
            .timeout(10000)
            .build();
    }

    private HttpRequest buildAuthenticatedGetRequest(String url, String authPlayerId, String authToken) {
        HttpRequest request = buildGetRequest(url);
        applyAuthHeaders(request, authPlayerId, authToken);
        return request;
    }

    private HttpRequest buildPostRequest(String url, String body) {
        HttpRequest request = new HttpRequestBuilder()
            .newRequest()
            .method(HttpMethods.POST)
            .url(url)
            .timeout(10000)
            .build();

        request.setHeader("Content-Type", "application/json");
        request.setContent(body);
        return request;
    }

    private HttpRequest buildAuthenticatedPostRequest(String url, String body, String authPlayerId, String authToken) {
        HttpRequest request = buildPostRequest(url, body);
        applyAuthHeaders(request, authPlayerId, authToken);
        return request;
    }

    private void applyAuthHeaders(HttpRequest request, String authPlayerId, String authToken) {
        if (request == null) {
            return;
        }

        if (!isBlank(authPlayerId)) {
            request.setHeader(HEADER_PLAYER_ID, authPlayerId.trim());
        }
        if (!isBlank(authToken)) {
            request.setHeader(HEADER_PLAYER_TOKEN, authToken.trim());
        }
    }

    private void debugAuth(String label, String authPlayerId, String authToken) {
        System.out.println(label + " AUTH PLAYER ID: " + (isBlank(authPlayerId) ? "<missing>" : authPlayerId));
        System.out.println(label + " AUTH TOKEN PRESENT: " + (!isBlank(authToken)));
        System.out.println(label + " AUTH TOKEN PREVIEW: " + previewToken(authToken));
    }

    private String previewToken(String token) {
        if (isBlank(token)) {
            return "<missing>";
        }

        String trimmed = token.trim();
        if (trimmed.length() <= 8) {
            return trimmed;
        }

        return trimmed.substring(0, 8) + "...";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private <T> void sendRequest(
        final String label,
        HttpRequest request,
        final BaseCallback<T> callback,
        final ResponseParser<T> parser
    ) {
        Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(HttpResponse httpResponse) {
                try {
                    int statusCode = httpResponse.getStatus() == null ? -1 : httpResponse.getStatus().getStatusCode();
                    String result = httpResponse.getResultAsString();

                    System.out.println(label + " STATUS: " + statusCode);
                    System.out.println(label + " RAW RESPONSE: " + result);

                    if (statusCode < 200 || statusCode >= 300) {
                        String errorMessage = extractApiError(result);
                        callback.onFailure(new RuntimeException(
                            label + " request failed. HTTP status: " + statusCode + ", error: " + errorMessage
                        ));
                        return;
                    }

                    if (result == null || result.trim().isEmpty()) {
                        callback.onFailure(new RuntimeException(
                            label + " request returned an empty response."
                        ));
                        return;
                    }

                    T parsed = parser.parse(result);
                    callback.onSuccess(parsed);

                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }

            @Override
            public void failed(Throwable t) {
                System.out.println(label + " REQUEST FAILED:");
                if (t != null) {
                    t.printStackTrace();
                    System.out.println(label + " FAILURE TYPE: " + t.getClass().getName());
                    System.out.println(label + " FAILURE MESSAGE: " + t.getMessage());
                } else {
                    System.out.println(label + " FAILURE: throwable was null");
                }
                callback.onFailure(t == null ? new RuntimeException("Unknown request failure") : t);
            }

            @Override
            public void cancelled() {
                callback.onFailure(new RuntimeException(label + " request cancelled"));
            }
        });
    }

    private RegisterResponse parseRegisterResponse(String jsonText) {
        JsonValue data = parseApiData(jsonText, "Register");

        RegisterResponse response = new RegisterResponse();
        response.id = data.getString("id");
        response.username = data.getString("username");
        response.authToken = data.has("authToken") ? data.getString("authToken") : "";
        return response;
    }

    private PlayerData parsePlayerData(String jsonText) {
        JsonValue data = parseApiData(jsonText, "Get player");

        PlayerData playerData = new PlayerData();
        playerData.id = data.getString("id");
        playerData.username = data.getString("username");
        playerData.musicEnabled = data.getBoolean("musicEnabled");
        playerData.sfxEnabled = data.getBoolean("sfxEnabled");
        playerData.vibrationEnabled = data.has("vibrationEnabled") ? data.getBoolean("vibrationEnabled") : true;
        playerData.theme = data.getString("theme");
        playerData.wins = data.getInt("wins");
        playerData.losses = data.getInt("losses");
        playerData.currentGamemode = data.getString("currentGamemode");
        playerData.currentBoardWidth = data.getInt("currentBoardWidth");
        playerData.currentBoardHeight = data.getInt("currentBoardHeight");
        playerData.createdAt = data.getString("createdAt");
        playerData.updatedAt = data.getString("updatedAt");
        playerData.authToken = data.has("authToken") ? data.getString("authToken") : "";

        JsonValue mmr = data.get("mmr");
        playerData.mmr4x4 = getIntOrDefault(mmr, "4x4", 1000);
        playerData.mmr4x5 = getIntOrDefault(mmr, "4x5", 1000);
        playerData.mmr5x5 = getIntOrDefault(mmr, "5x5", 1000);

        return playerData;
    }

    private UpdateSettingsResponse parseUpdateSettingsResponse(String jsonText) {
        JsonValue data = parseApiData(jsonText, "Update settings");

        UpdateSettingsResponse response = new UpdateSettingsResponse();
        response.id = data.getString("id");
        response.musicEnabled = data.getBoolean("musicEnabled");
        response.sfxEnabled = data.getBoolean("sfxEnabled");
        response.vibrationEnabled = data.has("vibrationEnabled") ? data.getBoolean("vibrationEnabled") : true;
        response.theme = data.getString("theme");
        response.currentGamemode = data.getString("currentGamemode");
        response.currentBoardWidth = data.getInt("currentBoardWidth");
        response.currentBoardHeight = data.getInt("currentBoardHeight");
        response.updatedAt = data.getString("updatedAt");

        return response;
    }

    private UpdateStatsResponse parseUpdateStatsResponse(String jsonText) {
        JsonValue data = parseApiData(jsonText, "Update stats");

        UpdateStatsResponse response = new UpdateStatsResponse();
        response.id = data.getString("id");
        response.wins = data.getInt("wins");
        response.losses = data.getInt("losses");
        response.updatedAt = data.getString("updatedAt");

        return response;
    }

    private UpdateMmrResponse parseUpdateMmrResponse(String jsonText) {
        JsonValue data = parseApiData(jsonText, "Update MMR");

        UpdateMmrResponse response = new UpdateMmrResponse();
        response.id = data.getString("id");
        response.mode = data.getString("mode");
        response.mmr = data.getInt("mmr");
        response.updatedAt = data.getString("updatedAt");

        return response;
    }

    private JsonValue parseApiData(String jsonText, String context) {
        JsonValue root = new JsonReader().parse(jsonText);

        if (root == null) {
            throw new RuntimeException(context + " response JSON was null. Raw response: " + jsonText);
        }

        boolean success = root.getBoolean("success", false);
        if (!success) {
            String error = root.has("error") ? root.getString("error") : "Unknown error";
            throw new RuntimeException(context + " failed: " + error);
        }

        JsonValue data = root.get("data");
        if (data == null) {
            throw new RuntimeException(context + " response did not contain data. Raw response: " + jsonText);
        }

        return data;
    }

    private String extractApiError(String jsonText) {
        if (jsonText == null || jsonText.trim().isEmpty()) {
            return "Unknown error";
        }

        try {
            JsonValue root = new JsonReader().parse(jsonText);
            if (root != null && root.has("error")) {
                return root.getString("error");
            }
        } catch (Exception ignored) {
        }

        return jsonText;
    }

    private int getIntOrDefault(JsonValue json, String key, int defaultValue) {
        if (json == null || !json.has(key)) {
            return defaultValue;
        }
        return json.getInt(key);
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode URL value: " + value, e);
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}
