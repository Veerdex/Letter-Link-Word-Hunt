package com.grantkoupal.letterlink.backend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Timer;
import com.grantkoupal.letterlink.backend.data.PlayerData;
import com.grantkoupal.letterlink.backend.data.SessionData;

import java.util.UUID;

public class BackendHandler {
    private static final BackendConnect backend = new BackendConnect();

    // 0 = silent
    // 1 = important events + failures
    // 2 = normal flow details
    // 3 = very verbose
    private static final int DEBUG = 0;

    private static Timer.Task heartbeatTask;
    private static Timer.Task statusPollTask;
    private static boolean acknowledgeInFlight = false;
    private static boolean matchFoundNotified = false;

    public interface StartupCallback {
        void onSuccess();
        void onFailure(Throwable t);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(Throwable t);
    }

    public interface CancelMatchmakingCallback {
        void onCancelled();
        void onAlreadyMatched(CancelQueueResponse response);
        void onFailure(Throwable t);
    }

    public interface MatchmakingCallback {
        void onQueued(QueueTicketResponse response);
        void onMatchFound(MatchStatusResponse response);
        void onMatchReady(MatchStatusResponse response);
        void onCancelled();
        void onFailure(Throwable t);
    }

    public static void startUp(final StartupCallback callback) {
        final Preferences prefs = getPreferences();

        String savedPlayerId = prefs.getString("playerId", "");
        String savedAuthToken = prefs.getString("authToken", "");

        if (savedPlayerId != null) {
            savedPlayerId = savedPlayerId.trim();
        }
        if (savedAuthToken != null) {
            savedAuthToken = savedAuthToken.trim();
        }

        debug(2, "Startup beginning.");
        debug(3, "Saved playerId present: " + !isBlank(savedPlayerId));
        debug(3, "Saved authToken present: " + !isBlank(savedAuthToken));

        if (isBlank(savedPlayerId)) {
            debug(1, "No saved player ID. Registering a fresh player.");
            registerFreshPlayer(prefs, callback);
            return;
        }

        SessionData.id = savedPlayerId;

        if (!isBlank(savedAuthToken)) {
            SessionData.authToken = savedAuthToken;
            debug(2, "Found saved auth token. Loading existing player data.");
            loadPlayerData(savedPlayerId, callback);
            return;
        }

        debug(1, "No saved auth token. Bootstrapping legacy session.");
        backend.bootstrapSession(savedPlayerId, new BackendConnect.PlayerDataCallback() {
            @Override
            public void onSuccess(final PlayerData playerData) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        applyPlayerDataToSession(playerData);

                        prefs.putString("playerId", playerData.id == null ? "" : playerData.id);
                        prefs.putString("authToken", playerData.authToken == null ? "" : playerData.authToken);
                        prefs.flush();

                        debug(1, "Legacy session bootstrapped successfully.");
                        debug(2, "Bootstrapped player: " + SessionData.username + " (" + SessionData.id + ")");

                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                });
            }

