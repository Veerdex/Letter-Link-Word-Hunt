package com.grantkoupal.letterlink.backend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Timer;
import com.grantkoupal.letterlink.backend.data.PlayerData;
import com.grantkoupal.letterlink.backend.data.SessionData;

import java.util.UUID;

public class BackendHandler {
    private static final BackendConnect backend = new BackendConnect();
    private static final int DEBUG = 3;

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
        final Preferences prefs = Gdx.app.getPreferences("LetterLink");

        String savedPlayerId = prefs.getString("playerId", "");
        String savedAuthToken = prefs.getString("authToken", "");

        if (savedPlayerId != null) {
            savedPlayerId = savedPlayerId.trim();
        }
        if (savedAuthToken != null) {
            savedAuthToken = savedAuthToken.trim();
        }

        if (savedPlayerId == null || savedPlayerId.isEmpty()) {
            registerFreshPlayer(prefs, callback);
            return;
        }

        SessionData.id = savedPlayerId;

        if (savedAuthToken != null && !savedAuthToken.isEmpty()) {
            SessionData.authToken = savedAuthToken;
            loadPlayerData(savedPlayerId, callback);
            return;
        }

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

                        if (DEBUG > 0) {
                            System.out.println("Bootstrapped legacy session successfully.");
                            System.out.println("ID: " + SessionData.id);
                            System.out.println("Username: " + SessionData.username);
                        }

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
                        System.out.println("Bootstrap failed with stale or missing local playerId. Registering a fresh local player.");
                        registerFreshPlayer(prefs, callback);
                    }
                });
            }
        });
    }

    private static void registerFreshPlayer(final Preferences prefs, final StartupCallback callback) {
        final String username = generateGuestUsername();

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

                        if (DEBUG > 0) {
                            System.out.println("Registered new player:");
                            System.out.println("ID: " + response.id);
                            System.out.println("Username: " + response.username);
                        }

                        loadPlayerData(response.id, callback);
                    }
                });
            }

            @Override
            public void onFailure(final Throwable t) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        t.printStackTrace();

                        if (callback != null) {
                            callback.onFailure(t);
                        }
                    }
                });
            }
        });
    }

    private static void loadPlayerData(final String playerId, final StartupCallback callback) {
        backend.getPlayerData(playerId, new BackendConnect.PlayerDataCallback() {
            @Override
            public void onSuccess(final PlayerData playerData) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        applyPlayerDataToSession(playerData);

                        if (DEBUG > 0) {
                            System.out.println("Loaded player data:");
                            System.out.println("ID: " + playerData.id);
                            System.out.println("Username: " + playerData.username);
                        }
                        if (DEBUG > 1) {
                            System.out.println("Theme: " + playerData.theme);
                            System.out.println("Mode: " + playerData.mode);
                            System.out.println("Gamemode: " + playerData.currentGamemode);
                        }
                        if (DEBUG > 2) {
                            System.out.println("Music: " + playerData.musicEnabled);
                            System.out.println("SFX: " + playerData.sfxEnabled);
                            System.out.println("Vibration: " + playerData.vibrationEnabled);
                            System.out.println("Ban Amount: " + playerData.banAmount);
                            System.out.println("4x4 MMR: " + playerData.mmr4x4);
                            System.out.println("4x5 MMR: " + playerData.mmr4x5);
                            System.out.println("5x5 MMR: " + playerData.mmr5x5);
                            System.out.println("Board Width: " + playerData.currentBoardWidth);
                            System.out.println("Board Height: " + playerData.currentBoardHeight);
                        }

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
                        t.printStackTrace();

                        if (callback != null) {
                            callback.onFailure(t);
                        }
                    }
                });
            }
        });
    }

    public static void refreshBanAmount(final SimpleCallback callback) {
        if (SessionData.id == null || SessionData.id.trim().isEmpty()) {
            RuntimeException error = new RuntimeException("SessionData.id is missing");
            if (callback != null) {
                callback.onFailure(error);
            }
            return;
        }

        backend.getBanAmount(SessionData.id, new BackendConnect.BanAmountCallback() {
            @Override
            public void onSuccess(final BanAmountResponse response) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        SessionData.banAmount = response.banAmount;
                        SessionData.updatedAt = response.updatedAt;

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
                        if (callback != null) {
                            callback.onFailure(t);
                        }
                    }
                });
            }
        });
    }

    public static void syncSessionSettingsToServer(final SimpleCallback callback) {
        if (SessionData.id == null || SessionData.id.trim().isEmpty()) {
            RuntimeException error = new RuntimeException("SessionData.id is missing");
            if (callback != null) {
                callback.onFailure(error);
            }
            return;
        }

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

                            if (DEBUG > 1) {
                                System.out.println("Session settings synced to server.");
                                System.out.println("Theme: " + SessionData.theme);
                                System.out.println("Mode: " + SessionData.mode);
                                System.out.println("Gamemode: " + SessionData.currentGamemode);
                                System.out.println("Music: " + SessionData.musicEnabled);
                                System.out.println("SFX: " + SessionData.sfxEnabled);
                                System.out.println("Vibration: " + SessionData.vibrationEnabled);
                                System.out.println("Board Width: " + SessionData.currentBoardWidth);
                                System.out.println("Board Height: " + SessionData.currentBoardHeight);
                                System.out.println("Updated At: " + SessionData.updatedAt);
                            }

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
                            t.printStackTrace();

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
        if (SessionData.id == null || SessionData.id.trim().isEmpty()) {
            RuntimeException error = new RuntimeException("SessionData.id is missing");
            if (callback != null) {
                callback.onFailure(error);
            }
            return;
        }

        stopMatchmakingTasks();

        final int power = SessionData.matchPower <= 0 ? 4 : SessionData.matchPower;

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
        if (SessionData.currentQueueTicketId == null || SessionData.currentQueueTicketId.trim().isEmpty()) {
            RuntimeException error = new RuntimeException("No active queue ticket");
            if (callback != null) {
                callback.onFailure(error);
            }
            return;
        }

        backend.cancelQueue(SessionData.currentQueueTicketId, new BackendConnect.CancelQueueCallback() {
            @Override
            public void onSuccess(final CancelQueueResponse response) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (response.cancelled) {
                            clearMatchmakingState();
                            if (callback != null) {
                                callback.onCancelled();
                            }
                            return;
                        }

                        SessionData.currentMatchId = response.matchId;
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
                        if (callback != null) {
                            callback.onFailure(t);
                        }
                    }
                });
            }
        });
    }

    public static void abandonCurrentMatch(final SimpleCallback callback) {
        if (SessionData.currentMatchId == null || SessionData.currentMatchId.trim().isEmpty()) {
            RuntimeException error = new RuntimeException("No active match");
            if (callback != null) {
                callback.onFailure(error);
            }
            return;
        }

        backend.abandonMatch(SessionData.currentMatchId, new BackendConnect.AbandonMatchCallback() {
            @Override
            public void onSuccess(final AbandonMatchResponse response) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        SessionData.banAmount = response.banAmount;
                        SessionData.currentMatchId = null;
                        SessionData.matchmakingActive = false;
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
                        if (callback != null) {
                            callback.onFailure(t);
                        }
                    }
                });
            }
        });
    }

    private static void startHeartbeat(final String ticketId) {
        heartbeatTask = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                backend.sendQueueHeartbeat(ticketId, new BackendConnect.QueueHeartbeatCallback() {
                    @Override
                    public void onSuccess(QueueHeartbeatResponse response) {
                        if (DEBUG > 2) {
                            System.out.println("Queue heartbeat OK: " + response.ticketId + " | " + response.ticketStatus);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if (DEBUG > 1) {
                            System.out.println("Queue heartbeat failed: " + t.getMessage());
                        }
                    }
                });
            }
        }, 0f, 5f);
    }

    private static void startStatusPolling(final String ticketId, final MatchmakingCallback callback) {
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

                                if ("CANCELLED".equals(response.ticketStatus) || "EXPIRED".equals(response.ticketStatus)) {
                                    clearMatchmakingState();
                                    if (callback != null) {
                                        callback.onCancelled();
                                    }
                                    return;
                                }

                                if (response.matchId != null && "MATCH_FOUND".equals(response.matchStatus)) {
                                    if (!matchFoundNotified) {
                                        matchFoundNotified = true;
                                        if (callback != null) {
                                            callback.onMatchFound(response);
                                        }
                                    }

                                    if (!response.playerAcknowledged && !acknowledgeInFlight) {
                                        acknowledgeInFlight = true;
                                        backend.acknowledgeMatch(response.matchId, new BackendConnect.AcknowledgeMatchCallback() {
                                            @Override
                                            public void onSuccess(final AcknowledgeMatchResponse ackResponse) {
                                                Gdx.app.postRunnable(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        acknowledgeInFlight = false;
                                                        SessionData.currentMatchId = ackResponse.matchId;
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFailure(final Throwable t) {
                                                Gdx.app.postRunnable(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        acknowledgeInFlight = false;
                                                        if (DEBUG > 1) {
                                                            t.printStackTrace();
                                                        }
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
                                if (DEBUG > 1) {
                                    System.out.println("Queue status polling failed: " + t.getMessage());
                                }
                            }
                        });
                    }
                });
            }
        }, 0f, 1f);
    }

    private static void clearMatchmakingState() {
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
        }
        if (statusPollTask != null) {
            statusPollTask.cancel();
            statusPollTask = null;
        }
        acknowledgeInFlight = false;
        matchFoundNotified = false;
    }

    private static void stopMatchmakingTasks() {
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

        if (playerData.authToken != null && !playerData.authToken.trim().isEmpty()) {
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
}
