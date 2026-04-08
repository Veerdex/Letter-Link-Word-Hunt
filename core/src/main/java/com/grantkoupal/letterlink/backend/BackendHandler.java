package com.grantkoupal.letterlink.backend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.grantkoupal.letterlink.backend.data.PlayerData;
import com.grantkoupal.letterlink.backend.data.SessionData;

import java.util.UUID;

public class BackendHandler {
    private static final BackendConnect backend = new BackendConnect();
    private static final int DEBUG = 3;

    public interface StartupCallback {
        void onSuccess();
        void onFailure(Throwable t);
    }

    public interface SimpleCallback {
        void onSuccess();
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
                            System.out.println("Gamemode: " + playerData.currentGamemode);
                        }
                        if (DEBUG > 2) {
                            System.out.println("Music: " + playerData.musicEnabled);
                            System.out.println("SFX: " + playerData.sfxEnabled);
                            System.out.println("Vibration: " + playerData.vibrationEnabled);
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

    private static void applyPlayerDataToSession(PlayerData playerData) {
        SessionData.id = playerData.id;
        SessionData.username = playerData.username;

        SessionData.musicEnabled = playerData.musicEnabled;
        SessionData.sfxEnabled = playerData.sfxEnabled;
        SessionData.vibrationEnabled = playerData.vibrationEnabled;
        SessionData.theme = playerData.theme;

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
        SessionData.currentGamemode = response.currentGamemode;
        SessionData.currentBoardWidth = response.currentBoardWidth;
        SessionData.currentBoardHeight = response.currentBoardHeight;
        SessionData.updatedAt = response.updatedAt;
    }

    private static String generateGuestUsername() {
        return "Player-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
