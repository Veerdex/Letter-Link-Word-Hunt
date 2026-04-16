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
import java.util.ArrayList;
import java.util.List;

public class BackendConnect {

    private static final String BASE_URL = "https://letter-link-backend-production.up.railway.app";

    // 0 = silent
    // 1 = errors only
    // 2 = request summaries + status codes
    // 3 = raw responses, auth previews, stack traces
    private static final int DEBUG = 3;

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

    public interface BanAmountCallback extends BaseCallback<BanAmountResponse> {
    }

    public interface QueueTicketCallback extends BaseCallback<QueueTicketResponse> {
    }

    public interface CancelQueueCallback extends BaseCallback<CancelQueueResponse> {
    }

    public interface QueueHeartbeatCallback extends BaseCallback<QueueHeartbeatResponse> {
    }

    public interface MatchStatusCallback extends BaseCallback<MatchStatusResponse> {
    }

    public interface AcknowledgeMatchCallback extends BaseCallback<AcknowledgeMatchResponse> {
    }

    public interface AbandonMatchCallback extends BaseCallback<AbandonMatchResponse> {
    }

    public void registerPlayer(String username, RegisterCallback callback) {
        String url = BASE_URL + "/players/register";
        String body = "{\"username\":\"" + escapeJson(username) + "\"}";

        debug(2, "REGISTER URL: " + url);
        debug(3, "REGISTER BODY: " + body);

        HttpRequest request = buildPostRequest(url, body, false);
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

        debug(2, "BOOTSTRAP SESSION URL: " + url);
        debug(3, "BOOTSTRAP SESSION BODY: " + body);

        HttpRequest request = buildPostRequest(url, body, false);
        sendRequest("BOOTSTRAP SESSION", request, callback, new ResponseParser<PlayerData>() {
            @Override
            public PlayerData parse(String jsonText) {
                return parsePlayerData(jsonText);
            }
        });
    }

    public void getPlayerData(String playerId, PlayerDataCallback callback) {
        String url = BASE_URL + "/players/get?id=" + encode(playerId);

        debug(2, "GET PLAYER URL: " + url);
        debug(3, "GET PLAYER AUTH PLAYER ID: " + SessionData.id);
        debug(3, "GET PLAYER AUTH TOKEN PRESENT: " + (SessionData.authToken != null && !SessionData.authToken.trim().isEmpty()));
        debug(3, "GET PLAYER AUTH TOKEN PREVIEW: " + previewToken(SessionData.authToken));

        HttpRequest request = buildGetRequest(url, true);
        sendRequest("GET PLAYER", request, callback, new ResponseParser<PlayerData>() {
            @Override
            public PlayerData parse(String jsonText) {
                return parsePlayerData(jsonText);
            }
        });
    }

    public void getPlayerByUsername(String username, PlayerDataCallback callback) {
        String url = BASE_URL + "/players/by-username?username=" + encode(username);

        debug(2, "GET PLAYER BY USERNAME URL: " + url);

        HttpRequest request = buildGetRequest(url, true);
        sendRequest("GET PLAYER BY USERNAME", request, callback, new ResponseParser<PlayerData>() {
            @Override
            public PlayerData parse(String jsonText) {
                return parsePlayerData(jsonText);
            }
        });
    }

    public void getBanAmount(String playerId, BanAmountCallback callback) {
        String url = BASE_URL + "/players/ban-amount?id=" + encode(playerId);

        debug(2, "GET BAN AMOUNT URL: " + url);

        HttpRequest request = buildGetRequest(url, true);
        sendRequest("GET BAN AMOUNT", request, callback, new ResponseParser<BanAmountResponse>() {
            @Override
            public BanAmountResponse parse(String jsonText) {
                return parseBanAmountResponse(jsonText);
            }
        });
    }

    public void updatePlayerSettings(
        String playerId,
        boolean musicEnabled,
        boolean sfxEnabled,
        boolean vibrationEnabled,
        String theme,
        String mode,
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
            + "\"mode\":\"" + escapeJson(mode) + "\","
            + "\"currentGamemode\":\"" + escapeJson(currentGamemode) + "\","
            + "\"currentBoardWidth\":" + currentBoardWidth + ","
            + "\"currentBoardHeight\":" + currentBoardHeight
            + "}";

        debug(2, "UPDATE SETTINGS URL: " + url);
        debug(3, "UPDATE SETTINGS BODY: " + body);

        HttpRequest request = buildPostRequest(url, body, true);
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

        debug(2, "UPDATE STATS URL: " + url);
        debug(3, "UPDATE STATS BODY: " + body);

        HttpRequest request = buildPostRequest(url, body, true);
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

        debug(2, "UPDATE MMR URL: " + url);
        debug(3, "UPDATE MMR BODY: " + body);

        HttpRequest request = buildPostRequest(url, body, true);
        sendRequest("UPDATE MMR", request, callback, new ResponseParser<UpdateMmrResponse>() {
            @Override
            public UpdateMmrResponse parse(String jsonText) {
                return parseUpdateMmrResponse(jsonText);
            }
        });
    }

