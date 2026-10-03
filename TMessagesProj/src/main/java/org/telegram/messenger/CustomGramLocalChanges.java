package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;
import org.telegram.tgnet.TLRPC;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class CustomGramLocalChanges {

    public static final int STATUS_ORIGINAL = 0;
    public static final int STATUS_STATIC = 1;
    public static final int STATUS_REAL_TIME = 2;

    private static final String PREFS = "customgram_local_changes";

    private CustomGramLocalChanges() {
    }

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
        );
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
        prefs()
                .edit()
                .putString(userKey(userId), object.toString())
                .apply();
    }

    public static boolean hasChanges(long userId) {
        return prefs().contains(userKey(userId));
    }

    public static void clearUser(long userId) {
        prefs()
                .edit()
                .remove(userKey(userId))
                .apply();
    }

    public static boolean isOwnUser(int currentAccount, TLRPC.User user) {
        if (user == null) {
            return false;
        }

        try {
            return user.id == UserConfig.getInstance(currentAccount).getClientUserId();
        } catch (Exception ignore) {
            return user.self;
        }
    }

    // ------------------------------------------------------------
    // Имя
    // ------------------------------------------------------------

    public static void setFirstName(long userId, String value) {
        try {
            JSONObject object = loadUser(userId);
            object.put("first_name", value == null ? "" : value);
            saveUser(userId, object);
        } catch (Exception ignore) {
        }
    }

    public static String getFirstName(long userId, String original) {
        JSONObject object = loadUser(userId);

        if (!object.has("first_name")) {
            return original;
        }

        return object.optString("first_name", original);
    }

    // ------------------------------------------------------------
    // Фамилия
    // ------------------------------------------------------------

    public static void setLastName(long userId, String value) {
        try {
            JSONObject object = loadUser(userId);
            object.put("last_name", value == null ? "" : value);
            saveUser(userId, object);
        } catch (Exception ignore) {
        }
    }

    public static String getLastName(long userId, String original) {
        JSONObject object = loadUser(userId);

        if (!object.has("last_name")) {
            return original;
        }

        return object.optString("last_name", original);
    }

    // ------------------------------------------------------------
    // Username
    // ------------------------------------------------------------

    public static void setUsername(long userId, String value) {
        try {
            JSONObject object = loadUser(userId);

            String username = value == null ? "" : value.trim();

            if (username.startsWith("@")) {
                username = username.substring(1);
            }

            object.put("username", username);
            saveUser(userId, object);
        } catch (Exception ignore) {
        }
    }

    public static String getUsername(long userId, String original) {
        JSONObject object = loadUser(userId);

        if (!object.has("username")) {
            return original;
        }

        return object.optString("username", original);
    }

    // ------------------------------------------------------------
    // Описание профиля
    // ------------------------------------------------------------

    public static void setAbout(long userId, String value) {
        try {
            JSONObject object = loadUser(userId);
            object.put("about", value == null ? "" : value);
            saveUser(userId, object);
        } catch (Exception ignore) {
        }
    }

    public static String getAbout(long userId, String original) {
        JSONObject object = loadUser(userId);

        if (!object.has("about")) {
            return original;
        }

        return object.optString("about", original);
    }

    // ------------------------------------------------------------
    // Статус
    // ------------------------------------------------------------

    public static void setStatusMode(long userId, int mode) {
        try {
            JSONObject object = loadUser(userId);
            object.put("status_mode", mode);
            saveUser(userId, object);
        } catch (Exception ignore) {
        }
    }

    public static int getStatusMode(long userId) {
        return loadUser(userId).optInt(
                "status_mode",
                STATUS_ORIGINAL
        );
    }

    public static void setStatusText(long userId, String value) {
        try {
            JSONObject object = loadUser(userId);
            object.put("status_text", value == null ? "" : value);
            saveUser(userId, object);
        } catch (Exception ignore) {
        }
    }

    public static String getStatusText(long userId) {
        return loadUser(userId).optString(
                "status_text",
                ""
        );
    }

    public static void setStatusTemplate(long userId, String value) {
        try {
            JSONObject object = loadUser(userId);
            object.put(
                    "status_template",
                    value == null ? "" : value
            );
            saveUser(userId, object);
        } catch (Exception ignore) {
        }
    }

    public static String getStatusTemplate(long userId) {
        return loadUser(userId).optString(
                "status_template",
                "был(а) здесь в {time}"
        );
    }

    public static String formatStatus(
            int currentAccount,
            TLRPC.User user,
            String originalStatus
    ) {
        if (user == null) {
            return originalStatus;
        }

        if (isOwnUser(currentAccount, user)) {
            return originalStatus;
        }

        long userId = user.id;

        int mode = getStatusMode(userId);

        if (mode == STATUS_ORIGINAL) {
            return originalStatus;
        }

        if (mode == STATUS_STATIC) {
            String text = getStatusText(userId);

            if (text == null || text.trim().isEmpty()) {
                return originalStatus;
            }

            return text;
        }

        if (mode == STATUS_REAL_TIME) {
            String template = getStatusTemplate(userId);

            if (template == null || template.trim().isEmpty()) {
                return originalStatus;
            }

            long timestamp = getRealTelegramStatusTime(user);

            if (timestamp <= 0) {
                return originalStatus;
            }

            Date date = new Date(timestamp * 1000L);

            String time = new SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
            ).format(date);

            String day = new SimpleDateFormat(
                    "dd.MM.yyyy",
                    Locale.getDefault()
            ).format(date);

            return template
                    .replace("{time}", time)
                    .replace("{date}", day);
        }

        return originalStatus;
    }

    private static long getRealTelegramStatusTime(TLRPC.User user) {
        if (user == null || user.status == null) {
            return 0;
        }

        /*
         * Для точного last seen Telegram хранит timestamp
         * в status.expires.
         *
         * Для "был недавно", "на этой неделе" и т.п.
         * точного времени Telegram не отдаёт —
         * поэтому подделывать время здесь не будем.
         */
        int expires = user.status.expires;

        if (expires > 0) {
            long now = System.currentTimeMillis() / 1000L;

            // Если expires в будущем — пользователь сейчас online.
            // Это не last seen.
            if (expires > now) {
                return 0;
            }

            return expires;
        }

        return 0;
    }

    // ------------------------------------------------------------
    // Предпросмотр статуса
    // ------------------------------------------------------------

    public static String previewStatus(
            int currentAccount,
            TLRPC.User user,
            String originalStatus
    ) {
        return formatStatus(
                currentAccount,
                user,
                originalStatus
        );
    }
}