            @Override
            public void onFailure(final Throwable t) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        debugError(1, "Bootstrap failed. Registering a fresh local player.", t);
                        registerFreshPlayer(prefs, callback);
                    }
                });
            }
        });
    }

    private static void registerFreshPlayer(final Preferences prefs, final StartupCallback callback) {
        final String username = generateGuestUsername();
        debug(1, "Registering new player: " + username);

        backend.registerPlayer(username, new BackendConnect.RegisterCallback() {
            @Override
            public void onSuccess(final RegisterResponse response) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        prefs.putString("playerId", response.id == null ? "" : response.id);
                        prefs.putString("authToken", response.authToken == null ? "" : response.authToken);
                        prefs.flush();

                        SessionData.id = response.id;
                        SessionData.username = response.username;
                        SessionData.authToken = response.authToken;

                        debug(1, "Registered new player successfully.");
                        debug(2, "Registered player: " + response.username + " (" + response.id + ")");

                        loadPlayerData(response.id, callback);
                    }
                });
            }

            @Override
            public void onFailure(final Throwable t) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        debugError(1, "Failed to register fresh player.", t);

                        if (callback != null) {
                            callback.onFailure(t);
                        }
                    }
                });
            }
        });
    }

    private static void loadPlayerData(final String playerId, final StartupCallback callback) {
        debug(2, "Loading player data for: " + playerId);

        backend.getPlayerData(playerId, new BackendConnect.PlayerDataCallback() {
            @Override
            public void onSuccess(final PlayerData playerData) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        applyPlayerDataToSession(playerData);

                        debug(1, "Player data loaded.");
                        debug(2, "Player: " + playerData.username
                            + " | Mode: " + playerData.mode
                            + " | Gamemode: " + playerData.currentGamemode);
                        debug(3, "Music=" + playerData.musicEnabled
                            + ", SFX=" + playerData.sfxEnabled
                            + ", Vibration=" + playerData.vibrationEnabled
                            + ", BanAmount=" + playerData.banAmount
                            + ", 4x4 MMR=" + playerData.mmr4x4
                            + ", 4x5 MMR=" + playerData.mmr4x5
                            + ", 5x5 MMR=" + playerData.mmr5x5
                            + ", Board=" + playerData.currentBoardWidth + "x" + playerData.currentBoardHeight);

                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                });
            }

            @Override
            public void onFailure(final Throwable t) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        debugError(1, "Failed to load player data.", t);

                        if (callback != null) {
                            callback.onFailure(t);
                        }
                    }
                });
            }
        });
    }

    public static void refreshBanAmount(final SimpleCallback callback) {
        if (isBlank(SessionData.id)) {
            RuntimeException error = new RuntimeException("SessionData.id is missing");
            debugError(1, "Cannot refresh ban amount.", error);
            if (callback != null) {
                callback.onFailure(error);
            }
            return;
        }

        debug(2, "Refreshing ban amount for player: " + SessionData.id);

        backend.getBanAmount(SessionData.id, new BackendConnect.BanAmountCallback() {
            @Override
            public void onSuccess(final BanAmountResponse response) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        SessionData.banAmount = response.banAmount;
                        SessionData.updatedAt = response.updatedAt;

                        debug(2, "Ban amount refreshed: " + response.banAmount);

                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                });
            }

            @Override
            public void onFailure(final Throwable t) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        debugError(1, "Failed to refresh ban amount.", t);

                        if (callback != null) {
                            callback.onFailure(t);
                        }
                    }
                });
            }
        });
    }

    public static void syncSessionSettingsToServer(final SimpleCallback callback) {
        if (isBlank(SessionData.id)) {
            RuntimeException error = new RuntimeException("SessionData.id is missing");
            debugError(1, "Cannot sync settings.", error);
            if (callback != null) {
                callback.onFailure(error);
            }
            return;
        }

        debug(2, "Syncing session settings to server.");
        debug(3, "Theme=" + SessionData.theme
            + ", Mode=" + SessionData.mode
            + ", Gamemode=" + SessionData.currentGamemode
            + ", Music=" + SessionData.musicEnabled
            + ", SFX=" + SessionData.sfxEnabled
            + ", Vibration=" + SessionData.vibrationEnabled
            + ", Board=" + SessionData.currentBoardWidth + "x" + SessionData.currentBoardHeight);

        backend.updatePlayerSettings(
            SessionData.id,
            SessionData.musicEnabled,
            SessionData.sfxEnabled,
            SessionData.vibrationEnabled,
            SessionData.theme,
            SessionData.mode,
            SessionData.currentGamemode,
            SessionData.currentBoardWidth,
            SessionData.currentBoardHeight,
            new BackendConnect.UpdateSettingsCallback() {
                @Override
                public void onSuccess(final UpdateSettingsResponse response) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            applySettingsResponseToSession(response);

                            debug(1, "Session settings synced successfully.");
                            debug(2, "Updated at: " + SessionData.updatedAt);

                            if (callback != null) {
                                callback.onSuccess();
                            }
                        }
                    });
                }

                @Override
                public void onFailure(final Throwable t) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            debugError(1, "Failed to sync session settings.", t);

                            if (callback != null) {
                                callback.onFailure(t);
                            }
                        }
                    });
                }
            }
        );
    }

    public static void startMatchmaking(
        final String mode,
        final String currentGamemode,
        final int boardWidth,
        final int boardHeight,
        final MatchmakingCallback callback
    ) {
        if (isBlank(SessionData.id)) {
            RuntimeException error = new RuntimeException("SessionData.id is missing");
            debugError(1, "Cannot start matchmaking.", error);
            if (callback != null) {
                callback.onFailure(error);
            }
            return;
        }

        stopMatchmakingTasks();

        final int power = SessionData.matchPower <= 0 ? 4 : SessionData.matchPower;

        debug(1, "Starting matchmaking.");
        debug(2, "Queue request: mode=" + mode
            + ", gamemode=" + currentGamemode
            + ", board=" + boardWidth + "x" + boardHeight
            + ", power=" + power);

        backend.queueForMatch(
            SessionData.id,
            mode,
            currentGamemode,
            boardWidth,
            boardHeight,
            power,
            new BackendConnect.QueueTicketCallback() {
                @Override
                public void onSuccess(final QueueTicketResponse response) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            SessionData.currentQueueTicketId = response.ticketId;
                            SessionData.matchmakingActive = true;
                            SessionData.currentMatchId = response.matchId;
                            acknowledgeInFlight = false;
                            matchFoundNotified = false;

                            debug(1, "Queued successfully.");
                            debug(2, "Ticket=" + response.ticketId
                                + ", Mode=" + response.mode
                                + ", Gamemode=" + response.currentGamemode
                                + ", Board=" + response.boardWidth + "x" + response.boardHeight
                                + ", MMR=" + response.mmr);

                            if (callback != null) {
                                callback.onQueued(response);
                            }

                            startHeartbeat(response.ticketId);
                            startStatusPolling(response.ticketId, callback);
                        }
                    });
                }

                @Override
                public void onFailure(final Throwable t) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            SessionData.matchmakingActive = false;
                            debugError(1, "Failed to queue for matchmaking.", t);

                            if (callback != null) {
                                callback.onFailure(t);
                            }
                        }
                    });
                }
            }
        );
    }

    public static void cancelMatchmaking(final CancelMatchmakingCallback callback) {
        if (isBlank(SessionData.currentQueueTicketId)) {
            RuntimeException error = new RuntimeException("No active queue ticket");
            debugError(1, "Cannot cancel matchmaking.", error);
            if (callback != null) {
                callback.onFailure(error);
            }
            return;
        }

        debug(1, "Cancelling matchmaking for ticket: " + SessionData.currentQueueTicketId);

        backend.cancelQueue(SessionData.currentQueueTicketId, new BackendConnect.CancelQueueCallback() {
            @Override
            public void onSuccess(final CancelQueueResponse response) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (response.cancelled) {
                            debug(1, "Queue cancelled successfully.");
                            clearMatchmakingState();
                            if (callback != null) {
                                callback.onCancelled();
                            }
                            return;
                        }

                        SessionData.currentMatchId = response.matchId;
                        debug(1, "Cancel arrived too late. Already matched.");
                        debug(2, "Match ID: " + response.matchId);

                        if (callback != null) {
                            callback.onAlreadyMatched(response);
                        }
                    }
                });
            }

            @Override
            public void onFailure(final Throwable t) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        debugError(1, "Failed to cancel matchmaking.", t);

                        if (callback != null) {
                            callback.onFailure(t);
                        }
                    }
                });
            }
        });
    }

    public static void abandonCurrentMatch(final SimpleCallback callback) {
        if (isBlank(SessionData.currentMatchId)) {
            RuntimeException error = new RuntimeException("No active match");
            debugError(1, "Cannot abandon match.", error);
            if (callback != null) {
                callback.onFailure(error);
            }
            return;
        }

        debug(1, "Abandoning current match: " + SessionData.currentMatchId);

        backend.abandonMatch(SessionData.currentMatchId, new BackendConnect.AbandonMatchCallback() {
            @Override
            public void onSuccess(final AbandonMatchResponse response) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        SessionData.banAmount = response.banAmount;
                        SessionData.currentMatchId = null;
                        SessionData.matchmakingActive = false;

                        debug(1, "Match abandoned successfully.");
                        debug(2, "New ban amount: " + response.banAmount);

                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                });
            }

            @Override
            public void onFailure(final Throwable t) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        debugError(1, "Failed to abandon match.", t);

                        if (callback != null) {
                            callback.onFailure(t);
                        }
                    }
                });
            }
        });
    }

    private static void startHeartbeat(final String ticketId) {
        debug(2, "Starting queue heartbeat for ticket: " + ticketId);

        heartbeatTask = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                backend.sendQueueHeartbeat(ticketId, new BackendConnect.QueueHeartbeatCallback() {
                    @Override
                    public void onSuccess(QueueHeartbeatResponse response) {
                        debug(3, "Heartbeat OK: ticket=" + response.ticketId
                            + ", ticketStatus=" + response.ticketStatus
                            + ", matchId=" + response.matchId);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        debugError(2, "Queue heartbeat failed.", t);
                    }
                });
            }
        }, 0f, 5f);
    }

    private static void startStatusPolling(final String ticketId, final MatchmakingCallback callback) {
        debug(2, "Starting matchmaking status polling for ticket: " + ticketId);

        statusPollTask = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                backend.getQueueStatus(ticketId, new BackendConnect.MatchStatusCallback() {
                    @Override
                    public void onSuccess(final MatchStatusResponse response) {
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                SessionData.currentQueueTicketId = response.ticketId;
                                SessionData.currentMatchId = response.matchId;

                                debug(3, "Status poll: ticketStatus=" + response.ticketStatus
                                    + ", matchStatus=" + response.matchStatus
                                    + ", matchId=" + response.matchId
                                    + ", ready=" + response.ready);

                                if ("CANCELLED".equals(response.ticketStatus) || "EXPIRED".equals(response.ticketStatus)) {
                                    debug(1, "Matchmaking cancelled or expired.");
                                    clearMatchmakingState();
                                    if (callback != null) {
                                        callback.onCancelled();
                                    }
                                    return;
                                }

                                if (response.matchId != null && response.matchId.trim().length() > 0
                                    && "MATCH_FOUND".equals(response.matchStatus)) {

                                    if (!matchFoundNotified) {
                                        matchFoundNotified = true;
                                        debug(1, "Match found.");
                                        debug(2, "Match ID=" + response.matchId
                                            + ", Opponent=" + response.opponentUsername);

                                        if (callback != null) {
                                            callback.onMatchFound(response);
                                        }
                                    }

                                    if (!response.playerAcknowledged && !acknowledgeInFlight) {
                                        acknowledgeInFlight = true;
                                        debug(2, "Acknowledging match: " + response.matchId);

                                        backend.acknowledgeMatch(response.matchId, new BackendConnect.AcknowledgeMatchCallback() {
                                            @Override
                                            public void onSuccess(final AcknowledgeMatchResponse ackResponse) {
                                                Gdx.app.postRunnable(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        acknowledgeInFlight = false;
                                                        SessionData.currentMatchId = ackResponse.matchId;

                                                        debug(2, "Match acknowledged successfully.");
                                                        debug(3, "Ack result: bothAcknowledged=" + ackResponse.bothAcknowledged
                                                            + ", ready=" + ackResponse.ready);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFailure(final Throwable t) {
                                                Gdx.app.postRunnable(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        acknowledgeInFlight = false;
                                                        debugError(2, "Failed to acknowledge match.", t);
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }

                                if (response.ready) {
                                    SessionData.mode = response.mode == null ? SessionData.mode : response.mode;
                                    SessionData.currentGamemode = response.currentGamemode == null ? SessionData.currentGamemode : response.currentGamemode;
                                    SessionData.currentBoardWidth = response.boardWidth;
                                    SessionData.currentBoardHeight = response.boardHeight;
                                    SessionData.currentMatchPower = response.power;
                                    SessionData.currentMatchBoardLetters = response.boardLetters;
                                    SessionData.currentOpponentId = response.opponentId;
                                    SessionData.currentOpponentUsername = response.opponentUsername;
                                    clearMatchmakingTasksOnly();
                                    SessionData.matchmakingActive = false;

                                    debug(1, "Match is ready.");
                                    debug(2, "Match ID=" + response.matchId
                                        + ", Opponent=" + response.opponentUsername
                                        + ", Board=" + response.boardWidth + "x" + response.boardHeight);
                                    debug(3, "Board letters: " + response.boardLetters);

                                    if (callback != null) {
                                        callback.onMatchReady(response);
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(final Throwable t) {
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                debugError(2, "Queue status polling failed.", t);
                            }
                        });
                    }
                });
            }
        }, 0f, 1f);
    }

    private static void clearMatchmakingState() {
        debug(2, "Clearing matchmaking state.");

        clearMatchmakingTasksOnly();
        SessionData.matchmakingActive = false;
        SessionData.currentQueueTicketId = null;
        SessionData.currentMatchId = null;
        SessionData.currentOpponentId = null;
        SessionData.currentOpponentUsername = null;
        SessionData.currentMatchBoardLetters = null;
    }

    private static void clearMatchmakingTasksOnly() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel();
            heartbeatTask = null;
            debug(3, "Heartbeat task cancelled.");
        }

        if (statusPollTask != null) {
            statusPollTask.cancel();
            statusPollTask = null;
            debug(3, "Status polling task cancelled.");
        }

        acknowledgeInFlight = false;
        matchFoundNotified = false;
    }

    private static void stopMatchmakingTasks() {
        debug(3, "Stopping existing matchmaking tasks.");
        clearMatchmakingTasksOnly();
    }

    private static void applyPlayerDataToSession(PlayerData playerData) {
        SessionData.id = playerData.id;
        SessionData.username = playerData.username;

        SessionData.musicEnabled = playerData.musicEnabled;
        SessionData.sfxEnabled = playerData.sfxEnabled;
        SessionData.vibrationEnabled = playerData.vibrationEnabled;
        SessionData.theme = playerData.theme;
        SessionData.mode = playerData.mode;
        SessionData.banAmount = playerData.banAmount;

        SessionData.wins = playerData.wins;
        SessionData.losses = playerData.losses;

        SessionData.currentGamemode = playerData.currentGamemode;
        SessionData.currentBoardWidth = playerData.currentBoardWidth;
        SessionData.currentBoardHeight = playerData.currentBoardHeight;

        SessionData.createdAt = playerData.createdAt;
        SessionData.updatedAt = playerData.updatedAt;

        SessionData.mmr4x4 = playerData.mmr4x4;
        SessionData.mmr4x5 = playerData.mmr4x5;
        SessionData.mmr5x5 = playerData.mmr5x5;

        if (!isBlank(playerData.authToken)) {
            SessionData.authToken = playerData.authToken.trim();
        }
    }

    private static void applySettingsResponseToSession(UpdateSettingsResponse response) {
        SessionData.musicEnabled = response.musicEnabled;
        SessionData.sfxEnabled = response.sfxEnabled;
        SessionData.vibrationEnabled = response.vibrationEnabled;
        SessionData.theme = response.theme;
        SessionData.mode = response.mode;
        SessionData.currentGamemode = response.currentGamemode;
        SessionData.currentBoardWidth = response.currentBoardWidth;
        SessionData.currentBoardHeight = response.currentBoardHeight;
        SessionData.updatedAt = response.updatedAt;
    }

    private static String generateGuestUsername() {
        return "Player-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static Preferences getPreferences() {
        String profile = System.getenv("LETTERLINK_PROFILE");

        if (profile == null || profile.trim().isEmpty()) {
            profile = "Default";
        } else {
            profile = profile.trim();
        }

        String prefsName = "LetterLink-" + profile;
        System.out.println("[BackendHandler] Using preferences: " + prefsName);

        return Gdx.app.getPreferences(prefsName);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static void debug(int level, String message) {
        if (DEBUG >= level) {
            System.out.println("[BackendHandler] " + message);
        }
    }

    private static void debugError(int level, String message, Throwable t) {
        if (DEBUG >= level) {
            System.out.println("[BackendHandler] " + message);
            if (t != null) {
                System.out.println("[BackendHandler] " + t.getClass().getSimpleName() + ": " + t.getMessage());
                if (DEBUG >= 3) {
                    t.printStackTrace();
                }
            }
        }
    }
}