    public void queueForMatch(
        String playerId,
        String mode,
        String currentGamemode,
        int boardWidth,
        int boardHeight,
        int power,
        QueueTicketCallback callback
    ) {
        String url = BASE_URL + "/matchmaking/queue";
        String body = "{"
            + "\"id\":\"" + escapeJson(playerId) + "\","
            + "\"mode\":\"" + escapeJson(mode) + "\","
            + "\"currentGamemode\":\"" + escapeJson(currentGamemode) + "\","
            + "\"boardWidth\":" + boardWidth + ","
            + "\"boardHeight\":" + boardHeight + ","
            + "\"power\":" + power
            + "}";

        debug(2, "QUEUE URL: " + url);
        debug(3, "QUEUE BODY: " + body);

        HttpRequest request = buildPostRequest(url, body, true);
        sendRequest("QUEUE", request, callback, new ResponseParser<QueueTicketResponse>() {
            @Override
            public QueueTicketResponse parse(String jsonText) {
                return parseQueueTicketResponse(jsonText);
            }
        });
    }

    public void cancelQueue(String ticketId, CancelQueueCallback callback) {
        String url = BASE_URL + "/matchmaking/cancel";
        String body = "{"
            + "\"ticketId\":\"" + escapeJson(ticketId) + "\""
            + "}";

        debug(2, "CANCEL QUEUE URL: " + url);
        debug(3, "CANCEL QUEUE BODY: " + body);

        HttpRequest request = buildPostRequest(url, body, true);
        sendRequest("CANCEL QUEUE", request, callback, new ResponseParser<CancelQueueResponse>() {
            @Override
            public CancelQueueResponse parse(String jsonText) {
                return parseCancelQueueResponse(jsonText);
            }
        });
    }

    public void sendQueueHeartbeat(String ticketId, QueueHeartbeatCallback callback) {
        String url = BASE_URL + "/matchmaking/heartbeat";
        String body = "{"
            + "\"ticketId\":\"" + escapeJson(ticketId) + "\""
            + "}";

        debug(3, "QUEUE HEARTBEAT URL: " + url);
        debug(3, "QUEUE HEARTBEAT BODY: " + body);

        HttpRequest request = buildPostRequest(url, body, true);
        sendRequest("QUEUE HEARTBEAT", request, callback, new ResponseParser<QueueHeartbeatResponse>() {
            @Override
            public QueueHeartbeatResponse parse(String jsonText) {
                return parseQueueHeartbeatResponse(jsonText);
            }
        });
    }

    public void getQueueStatus(String ticketId, MatchStatusCallback callback) {
        String url = BASE_URL + "/matchmaking/status?ticketId=" + encode(ticketId);

        debug(3, "QUEUE STATUS URL: " + url);

        HttpRequest request = buildGetRequest(url, true);
        sendRequest("QUEUE STATUS", request, callback, new ResponseParser<MatchStatusResponse>() {
            @Override
            public MatchStatusResponse parse(String jsonText) {
                return parseMatchStatusResponse(jsonText);
            }
        });
    }

    public void acknowledgeMatch(String matchId, AcknowledgeMatchCallback callback) {
        String url = BASE_URL + "/matchmaking/acknowledge";
        String body = "{"
            + "\"matchId\":\"" + escapeJson(matchId) + "\""
            + "}";

        debug(2, "ACKNOWLEDGE MATCH URL: " + url);
        debug(3, "ACKNOWLEDGE MATCH BODY: " + body);

        HttpRequest request = buildPostRequest(url, body, true);
        sendRequest("ACKNOWLEDGE MATCH", request, callback, new ResponseParser<AcknowledgeMatchResponse>() {
            @Override
            public AcknowledgeMatchResponse parse(String jsonText) {
                return parseAcknowledgeMatchResponse(jsonText);
            }
        });
    }

    public void abandonMatch(String matchId, AbandonMatchCallback callback) {
        String url = BASE_URL + "/matchmaking/abandon";
        String body = "{"
            + "\"matchId\":\"" + escapeJson(matchId) + "\""
            + "}";

        debug(2, "ABANDON MATCH URL: " + url);
        debug(3, "ABANDON MATCH BODY: " + body);

        HttpRequest request = buildPostRequest(url, body, true);
        sendRequest("ABANDON MATCH", request, callback, new ResponseParser<AbandonMatchResponse>() {
            @Override
            public AbandonMatchResponse parse(String jsonText) {
                return parseAbandonMatchResponse(jsonText);
            }
        });
    }

