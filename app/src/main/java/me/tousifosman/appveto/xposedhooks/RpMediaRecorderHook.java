package me.tousifosman.appveto.xposedhooks;

import android.media.MediaRecorder;
import android.util.Log;

import me.tousifosman.appveto_manager.RpProcessMonitorClient;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class RpMediaRecorderHook {

    private static final String TAG = RpMediaRecorderHook.class.getSimpleName();

    @Nullable
    private static RpMediaRecorderHook instance;

    private final HashMap<MediaRecorder, MediaRecorderHolder> mediaRecorders = new HashMap<>();

    public static RpMediaRecorderHook getInstance() {
        if (instance == null) instance = new RpMediaRecorderHook();
        return instance;
    }

    /**
     * Initialize hooks for camera. Must be called from implementation of
     * {@link de.robv.android.xposed.IXposedHookLoadPackage#handleLoadPackage(XC_LoadPackage.LoadPackageParam)}
     * method.
     */
    static void init(@NotNull final XC_LoadPackage.LoadPackageParam lpparam) throws ClassNotFoundException {


        // Hook MediaReorder#MediaRecorder()
        // ---------------------------------
        XposedHelpers.findAndHookConstructor(MediaRecorder.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                MediaRecorder mediaRecorder = (MediaRecorder) param.thisObject;
                getInstance().mediaRecorders.put(mediaRecorder, new MediaRecorderHolder(mediaRecorder));
                Log.d(TAG, "afterHookedMethod: Recorder added -> " + Arrays.toString(getInstance().mediaRecorders.values().toArray()));
            }
        });

        // Hook MediaRecorder#release() method
        // -----------------------------------
        XposedHelpers.findAndHookMethod(MediaRecorder.class, "release", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                getInstance().mediaRecorders.remove(param.thisObject);
                Log.d(TAG, "afterHookedMethod: Recorder removed -> " + Arrays.toString(getInstance().mediaRecorders.values().toArray()));
            }
        });

        // Hook MediaRecorder#setVideoSource() method
        // ------------------------------------------
        XposedHelpers.findAndHookMethod(MediaRecorder.class,
                "setVideoSource", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);

                Log.d(TAG, "beforeHookedMethod: MediaRecorder#setVideoSource() method hooked");

                MediaRecorderHolder mediaRecorderHolder = getInstance().mediaRecorders.get(param.thisObject);
                if (mediaRecorderHolder != null) {
                    mediaRecorderHolder.videoSourceAvailable = true;
                } else {
                    throw new IllegalStateException("Target MediaRecorder Object is not loaded in policy map");
                }
            }
        });

        // Hook MediaRecorder#setAudioSource() method
        // ------------------------------------------
        XposedHelpers.findAndHookMethod(MediaRecorder.class,
                "setAudioSource", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);

                Log.d(TAG, "beforeHookedMethod: MediaRecorder#setAudioSource() method hooked");

                MediaRecorderHolder mediaRecorderHolder = getInstance().mediaRecorders.get(param.thisObject);
                if (mediaRecorderHolder != null) {
                    mediaRecorderHolder.audioSourceAvailable = true;
                } else {
                    throw new IllegalStateException("Target MediaRecorder Object is not loaded in policy map");
                }
            }
        });

        // Hook MediaRecorder#start()
        // --------------------------
        XposedHelpers.findAndHookMethod(MediaRecorder.class, "start", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);

                Log.d(TAG, "replaceHookedMethod: MediaRecorder#start() method hooked");

                MediaRecorderHolder holder = getInstance().mediaRecorders.get(param);
                if (holder == null) {
                    param.setThrowable(new IllegalStateException("Target MediaRecorder Object is not loaded in policy map"));
                }

                if (!isMediaAllowed(holder)) {
                    Log.d(TAG, "beforeHookedMethod: MediaRecord#start() is not allowed");
                    param.setThrowable(new IllegalStateException("Mic is not Allowed to be Access by current Focus App"));
                }
            }
        });
    }

    // Media Request Handler
    // ---------------------
   /* private static Object handleMediaRecorderRequest(@NotNull XC_MethodHook.MethodHookParam param) throws InvocationTargetException, IllegalAccessException {

        MediaRecorderHolder holder = getInstance().mediaRecorders.get(param);

        if (holder == null) {
            throw new IllegalStateException("Target MediaRecorder Object is not loaded in policy map");
        }

        if (isMediaAllowed(holder)) {
            return ((Method) param.method).invoke(param.thisObject, param.args);
        }
        throw new IllegalStateException("Mic is not Allowed to be Access by current Focus App");
    }*/

    private static boolean isMediaAllowed(@NotNull MediaRecorderHolder holder) {
        return !(holder.videoSourceAvailable && !RpProcessMonitorClient.getInstance().isAllowedCameraAccessCached()) ||
                (holder.audioSourceAvailable && !RpProcessMonitorClient.getInstance().isAllowedMicAccessCached());
    }

    private static class MediaRecorderHolder {

        private boolean audioSourceAvailable = false;
        private boolean videoSourceAvailable = false;

        private MediaRecorder mediaRecorder;

        public MediaRecorderHolder(MediaRecorder mediaRecorder) {
            this.mediaRecorder = mediaRecorder;
        }
    }

    public void notifyMediaAccessUpdate() {
        for (MediaRecorder recorder: getInstance().mediaRecorders.keySet()) {
            if (!isMediaAllowed(getInstance().mediaRecorders.get(recorder))) {
                try {
                    recorder.stop();
                } catch (Exception e) {
                    Log.d(TAG, "notifyMediaAccessUpdate: ", e);
                    getInstance().mediaRecorders.remove(recorder);
                    Log.d(TAG, "notifyMediaAccessUpdate: Recorder removed");
                }
            }
        }
    }

}
