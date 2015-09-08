package com.drsoon.hongbao;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by dekunt on 15/9/3.
 */
public class AAAService extends AccessibilityService
{
    public static boolean needAutoReturnHome = true;
    public static boolean needAutoUnlock = true;
    public static Runnable runnableOnWakeup;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event)
    {
        if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED)
        {
            Notification notification = (Notification)event.getParcelableData();
            if (event.getPackageName().toString().equals("com.tencent.mm")
                    && !event.getText().isEmpty()
                    && event.getText().get(0).toString().contains("[微信红包]"))
            {
                onWeiXinNotification(notification);
            }
            else if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                    && event.getPackageName().toString().equals("com.tencent.mobileqq")
                    && !event.getText().isEmpty()
                    && event.getText().get(0).toString().contains("[QQ红包]"))
            {
                onQQNotification(notification);
            }
        }
        else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                && event.getPackageName().toString().equals("com.tencent.mm"))
        {
                onWeiXinWindowChanged();
        }
    }


    @Override
    public void onInterrupt()
    {
    }


    private void onWeiXinWindowChanged()
    {
        WeiXinTask.openTarget(this);
    }

    private void onWeiXinNotification(final Notification notification)
    {
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    notification.contentIntent.send();
                    WeiXinTask.findTarget(AAAService.this);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        };
        if (wakeUpAndUnlock())
            runnableOnWakeup = runnable;
        else
            runnable.run();
    }


    private void onQQNotification(final Notification notification)
    {
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    notification.contentIntent.send();
                    QQTask.findTarget(AAAService.this);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        };
        if (wakeUpAndUnlock())
            runnableOnWakeup = runnable;
        else
            runnable.run();
    }


    /**
     *  @return is going to unlock
     */
    private boolean wakeUpAndUnlock()
    {
        return needAutoUnlock && PublicMethods.wakeUpAndUnlock(getApplicationContext(), this);
    }
}
