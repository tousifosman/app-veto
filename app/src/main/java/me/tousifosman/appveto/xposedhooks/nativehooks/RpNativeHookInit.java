package me.tousifosman.appveto.xposedhooks.nativehooks;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.ref.WeakReference;

public class RpNativeHookInit {

    private static final String TAG = "RpNativeHookInit";

    private static RpNativeHookInit instance;
    private RpNativeHookInitObj initObj;

    private RpNativeHookInit(Context context) {
        initObj = new RpNativeHookInitObj(context);
    }

    @NonNull
    public static RpNativeHookInitObj bindWith(Context context) {
        if (instance == null)
            instance = new RpNativeHookInit(context);

        if (context != null)
            instance.initObj.contextRef = new WeakReference<>(context);

        return instance.initObj;
    }

    public class RpNativeHookInitObj {
        private WeakReference<Context> contextRef;

        private RpNativeHookInitObj(Context context) {
            contextRef = new WeakReference<>(context);
        }

        public void init() {

            Log.d(TAG, "init: Native hooks Initializing");

            if (!XHook.getInstance().init(contextRef.get())) {
                Log.e(TAG, "init: Unable to load XHook Library");
                return;
            }

            setNativeLibPath();

            XHook.getInstance().enableDebug(true);
            Log.d(TAG, "init: XHook Library Loaded");


            hookNativeJNI();

            XHook.getInstance().refresh(false);

            Log.d(TAG, "init: Native hooks refreshed");
        }

        public void setNativeLibPath() {
            if (contextRef.get() != null) {
                String nativeLibraryDir = contextRef.get().getApplicationInfo().nativeLibraryDir;
                setNativeLibPathJNI(nativeLibraryDir);
                Log.d(TAG, "setNativeLibPath: Initialized -> " + nativeLibraryDir);
            } else {
                Log.d(TAG, "setNativeLibPath: Not Initialized -> Context -> " + contextRef.get());
            }
        }

        public void updateSensorAccess(int[] sensorAccessMap) {
            if(!XHook.getInstance().isInited()) {
                XHook.getInstance().init(contextRef.get());
                Log.d(TAG, "updateSensorAccess: Initialized before pause");
            }
            try {
                if (XHook.getInstance().isInited()) {
                    updateSensorAccessJNI(sensorAccessMap);
                } else {
                    Log.e(TAG, "Library load status: " + XHook.getInstance().isInited() + ", PackageName: " + contextRef.get().getPackageName());
                }
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "Library load status: " + XHook.getInstance().isInited() + ", PackageName: " + contextRef.get().getPackageName(), e);
            }
        }

        public void updateMicAccess(boolean allowed) {
            if(!XHook.getInstance().isInited()) {
                XHook.getInstance().init(contextRef.get());
                Log.d(TAG, "updateSensorAccess: Initialized before pause");
            }
            try {
                if (XHook.getInstance().isInited()) {
                    updateMicAccessJNI(allowed);
                } else {
                    Log.e(TAG, "Library load status: " + XHook.getInstance().isInited() + ", PackageName: " + contextRef.get().getPackageName());
                }
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "Library load status: " + XHook.getInstance().isInited() + ", PackageName: " + contextRef.get().getPackageName(), e);
            }
        }
    }

    private native void hookNativeJNI();
    private native void setNativeLibPathJNI(String path);
    private native void updateSensorAccessJNI(int[] sensorAccessMap);
    private native void updateMicAccessJNI(boolean allowed);
    private native void updateCameraAccessJNI(boolean allowed);

}