    private HttpRequest buildGetRequest(String url, boolean includeAuthHeaders) {
        HttpRequest request = new HttpRequestBuilder()
            .newRequest()
            .method(HttpMethods.GET)
            .url(url)
            .timeout(10000)
            .build();

        if (includeAuthHeaders) {
            applyAuthHeaders(request);
        }

        return request;
    }

    private HttpRequest buildPostRequest(String url, String body, boolean includeAuthHeaders) {
        HttpRequest request = new HttpRequestBuilder()
            .newRequest()
            .method(HttpMethods.POST)
            .url(url)
            .timeout(10000)
            .build();

        request.setHeader("Content-Type", "application/json");
        request.setContent(body);

        if (includeAuthHeaders) {
            applyAuthHeaders(request);
        }

        return request;
    }

    private void applyAuthHeaders(HttpRequest request) {
        if (SessionData.id != null && !SessionData.id.trim().isEmpty()) {
            request.setHeader("X-Player-Id", SessionData.id);
        }

        if (SessionData.authToken != null && !SessionData.authToken.trim().isEmpty()) {
            request.setHeader("X-Player-Token", SessionData.authToken);
        }
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

                    debug(2, label + " STATUS: " + statusCode);
                    debug(3, label + " RAW RESPONSE: " + result);

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
                debugError(1, label + " REQUEST FAILED", t);
                callback.onFailure(t == null ? new RuntimeException("Unknown request failure") : t);
            }

