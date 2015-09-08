package com.drsoon.hongbao;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by dekunt on 15/9/3.
 */
public class MainActivity extends Activity
{
    private Button goSettingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // 解锁屏幕
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        setContentView(R.layout.activity_main);

        goSettingButton = (Button) findViewById(R.id.go_setting_button);goSettingButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onGoSetting();
            }
        });

        CheckBox unlockCheckBox = (CheckBox) findViewById(R.id.unlock_check_box);
        unlockCheckBox.setChecked(AAAService.needAutoUnlock);
        unlockCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                onCheckUnlock(isChecked);
            }
        });


    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (AAAService.runnableOnWakeup != null)
        {
            new Handler().postDelayed(AAAService.runnableOnWakeup, 500);
            AAAService.runnableOnWakeup = null;
        }

        AccessibilityManager manager = (AccessibilityManager) getSystemService(Activity.ACCESSIBILITY_SERVICE);
        boolean serviceEnabled = manager.isEnabled();
        goSettingButton.setTextColor(getResources().getColor(serviceEnabled ? R.color.btn_text_on : R.color.btn_text));
        goSettingButton.setText(serviceEnabled ? R.string.go_setting_on : R.string.go_setting);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        LinearLayout actionBarView = (LinearLayout) menu.findItem(R.id.title_layout).getActionView();
        TextView titleTextView = (TextView) actionBarView.findViewById(R.id.title_label);
        titleTextView.setText(getTitle());
        return super.onCreateOptionsMenu(menu);
    }

    private void onGoSetting()
    {
        Intent intent = new Intent("android.settings.ACCESSIBILITY_SETTINGS");
        startActivity(intent);
    }

    private void onCheckUnlock(boolean checked)
    {
        AAAService.needAutoUnlock = checked;
    }
}
