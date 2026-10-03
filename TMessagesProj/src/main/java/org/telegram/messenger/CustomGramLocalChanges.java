package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;
import org.telegram.tgnet.TLRPC;

public final class CustomGramLocalChanges {

    public static final int STATUS_ORIGINAL = 0;
    public static final int STATUS_STATIC = 1;
    public static final int STATUS_REAL_TIME = 2;

    private static final String PREFS = "customgram_local_changes";

    private CustomGramLocalChanges() {
    }

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String userKey(long userId) {
        return "user_" + userId;
    }

    private static JSONObject loadUser(long userId) {
        String raw = prefs().getString(userKey(userId), null);
        if (raw == null) {
            return new JSONObject();
        }
        try {
            return new JSONObject(raw);
        } catch (Exception ignore) {
            return new JSONObject();
        }
    }

    private static void saveUser(long userId, JSONObject object) {
        prefs().edit().putString(userKey(userId), object.toString()).apply();
    }

    private static void putString(long userId, String key, String value) {
        try {
            JSONObject object = loadUser(userId);
            object.put(key, value == null ? "" : value);
            saveUser(userId, object);
        } catch (Exception ignore) {
        }
    }

    public static boolean hasChanges(long userId) {
        return prefs().contains(userKey(userId));
    }

    public static void clearUser(long userId) {
        prefs().edit().remove(userKey(userId)).apply();
    }

    public static boolean isOwnUser(int currentAccount, TLRPC.User user) {
        return user != null && user.id == UserConfig.getInstance(currentAccount).getClientUserId();
    }

    public static boolean hasFirstNameOverride(long userId) {
        return loadUser(userId).has("first_name");
    }

    public static boolean hasLastNameOverride(long userId) {
        return loadUser(userId).has("last_name");
    }

    public static boolean hasUsernameOverride(long userId) {
        return loadUser(userId).has("username");
    }

    public static boolean hasAboutOverride(long userId) {
        return loadUser(userId).has("about");
    }

    public static void setFirstName(long userId, String value) {
        putString(userId, "first_name", value);
    }

    public static String getFirstName(long userId, String original) {
        JSONObject object = loadUser(userId);
        return object.has("first_name") ? object.optString("first_name", "") : original;
    }

    public static void setLastName(long userId, String value) {
        putString(userId, "last_name", value);
    }

    public static String getLastName(long userId, String original) {
        JSONObject object = loadUser(userId);
        return object.has("last_name") ? object.optString("last_name", "") : original;
    }

    public static void setUsername(long userId, String value) {
        String username = value == null ? "" : value.trim();
        if (username.startsWith("@")) {
            username = username.substring(1);
        }
        putString(userId, "username", username);
    }

    public static String getUsername(long userId, String original) {
        JSONObject object = loadUser(userId);
        return object.has("username") ? object.optString("username", "") : original;
    }

    public static void setAbout(long userId, String value) {
        putString(userId, "about", value);
    }

    public static String getAbout(long userId, String original) {
        JSONObject object = loadUser(userId);
        return object.has("about") ? object.optString("about", "") : original;
    }

    public static void setStatusMode(long userId, int mode) {
        try {
            JSONObject object = loadUser(userId);
            object.put("status_mode", mode);
            saveUser(userId, object);
        } catch (Exception ignore) {
        }
    }

    public static int getStatusMode(long userId) {
        return loadUser(userId).optInt("status_mode", STATUS_ORIGINAL);
    }

    public static void setStatusText(long userId, String value) {
        putString(userId, "status_text", value);
    }

    public static String getStatusText(long userId) {
        return loadUser(userId).optString("status_text", "");
    }

    public static void setStatusTemplate(long userId, String value) {
        putString(userId, "status_template", value);
    }

    public static String getStatusTemplate(long userId) {
        return loadUser(userId).optString("status_template", "в моём сердце в {time}");
    }

    public static String formatStatus(int currentAccount, TLRPC.User user, String originalStatus) {
        if (user == null || isOwnUser(currentAccount, user)) {
            return originalStatus;
        }

        int mode = getStatusMode(user.id);
        if (mode == STATUS_ORIGINAL) {
            return originalStatus;
        }

        if (mode == STATUS_STATIC) {
            String text = getStatusText(user.id).trim();
            return text.isEmpty() ? originalStatus : text;
        }

        if (mode == STATUS_REAL_TIME) {
            String template = getStatusTemplate(user.id).trim();
            if (template.isEmpty()) {
                return originalStatus;
            }

            long timestamp = getExactLastSeenTimestamp(user);
            if (timestamp <= 0) {
                return originalStatus;
            }

            long millis = timestamp * 1000L;
            String time = LocaleController.getInstance().getFormatterDay().format(millis);
            String date = LocaleController.getInstance().getFormatterYearMax().format(millis);
            return template.replace("{time}", time).replace("{date}", date);
        }

        return originalStatus;
    }

    private static long getExactLastSeenTimestamp(TLRPC.User user) {
        if (user == null || user.status == null) {
            return 0;
        }
        int expires = user.status.expires;
        long now = System.currentTimeMillis() / 1000L;
        if (expires > 0 && expires <= now) {
            return expires;
        }
        return 0;
    }
}