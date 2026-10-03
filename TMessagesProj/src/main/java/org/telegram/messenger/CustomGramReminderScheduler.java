package org.telegram.messenger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public final class CustomGramReminderScheduler {
    public static void schedule(CustomGramReminders.Item item) {
        Context context = ApplicationLoader.applicationContext;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = pending(context, item.id, item.text);
        long at = Math.max(System.currentTimeMillis() + 1000L, item.triggerAt);
        if (Build.VERSION.SDK_INT >= 23) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, at, pi);
        }
    }

    public static void cancel(long id) {
        Context context = ApplicationLoader.applicationContext;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pending(context, id, ""));
    }

    private static PendingIntent pending(Context context, long id, String text) {
        Intent i = new Intent(context, CustomGramReminderReceiver.class)
                .putExtra(CustomGramReminderReceiver.EXTRA_ID, id)
                .putExtra(CustomGramReminderReceiver.EXTRA_TEXT, text);
        return PendingIntent.getBroadcast(context, (int) id, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private CustomGramReminderScheduler() { }
}
