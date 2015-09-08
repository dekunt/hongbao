package com.drsoon.hongbao;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by dekunt on 15/9/3.
 */
public class QQTask
{
    private static final long SPLASH_TIME = 3000;
    private static final long FINDING_TIMEOUT = 5000;

    private static long currentTaskId;

    private AccessibilityService mService;
    private long mTaskId;
    private boolean maybeQQTarget;
    private boolean mEnteredChatUI;


    // Find QQ hongBao and open
    public static void findTarget(AccessibilityService service)
    {
        new QQTask(service).findTarget();
    }

    private QQTask(AccessibilityService service)
    {
        this.mService = service;
        this.mTaskId = System.currentTimeMillis();
        this.maybeQQTarget = false;
        this.mEnteredChatUI = false;
        currentTaskId = this.mTaskId;
    }

    private boolean isCurrentTask()
    {
        return this.mTaskId == currentTaskId ;
    }

    private boolean isTimeout()
    {
        return (this.mTaskId + FINDING_TIMEOUT) < System.currentTimeMillis();
    }

    private boolean isSplashTimeout()
    {
        return (this.mTaskId + SPLASH_TIME) < System.currentTimeMillis();
    }

    // Find target with runnable
    private void findTarget()
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                if (!isCurrentTask())
                    return;

                AccessibilityNodeInfo nodeInfo = mService.getRootInActiveWindow();
                if (!mEnteredChatUI)
                {
                    if (isChatFragment(nodeInfo)) {
                        mEnteredChatUI = true;
                        mTaskId = System.currentTimeMillis();
                        currentTaskId = mTaskId;
                    }
                    else if (isSplashTimeout()) {
                        PublicMethods.returnHome(mService);
                        return;
                    }
                    else {
                        findTarget();
                        return;
                    }
                }

                if (isTimeout())
                {
                    if (AAAService.needAutoReturnHome)
                        PublicMethods.returnHome(mService);
                    return;
                }

                if (!openTargetLoop(nodeInfo))
                    findTarget();
            }
        };
        new Handler().postDelayed(runnable, 100L);
    }


    private boolean isChatFragment(AccessibilityNodeInfo nodeInfo)
    {
        if (nodeInfo == null || !nodeInfo.getPackageName().toString().equals("com.tencent.mobileqq"))
            return false;
        int count = nodeInfo.getChildCount();
        if (count > 0)
        {
            if (count >= 4 && nodeInfo.getClassName().toString().equals("android.widget.FrameLayout")
                    && nodeInfo.getChild(count - 4).getClassName().toString().equals("android.widget.ImageButton")
                    && nodeInfo.getChild(count - 3).getClassName().toString().equals("android.widget.ImageButton")
                    && nodeInfo.getChild(count - 2).getClassName().toString().equals("android.widget.EditText")
                    && nodeInfo.getChild(count - 1).getClassName().toString().equals("android.widget.Button"))
            {
                return true;
            }
            for (int i = count - 1; i >= 0; i--)
            {
                if (isChatFragment(nodeInfo.getChild(i)))
                    return true;
            }
        }
        return false;
    }

    // Open QQ target loop
    private boolean openTargetLoop(AccessibilityNodeInfo nodeInfo)
    {
        if (nodeInfo == null)
            return false;
        if (nodeInfo.getChildCount() == 0)
        {
            if (maybeQQTarget)
            {
                maybeQQTarget = false;
                if (nodeInfo.getClassName().toString().equals("android.widget.TextView")
                        && nodeInfo.getParent().getClassName().toString().equals("android.widget.RelativeLayout"))
                {
                    nodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return true;
                }
            }
            CharSequence nodeText = nodeInfo.getText();
            maybeQQTarget = nodeText != null && nodeText.toString().equals("赶紧点击拆开吧")
                    && nodeInfo.getParent().getClassName().toString().equals("android.widget.RelativeLayout");
            return false;
        }

        for (int i = -1 + nodeInfo.getChildCount(); i >= 0; i--)
        {
            if (openTargetLoop(nodeInfo.getChild(i)))
                return true;
        }
        return false;
    }
}
