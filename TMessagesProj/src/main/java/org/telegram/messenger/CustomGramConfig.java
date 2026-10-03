package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.util.Iterator;

/**
 * Single source of truth for CustomGram settings.
 * A setting must not be exposed in UI until the matching runtime hook exists.
 */
public final class CustomGramConfig {
    private static final String PREFS = "customgram_settings_v2";

    public static final String KEY_CLEAN_ACTION_BAR = "clean_action_bar";
    public static final String KEY_HIDE_CHAT_CALL = "hide_chat_call";
    public static final String KEY_MAIN_TITLE = "main_title";
    public static final String KEY_DELETE_EFFECT = "delete_effect";

    public static final int DELETE_EFFECT_OFF = 0;
    public static final int DELETE_EFFECT_TNT = 1;
    public static final int DELETE_EFFECT_PORTAL = 2;
    public static final int DELETE_EFFECT_ENDERMAN = 3;
    public static final int DELETE_EFFECT_FIRE = 4;
    public static final int DELETE_EFFECT_RANDOM = 5;

    private CustomGramConfig() {}

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean cleanActionBar() {
        return prefs().getBoolean(KEY_CLEAN_ACTION_BAR, true);
    }

    public static void setCleanActionBar(boolean value) {
        prefs().edit().putBoolean(KEY_CLEAN_ACTION_BAR, value).apply();
    }

    public static boolean hideChatCallButton() {
        return prefs().getBoolean(KEY_HIDE_CHAT_CALL, true);
    }

    public static void setHideChatCallButton(boolean value) {
        prefs().edit().putBoolean(KEY_HIDE_CHAT_CALL, value).apply();
    }

    public static String getMainTitle() {
        String title = prefs().getString(KEY_MAIN_TITLE, "CustomGram");
        if (title == null) return "CustomGram";
        title = title.trim();
        return title.isEmpty() ? "CustomGram" : title;
    }

    public static void setMainTitle(String value) {
        String title = value == null ? "" : value.trim();
        prefs().edit().putString(KEY_MAIN_TITLE, title.isEmpty() ? "CustomGram" : title).apply();
    }

    public static int getDeleteEffect() {
        int value = prefs().getInt(KEY_DELETE_EFFECT, DELETE_EFFECT_OFF);
        return value < DELETE_EFFECT_OFF || value > DELETE_EFFECT_RANDOM ? DELETE_EFFECT_OFF : value;
    }

    public static void setDeleteEffect(int value) {
        if (value < DELETE_EFFECT_OFF || value > DELETE_EFFECT_RANDOM) value = DELETE_EFFECT_OFF;
        prefs().edit().putInt(KEY_DELETE_EFFECT, value).apply();
    }

    /** True when CustomGram owns the delete animation instead of Telegram's normal/Thanos animation. */
    public static boolean hasCustomDeleteEffect() {
        return getDeleteEffect() != DELETE_EFFECT_OFF;
    }

    public static String getDeleteEffectName() {
        switch (getDeleteEffect()) {
            case DELETE_EFFECT_TNT: return "TNT";
            case DELETE_EFFECT_PORTAL: return "Портал";
            case DELETE_EFFECT_ENDERMAN: return "Эндермен";
            case DELETE_EFFECT_FIRE: return "Огонь";
            case DELETE_EFFECT_RANDOM: return "Случайно";
            default: return "Выключено";
        }
    }

    public static boolean matchesSearch(String query) {
        if (query == null) return false;
        String q = query.trim().toLowerCase();
        if (q.isEmpty()) return false;
        return "customgram".contains(q)
                || "кастомграм".contains(q)
                || "чистая шапка".contains(q)
                || "кнопка звонка".contains(q)
                || "заголовок главного экрана".contains(q)
                || "локальные изменения".contains(q)
                || "умные напоминания".contains(q)
                || "minecraft майнкрафт tnt портал эндермен удаление сообщений".contains(q)
                || "бэкап настройки резервная копия".contains(q);
    }

    /** Export only CustomGram-owned preferences. Telegram account/session data is never included. */
    public static String exportJson() {
        try {
            JSONObject root = new JSONObject();
            root.put("format", "CustomGramSettings");
            root.put("version", 2);
            JSONObject values = new JSONObject();
            for (String key : prefs().getAll().keySet()) {
                Object value = prefs().getAll().get(key);
                if (value instanceof Boolean || value instanceof Integer || value instanceof Long || value instanceof Float || value instanceof String) {
                    values.put(key, value);
                }
            }
            root.put("values", values);
            return root.toString(2);
        } catch (Exception e) {
            FileLog.e(e);
            return "";
        }
    }

    public static boolean importJson(String raw) {
        if (raw == null || raw.trim().isEmpty()) return false;
        try {
            JSONObject root = new JSONObject(raw);
            if (!"CustomGramSettings".equals(root.optString("format"))) return false;
            JSONObject values = root.optJSONObject("values");
            if (values == null) return false;
            SharedPreferences.Editor editor = prefs().edit();
            Iterator<String> keys = values.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = values.get(key);
                if (value instanceof Boolean) editor.putBoolean(key, (Boolean) value);
                else if (value instanceof Integer) editor.putInt(key, (Integer) value);
                else if (value instanceof Long) editor.putLong(key, (Long) value);
                else if (value instanceof Double) editor.putFloat(key, ((Double) value).floatValue());
                else if (value instanceof String) editor.putString(key, (String) value);
            }
            editor.apply();
            return true;
        } catch (Exception e) {
            FileLog.e(e);
            return false;
        }
    }
}
