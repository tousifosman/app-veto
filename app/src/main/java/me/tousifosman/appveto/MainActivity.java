package me.tousifosman.appveto;

import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import me.tousifosman.appveto.xposedhooks.RpXposedHookInit;
import me.tousifosman.appveto.xposedhooks.nativehooks.RpNativeHookInit;
import me.tousifosman.appveto_manager.RpProcessMonitorClient;
import me.tousifosman.appveto_manager.RpProcessMonitorService;

import com.tousifosman.appveto.R;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "AppVeto.MainActivity";

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

        findViewById(R.id.btn_install).setOnClickListener(this);
        findViewById(R.id.btn_uninstall).setOnClickListener(this);

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

        String[] nativeDirPathComponents = this.getApplicationInfo().nativeLibraryDir.split("/");

        Log.d(TAG, "onCreate: architecture-size -> " + this.getApplicationInfo().nativeLibraryDir);

        /*findViewById(R.id.btn_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RpProcessMonitorClient.getInstance().getCurFocusAppOverIpcService(getApplicationContext(), new RpProcessMonitorClient.RvPmCurPsCallback() {
                    @Override
                    public void onFoundCurrentActivity(String curFocusApp, String curFocusActivity) {
                        Log.d(TAG, "onCheckedChanged: Service status: CurApp -> " + curFocusApp + ", CurActivity -> " + curFocusActivity);
                    }
                });
            }
        });*/

        //RpNativeHookInit.bindWith(this).init();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_install:
                installLibraries();
                break;
            case R.id.btn_uninstall:
                uninstallLibraries();
        }
    }

    void installLibraries() {
        String tmpLibPath = "/data/local/tmp/" + this.getPackageName();
        String localLibPath = "/data/data/" + this.getPackageName();

        copyFileOrDir("lib", localLibPath, tmpLibPath);

        try {
            Process su = Runtime.getRuntime().exec("su");

            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            //outputStream.writeBytes("rm -r " + tmpLibPath +";");
            //outputStream.writeBytes("mkdir " + tmpLibPath +";");

            if (new File("/system/lib64/").exists()) {
                outputStream.writeBytes("rm /system/lib64/librpxhook.so;");
                outputStream.writeBytes("cp " + localLibPath + "/lib/arm64-v8a/librpxhook.so /system/lib64/;");
            }

            if (new File("/system/lib/").exists()) {
                outputStream.writeBytes("rm /system/lib/librpxhook.so;");
                outputStream.writeBytes("cp " + localLibPath + "/lib/armeabi-v7a/librpxhook.so /system/lib/;");
            }

            outputStream.flush();

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            su.waitFor();

            Toast.makeText(this, "Library Installed", Toast.LENGTH_SHORT).show();

        } catch (InterruptedException | IOException e) {
            Log.e(TAG, "copyFileOrDir: Error occurred while installing lib", e);
            Toast.makeText(this, "Library Installation Failed", Toast.LENGTH_SHORT).show();
        }
    }

    void uninstallLibraries() {

        try {
            Process su = Runtime.getRuntime().exec("su");

            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            if (new File("/system/lib64/").exists()) {
                outputStream.writeBytes("rm /system/lib64/librpxhook.so;");
            }

            if (new File("/system/lib/").exists()) {
                outputStream.writeBytes("rm /system/lib/librpxhook.so;");
            }

            outputStream.flush();

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            su.waitFor();

            Toast.makeText(this, "Library Unistalled", Toast.LENGTH_SHORT).show();

        } catch (InterruptedException | IOException e) {
            Log.e(TAG, "copyFileOrDir: Error occurred while uninstalling lib", e);
            Toast.makeText(this, "Library Uninstallation Failed", Toast.LENGTH_SHORT).show();
        }

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

    private void copyFileOrDir(String path, String localLibPath, String tmpLibPath) {
        AssetManager assetManager = this.getAssets();
        String assets[] = null;

        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(path);
            } else {

                File tmpLibDir = new File(tmpLibPath);
                if (tmpLibDir.exists()) {
                    tmpLibDir.delete();
                }
                tmpLibDir.mkdir();

                String fullPath = localLibPath + "/" + path;
                File dir = new File(fullPath);
                if (!dir.exists())
                    dir.mkdir();
                for (int i = 0; i < assets.length; ++i) {
                    copyFileOrDir(path + "/" + assets[i], localLibPath, tmpLibPath);
                }
            }

        } catch (IOException ex) {
            Log.e(TAG, "I/O Exception", ex);
        }
    }

    private void copyFile(String filename) {
        AssetManager assetManager = this.getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            String newFileName = "/data/data/" + this.getPackageName() + "/" + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }
}
