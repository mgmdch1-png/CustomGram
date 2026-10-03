package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Lightweight local persistence for CustomGram reminders. */
public final class CustomGramReminders {
    private static final String PREFS = "customgram_reminders";
    private static final String KEY = "items";

    public static final class Item {
        public long id;
        public String text;
        public long triggerAt;
        public int repeatMinutes;
        public boolean enabled;

        public Item(long id, String text, long triggerAt, int repeatMinutes, boolean enabled) {
            this.id = id;
            this.text = text;
            this.triggerAt = triggerAt;
            this.repeatMinutes = repeatMinutes;
            this.enabled = enabled;
        }
    }

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static List<Item> getAll() {
        ArrayList<Item> out = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs().getString(KEY, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                out.add(new Item(
                        o.optLong("id"),
                        o.optString("text"),
                        o.optLong("at"),
                        o.optInt("repeat"),
                        o.optBoolean("enabled", true)
                ));
            }
        } catch (Exception ignore) { }
        return out;
    }

    public static Item get(long id) {
        for (Item item : getAll()) {
            if (item.id == id) return item;
        }
        return null;
    }

    public static void save(Item item) {
        List<Item> items = getAll();
        boolean replaced = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id == item.id) {
                items.set(i, item);
                replaced = true;
                break;
            }
        }
        if (!replaced) items.add(item);
        persist(items);
    }

    public static void remove(long id) {
        List<Item> items = getAll();
        for (int i = items.size() - 1; i >= 0; i--) {
            if (items.get(i).id == id) items.remove(i);
        }
        persist(items);
    }

    /** Restore alarms after reboot/app update. Expired one-shot reminders fire shortly after restore. */
    public static void rescheduleAll() {
        long now = System.currentTimeMillis();
        for (Item item : getAll()) {
            if (!item.enabled) continue;
            if (item.triggerAt <= now) {
                if (item.repeatMinutes > 0) {
                    long step = item.repeatMinutes * 60_000L;
                    long missed = Math.max(1L, ((now - item.triggerAt) / step) + 1L);
                    item.triggerAt += missed * step;
                    save(item);
                } else {
                    item.triggerAt = now + 5_000L;
                    save(item);
                }
            }
            CustomGramReminderScheduler.schedule(item);
        }
    }

    private static void persist(List<Item> items) {
        JSONArray array = new JSONArray();
        try {
            for (Item item : items) {
                JSONObject o = new JSONObject();
                o.put("id", item.id);
                o.put("text", item.text);
                o.put("at", item.triggerAt);
                o.put("repeat", item.repeatMinutes);
                o.put("enabled", item.enabled);
                array.put(o);
            }
        } catch (Exception ignore) { }
        prefs().edit().putString(KEY, array.toString()).apply();
    }

    private CustomGramReminders() { }
}