            @Override
            public void cancelled() {
                debug(1, label + " request cancelled");
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
        playerData.mode = data.has("mode") ? data.getString("mode") : "practice";
        playerData.banAmount = data.has("banAmount") ? data.getInt("banAmount") : 0;
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

    private BanAmountResponse parseBanAmountResponse(String jsonText) {
        JsonValue data = parseApiData(jsonText, "Get ban amount");

        BanAmountResponse response = new BanAmountResponse();
        response.id = data.getString("id");
        response.banAmount = data.getInt("banAmount");
        response.updatedAt = data.getString("updatedAt");
        return response;
    }

    private UpdateSettingsResponse parseUpdateSettingsResponse(String jsonText) {
        JsonValue data = parseApiData(jsonText, "Update settings");

        UpdateSettingsResponse response = new UpdateSettingsResponse();
        response.id = data.getString("id");
        response.musicEnabled = data.getBoolean("musicEnabled");
        response.sfxEnabled = data.getBoolean("sfxEnabled");
        response.vibrationEnabled = data.has("vibrationEnabled") ? data.getBoolean("vibrationEnabled") : true;
        response.theme = data.getString("theme");
        response.mode = data.has("mode") ? data.getString("mode") : "practice";
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

    private QueueTicketResponse parseQueueTicketResponse(String jsonText) {
        JsonValue data = parseApiData(jsonText, "Queue for match");

        QueueTicketResponse response = new QueueTicketResponse();
        response.ticketId = data.getString("ticketId");
        response.ticketStatus = data.getString("ticketStatus");
        response.mode = data.getString("mode");
        response.currentGamemode = data.getString("currentGamemode");
        response.boardWidth = data.getInt("boardWidth");
        response.boardHeight = data.getInt("boardHeight");
        response.mmr = data.getInt("mmr");
        response.power = data.getInt("power");
        response.queuedAt = data.getString("queuedAt");
        response.updatedAt = data.getString("updatedAt");
        response.matchId = data.has("matchId") && !data.get("matchId").isNull() ? data.getString("matchId") : null;
        return response;
    }

    private CancelQueueResponse parseCancelQueueResponse(String jsonText) {
        JsonValue data = parseApiData(jsonText, "Cancel queue");

        CancelQueueResponse response = new CancelQueueResponse();
        response.ticketId = data.getString("ticketId");
        response.cancelled = data.getBoolean("cancelled");
        response.ticketStatus = data.getString("ticketStatus");
        response.matchId = data.has("matchId") && !data.get("matchId").isNull() ? data.getString("matchId") : null;
        response.matchStatus = data.has("matchStatus") && !data.get("matchStatus").isNull() ? data.getString("matchStatus") : null;
        response.updatedAt = data.getString("updatedAt");
        return response;
    }

    private QueueHeartbeatResponse parseQueueHeartbeatResponse(String jsonText) {
        JsonValue data = parseApiData(jsonText, "Queue heartbeat");

        QueueHeartbeatResponse response = new QueueHeartbeatResponse();
        response.ticketId = data.getString("ticketId");
        response.ticketStatus = data.getString("ticketStatus");
        response.matchId = data.has("matchId") && !data.get("matchId").isNull() ? data.getString("matchId") : null;
        response.matchStatus = data.has("matchStatus") && !data.get("matchStatus").isNull() ? data.getString("matchStatus") : null;
        response.updatedAt = data.getString("updatedAt");
        return response;
    }

    private MatchStatusResponse parseMatchStatusResponse(String jsonText) {
        JsonValue data = parseApiData(jsonText, "Match status");

        MatchStatusResponse response = new MatchStatusResponse();
        response.ticketId = data.getString("ticketId");
        response.ticketStatus = data.getString("ticketStatus");
        response.matchId = data.has("matchId") && !data.get("matchId").isNull() ? data.getString("matchId") : null;
        response.matchStatus = data.has("matchStatus") && !data.get("matchStatus").isNull() ? data.getString("matchStatus") : null;
        response.playerAcknowledged = data.getBoolean("playerAcknowledged", false);
        response.bothAcknowledged = data.getBoolean("bothAcknowledged", false);
        response.ready = data.getBoolean("ready", false);
        response.opponentId = data.has("opponentId") && !data.get("opponentId").isNull() ? data.getString("opponentId") : null;
        response.opponentUsername = data.has("opponentUsername") && !data.get("opponentUsername").isNull() ? data.getString("opponentUsername") : null;
        response.mode = data.has("mode") && !data.get("mode").isNull() ? data.getString("mode") : null;
        response.currentGamemode = data.has("currentGamemode") && !data.get("currentGamemode").isNull() ? data.getString("currentGamemode") : null;
        response.boardWidth = data.getInt("boardWidth", 0);
        response.boardHeight = data.getInt("boardHeight", 0);
        response.power = data.getInt("power", 4);
        response.boardLetters = data.has("boardLetters") && !data.get("boardLetters").isNull() ? data.getString("boardLetters") : null;
        response.boardRows = parseStringList(data.get("boardRows"));
        response.updatedAt = data.getString("updatedAt");
        return response;
    }

    private AcknowledgeMatchResponse parseAcknowledgeMatchResponse(String jsonText) {
        JsonValue data = parseApiData(jsonText, "Acknowledge match");

        AcknowledgeMatchResponse response = new AcknowledgeMatchResponse();
        response.matchId = data.getString("matchId");
        response.matchStatus = data.has("matchStatus") && !data.get("matchStatus").isNull() ? data.getString("matchStatus") : null;
        response.playerAcknowledged = data.getBoolean("playerAcknowledged", false);
        response.bothAcknowledged = data.getBoolean("bothAcknowledged", false);
        response.ready = data.getBoolean("ready", false);
        response.power = data.getInt("power", 4);
        response.boardLetters = data.has("boardLetters") && !data.get("boardLetters").isNull() ? data.getString("boardLetters") : null;
        response.boardRows = parseStringList(data.get("boardRows"));
        response.updatedAt = data.getString("updatedAt");
        return response;
    }

    private AbandonMatchResponse parseAbandonMatchResponse(String jsonText) {
        JsonValue data = parseApiData(jsonText, "Abandon match");

        AbandonMatchResponse response = new AbandonMatchResponse();
        response.matchId = data.getString("matchId");
        response.matchStatus = data.getString("matchStatus");
        response.banAmount = data.getInt("banAmount");
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
        if (json == null || !json.has(key) || json.get(key).isNull()) {
            return defaultValue;
        }
        return json.getInt(key);
    }

    private List<String> parseStringList(JsonValue json) {
        List<String> values = new ArrayList<String>();
        if (json == null) {
            return values;
        }
        for (JsonValue child = json.child; child != null; child = child.next) {
            values.add(child.asString());
        }
        return values;
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

    private String previewToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return "<missing>";
        }

        String trimmed = token.trim();
        if (trimmed.length() <= 8) {
            return trimmed;
        }

        return trimmed.substring(0, 8) + "...";
    }

    private static void debug(int level, String message) {
        if (DEBUG >= level) {
            System.out.println("[BackendConnect] " + message);
        }
    }

    private static void debugError(int level, String message, Throwable t) {
        if (DEBUG >= level) {
            System.out.println("[BackendConnect] " + message);
            if (t != null) {
                System.out.println("[BackendConnect] " + t.getClass().getSimpleName() + ": " + t.getMessage());
                if (DEBUG >= 3) {
                    t.printStackTrace();
                }
            }
        }
    }
}
