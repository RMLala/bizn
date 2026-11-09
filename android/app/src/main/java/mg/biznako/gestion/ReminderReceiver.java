package mg.biznako.gestion;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
public class ReminderReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        NotificationManager nm = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(new NotificationChannel("biznako_reminders", "Rappels Biznako", NotificationManager.IMPORTANCE_DEFAULT));
        PendingIntent open = PendingIntent.getActivity(c,0,new Intent(c,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        try { nm.notify(1001,new NotificationCompat.Builder(c,"biznako_reminders").setSmallIcon(R.drawable.ic_notification).setContentTitle(i.getStringExtra("title")).setContentText(i.getStringExtra("body")).setContentIntent(open).setAutoCancel(true).build()); } catch (SecurityException ignored) { /* Permission revoked after scheduling: do not post. */ }
    }
}
