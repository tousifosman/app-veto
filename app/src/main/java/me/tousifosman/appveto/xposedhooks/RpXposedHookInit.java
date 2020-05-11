package me.tousifosman.appveto.xposedhooks;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.os.Bundle;
import android.util.Log;

import de.robv.android.xposed.XposedBridge;
import me.tousifosman.appveto.xposedhooks.nativehooks.RpNativeHookInit;
import me.tousifosman.appveto.xposedhooks.nativehooks.XHook;
import me.tousifosman.appveto_manager.RpProcessMonitorClient;

import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class RpXposedHookInit implements IXposedHookLoadPackage {

    private static final String TAG = "RV_Xposed_Framework";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        // -----------------------------------------------------------------------------------------
        // Hooking Sensors
        // -----------------------------------------------------------------------------------------

        // We are hooking SensorEventQueue because this class knows for which sensor this callback is.
        XposedHelpers.findAndHookMethod("android.hardware.SystemSensorManager$SensorEventQueue", lpparam.classLoader, "dispatchSensorEvent",
                int.class, float[].class, int.class, long.class,
                new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);

                        final Context context = AndroidAppHelper.currentApplication();

                        final int handle = (int) param.args[0];
                        final float[] values = (float[]) param.args[1];

                        Object thisObj = param.thisObject;

                        try {

                            Class baseEventQueueClass = Class.forName("android.hardware.SystemSensorManager$BaseEventQueue");
                            Field mManagerField = baseEventQueueClass.getDeclaredField("mManager");
                            mManagerField.setAccessible(true);
                            Object mManagerObj = mManagerField.get(thisObj);

                            Class systemSensorManagerClass = Class.forName("android.hardware.SystemSensorManager");

                            Field mHandleToSensorField = systemSensorManagerClass.getDeclaredField("mHandleToSensor");
                            mHandleToSensorField.setAccessible(true);
                            Object mHandleToSensorObj = mHandleToSensorField.get(mManagerObj);

                            Sensor sensorObj = (Sensor) XposedHelpers.callMethod(mHandleToSensorObj, "get", handle);

                            if (sensorObj != null) {
                                RpProcessMonitorClient.getInstance().isAllowedSensorAccess(context, sensorObj.getType(), new RpProcessMonitorClient.RpClientBinaryCallback() {
                                    @Override
                                    public void onResult(boolean result) {
                                        if (!result) {
                                            for (int i = 0; i < values.length; i++)
                                                values[i] = 0;
                                        }
                                    }
                                });
                            } else {
                                Log.e(TAG, "beforeHookedMethod: Sensor Object not found");
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "beforeHookedMethod: ", e);
                        }
                    }
                });

        // -----------------------------------------------------------------------------------------
        // Hook Camera
        // -----------------------------------------------------------------------------------------

        RpCameraHook.init(lpparam);
        RpAudioRecordHook.init(lpparam);
        RpMediaRecorderHook.init(lpparam);

        // -----------------------------------------------------------------------------------------
        // Hook Current Focus App
        // -----------------------------------------------------------------------------------------

        XposedHelpers.findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);

                Activity activity = (Activity) param.thisObject;
                String curFocusApp = AndroidAppHelper.currentApplicationInfo().packageName;

                RpProcessMonitorClient.getInstance().setCurFocusAppOverIpcService(
                        AndroidAppHelper.currentApplication(),
                        curFocusApp,
                        activity.getClass().getName());

                Log.d(TAG, "beforeHookedMethod: New focus -> " + curFocusApp);
            }
        });

        XposedHelpers.findAndHookMethod(Activity.class, "onPause", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                RpProcessMonitorClient.getInstance().setCurFocusAppOverIpcService(
                        AndroidAppHelper.currentApplication(),
                        null,
                        null);
            }
        });


        // -----------------------------------------------------------------------------------------
        // Hook Native
        // -----------------------------------------------------------------------------------------

        XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                RpNativeHookInit.bindWith((Application) param.thisObject).setNativeLibPath();
                //RpNativeHookInit.bindWith((Application) param.thisObject).init();
                Log.d(TAG, "beforeHookedMethod -> Application#onCreate: Native Hook Initialized");
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.d(TAG, "afterHookedMethod -> Application#onCreate: Native Hook Initialized");
                RpNativeHookInit.bindWith((Application) param.thisObject).setNativeLibPath();
                //RpNativeHookInit.bindWith((Application) param.thisObject).init();
            }
        });

        XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.d(TAG, "afterHookedMethod -> Activity#onCreate: Native Hook Initialized");
                RpNativeHookInit.bindWith((Context) param.thisObject).init();
            }
        });

        XposedHelpers.findAndHookMethod(Activity.class, "onPause"/*, Bundle.class*/, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.d(TAG, "afterHookedMethod -> Activity#onPause: Native Hook Initialized");
                RpNativeHookInit.bindWith((Context) param.thisObject).init();
            }
        });

        /*if (!(lpparam.packageName.matches("android" +
                "|com[.]android[.]providers[.]settings" +
                "|com[.]android[.]phone" +
                "|com[.]android[.]providers[.]telephony" +
                "|com[.]google[.]android[.]gms" +
                "|org[.]codeaurora[.]ims" +
                "|com[.]qualcomm[.]qti[.]services[.]secureui"))) {*/

        XposedHelpers.findAndHookMethod(Runtime.class, "loadLibrary0", ClassLoader.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.d(TAG, "afterHookedMethod -> loadLibrary(): Native hook lib: " + param.args[1] + ", " + lpparam.packageName);
                if (XHook.getInstance().isInited()) {
                    try {
                        RpNativeHookInit.bindWith(AndroidAppHelper.currentApplication().getBaseContext()).init();
                        Log.d(TAG, "afterHookedMethod -> loadLibrary(): Native lib hooked");
                    } catch (Exception e) {
                        Log.e(TAG, "afterHookedMethod -> loadLibrary(): Could not hook lib", e);
                    }
                } else {
                    Log.d(TAG, "afterHookedMethod -> loadLibrary(): Native hooks not yet initialized");
                }
            }
        });

        XposedHelpers.findAndHookMethod(Runtime.class, "load0", Class.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.d(TAG, "afterHookedMethod -> load(): Native hooked lib " + param.args[1] + ", " + lpparam.packageName);
                if (XHook.getInstance().isInited()) {
                    try {
                        RpNativeHookInit.bindWith(AndroidAppHelper.currentApplication().getBaseContext()).init();
                        Log.d(TAG, "afterHookedMethod -> load(): Native lib hooked");
                    } catch (Exception e) {
                        Log.e(TAG, "afterHookedMethod -> load(): Could not hook lib", e);
                    }
                } else {
                    Log.d(TAG, "afterHookedMethod -> load(): Native hooks not yet initialized");
                }
            }
        });
        /*}*/


        /*if (lpparam.packageName.equals("cc.pacer.androidapp")) {

            Log.d( "xposed_pacer", "handleLoadPackage: Ended Package: " + lpparam.packageName);

            XposedHelpers.findAndHookMethod("cc.pacer.androidapp.dataaccess.core.pedometer.tracker.Pedometer", lpparam.classLoader, "getRecentSteps", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Log.d(TAG, "beforeHookedMethod: xposed_pacer -> getRecentSteps called");
                }
            });
        }*/
    }
}
