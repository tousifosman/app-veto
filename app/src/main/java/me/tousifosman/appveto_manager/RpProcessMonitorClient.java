package me.tousifosman.appveto_manager;

import android.app.AndroidAppHelper;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.util.Log;

import me.tousifosman.appveto.xposedhooks.RpAudioRecordHook;
import me.tousifosman.appveto.xposedhooks.RpCameraHook;
import me.tousifosman.appveto.xposedhooks.RpMediaRecorderHook;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class RpProcessMonitorClient {

    private final String TAG = RpProcessMonitorClient.class.getSimpleName();

    @Nullable
    private static RpProcessMonitorClient instance;

    private boolean allowedCameraAccess = true;
    private boolean allowedMicAccess = true;

    @NonNull
    private RpPsServiceCallbackInf serviceCallbackInf = new RpPsServiceCallbackInf.Stub() {
        @Override
        public int getProcessId() throws RemoteException {
            return Process.myPid();
        }

        @Contract(pure = true)
        @Override
        public String getClientPackageName() throws RemoteException {
            return AndroidAppHelper.currentApplicationInfo().packageName;
        }

        @Override
        public void onFocusAppChanged() throws RemoteException {
            Log.d(TAG, "onFocusAppChanged: Focus App Changed Callback");
            Context context = AndroidAppHelper.currentApplication();
            updateAllowedCameraAccess(context);
            updateAllowedMicAccess(context);
            RpMediaRecorderHook.getInstance().notifyMediaAccessUpdate();
        }
    };

    private RpProcessMonitorClient(){
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitNetwork().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    public static RpProcessMonitorClient getInstance() {
        if (instance == null)
            instance = new RpProcessMonitorClient();
        return instance;
    }
    // ----------------------------------------------------


    @Nullable
    private RpPsServiceInf serviceInf;

    @Nullable
    private ServiceConnection connection;


    private void initIpcService(Context context, final RpPmClientConnCallback connCallback) {
        if (connection == null || serviceInf == null) {

            connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {

                    serviceInf = RpPsServiceInf.Stub.asInterface(service);
                    try {
                        serviceInf.registerCallback(serviceCallbackInf);
                    } catch (RemoteException e) {
                        Log.d(TAG, "onServiceConnected: Error while registering service callback", e);
                    }
                    connCallback.onConnReady(serviceInf);
                    Log.d("RV Xposed Framework", "onServiceConnected: Service connected");

                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    connection = null;
                    serviceInf = null;
                    Log.d("RV Xposed Framework", "onServiceConnected: Service disconnected");
                }
            };

            Intent intent = new Intent("me.tousifosman.appveto_manager.PROCESS_SERVICE");
            intent.setPackage("me.tousifosman.appveto");
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        } else {
            connCallback.onConnReady(serviceInf);
        }
    }


    public void getCurFocusAppOverIpcService(Context context, @NonNull final RvPmCurPsCallback rvPmCurPsCallback) {
        initIpcService(context, new RpPmClientConnCallback() {
            @Override
            public void onConnReady(@NonNull RpPsServiceInf serviceInf) {
                try {
                    rvPmCurPsCallback.onFoundCurrentActivity(serviceInf.getCurFocusApp(), serviceInf.getCurFocusActivity());
                } catch (RemoteException e) {
                    Log.d(TAG, "onConnReady: ", e);
                }
            }
        });
    }

    public void setCurFocusAppOverIpcService(Context context, final String appName, final String activityName) {
        initIpcService(context, new RpPmClientConnCallback() {
            @Override
            public void onConnReady(@NonNull RpPsServiceInf serviceInf) {
                try {
                    serviceInf.setCurFocusApp(appName, activityName);
                } catch (RemoteException e) {
                    Log.e(TAG, "onConnReady: ", e);
                }
            }
        });
    }

    public void setServiceStatus(Context context, final boolean flag) {
        initIpcService(context, new RpPmClientConnCallback() {
            @Override
            public void onConnReady(@NonNull RpPsServiceInf serviceInf) {
                try {
                    serviceInf.setServiceStatus(flag);
                } catch (RemoteException e) {
                    Log.e(TAG, "onConnReady: ", e);
                }
            }
        });
    }

    public void isServiceActive(Context context, @NonNull final RpClientBinaryCallback callback) {
        initIpcService(context, new RpPmClientConnCallback() {
            @Override
            public void onConnReady(@NonNull RpPsServiceInf serviceInf) {
                try {
                    callback.onResult(serviceInf.isServiceActive());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void isAllowedSensorAccess(Context context, final int sensorType, @NonNull final RpClientBinaryCallback callback) {
        initIpcService(context, new RpPmClientConnCallback() {
            @Override
            public void onConnReady(@NonNull RpPsServiceInf serviceInf) {
                try {
                    callback.onResult(serviceInf.isAllowSensor(sensorType));
                } catch (RemoteException e) {
                    Log.e(TAG, "onConnReady: ", e);
                }
            }
        });
    }

    public boolean isAllowedCameraAccessCached() {
        return allowedCameraAccess;
    }

    public void isAllowedCameraAccess(Context context, @NonNull final RpClientBinaryCallback callback) {
        initIpcService(context, new RpPmClientConnCallback() {
            @Override
            public void onConnReady(@NonNull RpPsServiceInf serviceInf) {
                try {
                    callback.onResult(serviceInf.isAllowCamera());
                } catch (RemoteException e) {
                    Log.e(TAG, "onConnReady: ", e);
                }
            }
        });
    }

    public void updateAllowedCameraAccess(Context context) {
        isAllowedCameraAccess(context, new RpClientBinaryCallback() {
            @Override
            public void onResult(boolean result) {
                allowedCameraAccess = result;
                RpCameraHook.getInstance().notifyCameraAccessUpdated();
                Log.d(TAG, "onResult: Updated Camera Access Notified -> " + result);
            }
        });
    }

    public boolean isAllowedMicAccessCached() {
        return allowedMicAccess;
    }

    public void isAllowedMicAccess(Context context, @NonNull final RpClientBinaryCallback callback) {
        initIpcService(context, new RpPmClientConnCallback() {
            @Override
            public void onConnReady(@NonNull RpPsServiceInf serviceInf) {
                try {
                    callback.onResult(serviceInf.isAllowedMic());
                } catch (RemoteException e) {
                    Log.e(TAG, "onConnReady: ", e);
                }
            }
        });
    }

    public void updateAllowedMicAccess(Context context) {
        isAllowedMicAccess(context, new RpClientBinaryCallback() {
            @Override
            public void onResult(boolean result) {
                allowedMicAccess = result;
                RpAudioRecordHook.getInstance().notifyMicAccessUpdated();
                Log.d(TAG, "onResult: Updated Mic Access Notified -> " + result);
            }
        });
    }

    public interface RpClientBinaryCallback {
        void onResult(boolean result);
    }

    public interface RvPmCurPsCallback {
        void onFoundCurrentActivity(String curFocusApp, String curFocusActivity);
    }

    private interface RpPmClientConnCallback {
        void onConnReady(@NonNull RpPsServiceInf serviceInf);
    }

}
