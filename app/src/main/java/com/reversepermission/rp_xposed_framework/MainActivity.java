package com.reversepermission.rp_xposed_framework;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.reversepermission.reverse_permission_manager.RpProcessMonitorClient;
import com.reversepermission.reverse_permission_manager.RpProcessMonitorService;


public class MainActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ButterKnife.bind(this);

        final ToggleButton btnToggleService = findViewById(R.id.btn_toggle_service);

        RpProcessMonitorClient.getInstance().isServiceActive(this, new RpProcessMonitorClient.RpClientBinaryCallback() {
            @Override
            public void onResult(boolean result) {
                btnToggleService.setChecked(result);
            }
        });

        btnToggleService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    RpProcessMonitorService.getInstance().startService(MainActivity.this);
                    Log.d(TAG, "onClickBtnToggleService: Process Monitor Service Started");
                } else {
                    RpProcessMonitorService.getInstance().stopService(MainActivity.this);
                    Log.d(TAG, "onClickBtnToggleService: Process Monitor Service Stopped");
                }
            }
        });

        findViewById(R.id.btn_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RpProcessMonitorClient.getInstance().getCurFocusAppOverIpcService(getApplicationContext(), new RpProcessMonitorClient.RvPmCurPsCallback() {
                    @Override
                    public void onFoundCurrentActivity(String curFocusApp, String curFocusActivity) {
                        Log.d(TAG, "onCheckedChanged: Service status: CurApp -> " + curFocusApp + ", CurActivity -> " + curFocusActivity);
                    }
                });
            }
        });
    }

    void onClickBtnToggleService() {
        /*if (toggleButton.isChecked()) {
            RpProcessMonitorService.stopService();
            Log.d(TAG, "onClickBtnToggleService: Process Monitor Service Started");
        } else {
            RpProcessMonitorService.startService();
            Log.d(TAG, "onClickBtnToggleService: Process Monitor Service Stopped");
        }*/
    }
}
