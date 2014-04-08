package br.com.cacira.portariamobile;

import java.util.Map;

import android.app.Application;
import android.app.Notification;
import android.support.v4.app.NotificationCompat;

//import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;
import com.urbanairship.push.BasicPushNotificationBuilder;
import com.urbanairship.push.PushManager;
import com.urbanairship.push.PushNotificationBuilder;

public class PortariaMobileApplication extends Application {
	
	   @Override
	   public void onCreate() {
		   
		   // Urban Airship Notifications
		   UAirship.takeOff(this);
		   
		   BasicPushNotificationBuilder nb = new BasicPushNotificationBuilder() {
			    @Override
			    public Notification buildNotification(String alert,
			            Map<String, String> extras) {
			        Notification notification = super.buildNotification(alert,
			                extras);
			        // The icon displayed in the status bar
			        notification.icon = R.drawable.ic_stat_icon;
			        // The icon displayed within the notification content
			        notification.contentView.setImageViewResource(
			                android.R.id.icon, R.drawable.ic_stat_icon);
			        return notification;
			    }
		   };
		   
//		   PushManager.enablePush();
		   PushManager.shared().setIntentReceiver(IntentReceiver.class);
		   PushManager.shared().setNotificationBuilder(nb);
	   }
	
}
