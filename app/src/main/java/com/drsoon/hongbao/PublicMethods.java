package com.drsoon.hongbao;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

/**
 * Created by dekunt on 15/9/3.
 */
public class PublicMethods
{

    /**
     *  @return is going to unlock
     */
    public static boolean wakeUpAndUnlock(Context context, final Service service)
    {
        PowerManager mgr = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        if (!mgr.isScreenOn())
        {
            PowerManager.WakeLock wakeLock = mgr.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                    PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "bright");
            wakeLock.acquire();
            wakeLock.release();
        }
        if (((KeyguardManager)context.getSystemService(Activity.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode())
        {
            Intent intent = new Intent(service, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            service.startActivity(intent);
            return true;
        }
        return false;
    }


    public static void returnHome(Service service)
    {
        Intent localIntent = new Intent("android.intent.action.MAIN");
        localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.addCategory("android.intent.category.HOME");
        service.startActivity(localIntent);
    }
}
