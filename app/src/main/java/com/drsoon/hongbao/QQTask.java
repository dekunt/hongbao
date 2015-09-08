package com.drsoon.hongbao;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by dekunt on 15/9/3.
 */
public class QQTask
{
    private static final long FINDING_TIMEOUT = 5000;

    private static long currentTaskId;

    private long mTaskId;
    private AccessibilityService mService;
    private boolean maybeQQTarget;


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
        currentTaskId = this.mTaskId;
    }

    private boolean isCurrentTask()
    {
        return this.mTaskId != currentTaskId ;
    }

    private boolean isTimeout()
    {
        return (this.mTaskId + FINDING_TIMEOUT) < System.currentTimeMillis();
    }


    // Find target with runnable
    private void findTarget()
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                if (isCurrentTask())
                    return;
                if (isTimeout())
                {
                    if (AAAService.needAutoReturnHome)
                        PublicMethods.returnHome(mService);
                    return;
                }

                AccessibilityNodeInfo nodeInfo = mService.getRootInActiveWindow();
                if (!openTargetLoop(nodeInfo))
                    findTarget();
            }
        };
        new Handler().postDelayed(runnable, 100L);
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
