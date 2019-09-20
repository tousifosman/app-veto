package com.reversepermission.reverse_permission_manager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import com.reversepermission.reverse_permission_manager.metadata.RpMetadata;

import org.jetbrains.annotations.Nullable;

public class RpProcessMonitorService extends Service {

    private final String TAG = RpProcessMonitorService.class.getName();

    @Nullable
    private static RpProcessMonitorService instance;

    private boolean flagServiceActive = false;

    @NonNull
    private final SparseArray<RpPsServiceCallbackInf> callbackInfMap = new SparseArray<>();

    @Nullable
    private RpProcessUtils.CurrentFocusedApp mCurrentFocusedApp = null;

    private final  RpPsServiceInf.Stub binderStub = new RpPsServiceInf.Stub(){
        @Nullable
        @Override
        public String getCurFocusApp() throws RemoteException {
            return (flagServiceActive && mCurrentFocusedApp != null) ? mCurrentFocusedApp.getFocusAppName() : null;
        }

        @Nullable
        @Override
        public String getCurFocusActivity() throws RemoteException {
            return (flagServiceActive && mCurrentFocusedApp != null) ? mCurrentFocusedApp.getFocusActivityName() : null;
        }

        @Override
        public void setCurFocusApp(String appName, String activityName) throws RemoteException {
            mCurrentFocusedApp = new RpProcessUtils.CurrentFocusedApp(appName, activityName);
            for (int i = 0; i < callbackInfMap.size(); i++) {
                int key = callbackInfMap.keyAt(i);
                try {
                    callbackInfMap.get(key).onFocusAppChanged();
                } catch (Exception e) {
                    Log.d(TAG, "setCurFocusApp: Client binder disconnected");
                    callbackInfMap.remove(key);
                }
            }
        }

        @Override
        public void setServiceStatus(boolean flag) throws RemoteException {
            flagServiceActive = flag;
        }

        @Override
        public boolean isServiceActive() throws RemoteException {
            return flagServiceActive;
        }

        @Override
        public boolean isAllowSensor(int sensorType) throws RemoteException {
            if (flagServiceActive && RpProcessUtils.CurrentFocusedApp.isValid(mCurrentFocusedApp)) {

                // Check for if the access is cached or not.
                Boolean allowAccess = mCurrentFocusedApp.cachedTypeAccessMap.get(sensorType);
                if (allowAccess == null) {

                    @Nullable
                    RpMetadata metaData = RpMetadata.typeToMetaData(sensorType);

                    allowAccess = metaData == null || !RpManager.bindWith(RpProcessMonitorService.this)
                            .isReversePermissionInPackage(
                                    mCurrentFocusedApp.getFocusAppName(), metaData);

                    mCurrentFocusedApp.cachedTypeAccessMap.put(sensorType, allowAccess);
                    Log.d(TAG, "isAllowSensor: " + allowAccess.booleanValue());
                }
                return allowAccess;
            }
            return true;
        }

        @Override
        public boolean isAllowCamera() throws RemoteException {

            if (flagServiceActive && RpProcessUtils.CurrentFocusedApp.isValid(mCurrentFocusedApp)) {
                if (mCurrentFocusedApp.cachedCameraAccess == null) {
                    mCurrentFocusedApp.cachedCameraAccess = !RpManager.bindWith(RpProcessMonitorService.this)
                            .isReversePermissionInPackage(mCurrentFocusedApp.getFocusAppName(), RpMetadata.RP_SENSOR_CAMERA);
                }
                return mCurrentFocusedApp.cachedCameraAccess;
            }
            return true;
        }


        @Override
        public boolean isAllowedMic() throws RemoteException {


            if (flagServiceActive && RpProcessUtils.CurrentFocusedApp.isValid(mCurrentFocusedApp)) {
                if (mCurrentFocusedApp.cachedMicAccess == null) {
                    mCurrentFocusedApp.cachedMicAccess = !RpManager.bindWith(RpProcessMonitorService.this)
                            .isReversePermissionInPackage(mCurrentFocusedApp.getFocusAppName(), RpMetadata.RP_SENSOR_MIC);
                }
                return mCurrentFocusedApp.cachedMicAccess;
            }
            return true;
        }

        @Override
        public void registerCallback(RpPsServiceCallbackInf callbackInf) throws RemoteException {
            callbackInfMap.put(callbackInf.getProcessId(), callbackInf);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: Service Created");
    }

    /**
     * Hidden public constructor.
     */
    public RpProcessMonitorService() {}

    public static RpProcessMonitorService getInstance() {
        if (instance == null)
            instance = new RpProcessMonitorService();
        return instance;
    }

    public void startService(Context context) {
        RpProcessMonitorClient.getInstance().setServiceStatus(context, true);
    }

    public void stopService(Context context) {
        RpProcessMonitorClient.getInstance().setServiceStatus(context, false);
    }

    @NonNull
    @Override
    public IBinder onBind(Intent intent) {
        return binderStub;
    }
}
