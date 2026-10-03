package org.telegram.messenger;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class CustomGramReminderReceiver extends BroadcastReceiver {
    public static final String CHANNEL = "customgram_reminders";
    public static final String EXTRA_ID = "id";
    public static final String EXTRA_TEXT = "text";

    @Override public void onReceive(Context context, Intent intent) {
        long id=intent.getLongExtra(EXTRA_ID,System.currentTimeMillis()); String text=intent.getStringExtra(EXTRA_TEXT); if(text==null||text.trim().isEmpty())text="Напоминание";
        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=26){NotificationChannel channel=new NotificationChannel(CHANNEL,"Напоминания CustomGram",NotificationManager.IMPORTANCE_HIGH);channel.setDescription("Умные локальные напоминания CustomGram");nm.createNotificationChannel(channel);}
        Intent open=context.getPackageManager().getLaunchIntentForPackage(context.getPackageName()); PendingIntent content=null;if(open!=null)content=PendingIntent.getActivity(context,(int)id,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Intent doneIntent=new Intent(context,CustomGramReminderActionReceiver.class).setAction(CustomGramReminderActionReceiver.ACTION_DONE).putExtra(EXTRA_ID,id); PendingIntent done=PendingIntent.getBroadcast(context,(int)(id^0x31),doneIntent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Intent snoozeIntent=new Intent(context,CustomGramReminderActionReceiver.class).setAction(CustomGramReminderActionReceiver.ACTION_SNOOZE).putExtra(EXTRA_ID,id).putExtra(EXTRA_TEXT,text); PendingIntent snooze=PendingIntent.getBroadcast(context,(int)(id^0x57),snoozeIntent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder b=new NotificationCompat.Builder(context,CHANNEL).setSmallIcon(R.drawable.notification).setContentTitle("CustomGram").setContentText(text).setStyle(new NotificationCompat.BigTextStyle().bigText(text)).setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH).addAction(0,"Выполнено",done).addAction(0,"Отложить на 10 мин",snooze);if(content!=null)b.setContentIntent(content);nm.notify((int)id,b.build());
        for(CustomGramReminders.Item item:CustomGramReminders.getAll()){if(item.id==id&&item.enabled&&item.repeatMinutes>0){item.triggerAt=System.currentTimeMillis()+item.repeatMinutes*60_000L;CustomGramReminders.save(item);CustomGramReminderScheduler.schedule(item);break;}}
    }
}
