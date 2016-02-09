package com.drsoon.hongbao;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.LinkedList;

/**
 * Created by dekunt on 15/9/3.
 */
public class WeiXinTask
{
    private static final long FINDING_TIMEOUT = 5000;
    private static final int TARGET_RETRY_MAX = 1;

    private static WeiXinTask waitingResultTask;
    private static LinkedList<WeiXinTask> waitingDialogTasks = new LinkedList<>();
    private static long currentTaskId;

    private AccessibilityService mService;
    private int mTargetRetryCount;
    private boolean mEnteredChatUI;
    private long mTaskId;
    private boolean mWaitingDialogShowed;

    // Find WeiXin hongBao
    public static void findTarget(AccessibilityService service)
    {
        onWaitingDialogShowed();
        new WeiXinTask(service).findTarget();
    }

    // Open WeiXin hongBao
    public static void openTarget(AccessibilityService service)
    {
        onWaitingDialogShowed();
        waitingResultTask = null;
        AccessibilityNodeInfo nodeInfo = service.getRootInActiveWindow();
        openTargetLoop(nodeInfo);
    }

    // Open hongBao Detail
    public static void onOpenDetailUI(AccessibilityService service)
    {
        onWaitingDialogShowed();
        if (waitingResultTask != null)
        {
            if (waitingResultTask.isCurrentTask() && waitingResultTask.mTargetRetryCount < TARGET_RETRY_MAX)
            {
                AccessibilityNodeInfo nodeInfo = service.getRootInActiveWindow();
                closeDetailLoop(nodeInfo);

                WeiXinTask other = new WeiXinTask(service);
                other.mEnteredChatUI = true;
                other.mTargetRetryCount = waitingResultTask.mTargetRetryCount + 1;
                other.findTarget();
            }
            waitingResultTask = null;
        }
    }

    // Waiting Dialog showed
    public static void onWaitingDialogShowed()
    {
        for (WeiXinTask task : waitingDialogTasks)
            task.mWaitingDialogShowed = true;
        waitingDialogTasks.clear();
    }

    private WeiXinTask(AccessibilityService service)
    {
        this.mService = service;
        this.mTargetRetryCount = 0;
        this.mEnteredChatUI = false;
        this.mWaitingDialogShowed = false;
        this.mTaskId = System.currentTimeMillis();
        currentTaskId = this.mTaskId;
    }

    private boolean isCurrentTask()
    {
        return this.mTaskId == currentTaskId;
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
                if (!isCurrentTask())
                    return;
                if (mWaitingDialogShowed)
                    return;

                AccessibilityNodeInfo nodeInfo = mService.getRootInActiveWindow();
                if (!mEnteredChatUI)
                {
                    if (isChatFragment(nodeInfo)) {
                        mEnteredChatUI = true;
                        mTaskId = System.currentTimeMillis();
                        currentTaskId = mTaskId;
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

                if (!findTargetLoop(nodeInfo))
                    findTarget();
            }
        };
        new Handler().postDelayed(runnable, 40);
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
                onFoundTarget(nodeInfo.getParent());
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

    private void onFoundTarget(AccessibilityNodeInfo nodeInfo)
    {
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        waitingResultTask = this;

        waitingDialogTasks.add(this);
        mWaitingDialogShowed = false;
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                if (isCurrentTask() && !mWaitingDialogShowed)
                {
                    WeiXinTask other = new WeiXinTask(mService);
                    waitingDialogTasks.add(other);
                    other.findTarget();
                }
            }
        };
        new Handler().postDelayed(runnable, 100);
    }


    private boolean isChatFragment(AccessibilityNodeInfo nodeInfo)
    {
        if (nodeInfo == null || !nodeInfo.getPackageName().toString().equals("com.tencent.mm"))
            return false;
        int count = nodeInfo.getChildCount();
        if (count > 0)
        {
            if (count == 4 && nodeInfo.getClassName().toString().equals("android.widget.LinearLayout")
                    && nodeInfo.getChild(0).getClassName().toString().equals("android.widget.ImageButton")
                    && nodeInfo.getChild(1).getClassName().toString().equals("android.widget.EditText")
                    && nodeInfo.getChild(2).getClassName().toString().equals("android.widget.ImageButton")
                    && nodeInfo.getChild(3).getClassName().toString().equals("android.widget.ImageButton"))
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

    // Open target loop
    private static boolean openTargetLoop(AccessibilityNodeInfo nodeInfo)
    {
        if (nodeInfo == null)
            return false;
        if (nodeInfo.getChildCount() == 0)
        {
            CharSequence nodeText = nodeInfo.getText();
            if (nodeText != null && "拆红包".equals(nodeText.toString())
                    && "android.widget.FrameLayout".equals(nodeInfo.getParent().getClassName().toString()))
            {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return true;
            }
            // 兼容微信新版本6.3.9
            else if (nodeText == null && "android.widget.Button".equals(nodeInfo.getClassName().toString())
                    && "android.widget.FrameLayout".equals(nodeInfo.getParent().getClassName().toString()))
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



    // Close Detail loop
    private static boolean closeDetailLoop(AccessibilityNodeInfo nodeInfo)
    {
        if (nodeInfo == null)
            return false;
        if (nodeInfo.getChildCount() == 0
                && nodeInfo.getClassName().toString().equals("android.widget.ImageView")
                && nodeInfo.getContentDescription().toString().equals("返回"))
        {
            nodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            return true;
        }
        for (int i = 0; i < nodeInfo.getChildCount(); i++)
        {
            if (closeDetailLoop(nodeInfo.getChild(i)))
                return true;
        }
        return false;
    }
}
