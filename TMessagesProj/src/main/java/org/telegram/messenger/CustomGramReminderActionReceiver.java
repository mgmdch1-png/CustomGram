package org.telegram.messenger;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CustomGramReminderActionReceiver extends BroadcastReceiver {
    public static final String ACTION_DONE = "org.telegram.customgram.REMINDER_DONE";
    public static final String ACTION_SNOOZE = "org.telegram.customgram.REMINDER_SNOOZE";

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(CustomGramReminderReceiver.EXTRA_ID, 0L);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel((int) id);
        if (ACTION_DONE.equals(intent.getAction())) {
            CustomGramReminders.remove(id);
            CustomGramReminderScheduler.cancel(id);
        } else if (ACTION_SNOOZE.equals(intent.getAction())) {
            String text = intent.getStringExtra(CustomGramReminderReceiver.EXTRA_TEXT);
            CustomGramReminders.Item item = new CustomGramReminders.Item(id, text, System.currentTimeMillis() + 10 * 60_000L, 0, true);
            CustomGramReminders.save(item);
            CustomGramReminderScheduler.schedule(item);
        }
    }
}
