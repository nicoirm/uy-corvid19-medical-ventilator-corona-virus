package utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.gonza.medicalventilator.MainActivity;
import com.gonza.medicalventilator.R;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationUtils {









	//=====================================okAG_CODE=============================================

	public static final int NOTIF_ID_WORKING = 3;





	public static Notification getForeground(String textTitle, String textContent,Context ctx) {
		createNotificationChannel(ctx);
		PendingIntent pIntent = PendingIntent
		  .getActivity(ctx, 0, new Intent(ctx, MainActivity.class),
			PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx,ctx.getPackageName())
		  .setSmallIcon(R.drawable.ic_stat_name).setContentTitle(textTitle).setContentText(textContent)
		  .setContentIntent(pIntent).setOnlyAlertOnce(true)
		  .setPriority(NotificationCompat.PRIORITY_DEFAULT);
		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);

		// notificationId is a unique int for each notification that you must define
		Notification not = builder.build();
		notificationManager.notify(NotificationUtils.NOTIF_ID_WORKING, not);
		// notificationId is a unique int for each notification that you must define
		return not;

	}





	private static void createNotificationChannel(Context ctx) {
		// Create the NotificationChannel, but only on API 26+ because
		// the NotificationChannel class is new and not in the support library
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = "Breathing";
			String description = "Shown when the app is breathing";
			int importance = NotificationManager.IMPORTANCE_LOW;
			NotificationChannel channel =
			  new NotificationChannel(ctx.getPackageName(),
				name, importance);
			channel.setDescription(description);

			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this
			NotificationManager notificationManager =
			  ctx.getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}
	}



}
