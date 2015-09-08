package com.drsoon.hongbao;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by dekunt on 15/9/3.
 */
public class WeiXinTask
{
    private static final long FINDING_TIMEOUT = 5000;

    private static long currentTaskId;

    private long mTaskId;
    private AccessibilityService mService;


    // Find WeiXin hongBao
    public static void findTarget(AccessibilityService service)
    {
        new WeiXinTask(service).findTarget();
    }

    // Open WeiXin hongBao
    public static void openTarget(AccessibilityService service)
    {
        AccessibilityNodeInfo nodeInfo = service.getRootInActiveWindow();
        openTargetLoop(nodeInfo);
    }

    private WeiXinTask(AccessibilityService service)
    {
        this.mService = service;
        this.mTaskId = System.currentTimeMillis();
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
                if (!findTargetLoop(nodeInfo))
                    findTarget();
            }
        };
        new Handler().postDelayed(runnable, 100L);
    }


    // Find target loop
    private boolean findTargetLoop(AccessibilityNodeInfo nodeInfo)
    {
        if (nodeInfo == null)
            return false;
        if (nodeInfo.getChildCount() == 0)
        {
            CharSequence nodeText = nodeInfo.getText();
            if (nodeText != null && nodeInfo.getText().toString().equals("领取红包")
                    && nodeInfo.getParent().getClassName().toString().equals("android.widget.LinearLayout"))
            {
                nodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return true;
            }
            return false;
        }

        for (int i = -1 + nodeInfo.getChildCount(); i >= 0; i--)
        {
            if (findTargetLoop(nodeInfo.getChild(i)))
                return true;
        }
        return false;
    }


    // Open target loop
    private static boolean openTargetLoop(AccessibilityNodeInfo nodeInfo)
    {
        if (nodeInfo == null)
            return false;
        if (nodeInfo.getChildCount() == 0)
        {
            CharSequence nodeText = nodeInfo.getText();
            if (nodeText != null && nodeText.toString().equals("拆红包")
                    && nodeInfo.getParent().getClassName().toString().equals("android.widget.FrameLayout"))
            {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return true;
            }
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
