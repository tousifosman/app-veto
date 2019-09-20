package com.reversepermission.rp_xposed_framework.xposed_hooks;

import android.media.AudioRecord;
import android.util.Log;

import com.reversepermission.reverse_permission_manager.RpProcessMonitorClient;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class RpAudioRecordHook {

    private static final String TAG = RpAudioRecordHook.class.getSimpleName();

    @Nullable
    private static RpAudioRecordHook instance;

    public static RpAudioRecordHook getInstance() {
        if (instance == null) {
            instance = new RpAudioRecordHook();
        }
        return instance;
    }

    /**
     * Initialize hooks for camera. Must be called from implementation of
     * {@link de.robv.android.xposed.IXposedHookLoadPackage#handleLoadPackage(XC_LoadPackage.LoadPackageParam)}
     * method.
     */
    static void init(@NotNull final XC_LoadPackage.LoadPackageParam lpparam) throws ClassNotFoundException {

        // Hook AudioRecord#native_start() method
        // --------------------------------------
        XposedHelpers.findAndHookMethod(AudioRecord.class,
                "native_start", int.class, int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        Log.d(TAG, "beforeHookedMethod: AduioRecord#native_start() method hooked");
                        if (!RpProcessMonitorClient.getInstance().isAllowedMicAccessCached()) {
                            Log.d(TAG, "beforeHookedMethod: AudioRecord#native_start() is not allowed");
                            param.setThrowable(new IllegalStateException("Mic is not Allowed to be Access by current Focus App"));
                        }
                    }
                });

        // Hook AudioRecord#native_read_in_byte_array()
        // --------------------------------------------
        try {
            XposedHelpers.findAndHookMethod(AudioRecord.class,
                    "native_read_in_byte_array", byte[].class, int.class, int.class, boolean.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Log.d(TAG, "beforeHookedMethod: AudioRecord#native_read_in_byte_array() method hooked");
                            if (!RpProcessMonitorClient.getInstance().isAllowedMicAccessCached()) {
                                Log.d(TAG, "beforeHookedMethod: AudioRecord#native_read_in_byte_array() is not allowed");
                                param.args[0] = new byte[]{0};
                            }
                        }
                    });
        } catch (NoSuchMethodError e) {
            try {
                XposedHelpers.findAndHookMethod(AudioRecord.class,
                        "native_read_in_byte_array", byte[].class, int.class, int.class, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                super.beforeHookedMethod(param);
                                Log.d(TAG, "beforeHookedMethod: AudioRecord#native_read_in_byte_array() method hooked");
                                if (!RpProcessMonitorClient.getInstance().isAllowedMicAccessCached()) {
                                    Log.d(TAG, "beforeHookedMethod: AudioRecord#native_read_in_byte_array() is not allowed");
                                    param.args[0] = new byte[]{0};
                                }
                            }
                        });
            } catch (NoSuchMethodError ei) {
                Log.d(TAG, "init: AudioRecord#native_read_in_byte_array() method not found", ei);
            }
        }

        // Hook AudioRecord#native_read_in_direct_buffer()
        // --------------------------------------------
        try {
            XposedHelpers.findAndHookMethod(AudioRecord.class,
                    "native_read_in_direct_buffer", Object.class, int.class, boolean.class, new XC_MethodHook() {

                        private ByteBuffer bbTemp;

                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Log.d(TAG, "beforeHookedMethod: AudioRecord#native_read_in_direct_buffer() method hooked");
                            if (!RpProcessMonitorClient.getInstance().isAllowedMicAccessCached()) {
                                Log.d(TAG, "beforeHookedMethod: AudioRecord#native_read_in_direct_buffer() is not allowed");
                                //param.args[0] = ByteBuffer.allocate(((ByteBuffer)param.args[0]).capacity());
                                ByteBuffer bb = ((ByteBuffer)param.args[0]);
                                bb.put(new byte[bb.capacity()]);

                                bbTemp = bb.duplicate();

                                param.setResult(1);
                            }
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            if (!RpProcessMonitorClient.getInstance().isAllowedMicAccessCached()) {
                                Log.d(TAG, "afterHookedMethod: AudioRecord#native_read_in_direct_buffer() is not allowed");
                                ByteBuffer bb = ((ByteBuffer)param.args[0]);
                                bb.clear();
                                bb.put(bbTemp);
                                param.setResult(1);
                            }
                        }
                    });
        } catch (NoSuchMethodError e) {
            try {
                XposedHelpers.findAndHookMethod(AudioRecord.class,
                        "native_read_in_direct_buffer", Object.class, int.class, new XC_MethodHook() {

                            private ByteBuffer bbTemp;

                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                super.beforeHookedMethod(param);
                                Log.d(TAG, "beforeHookedMethod: AudioRecord#native_read_in_direct_buffer() method hooked");
                                if (!RpProcessMonitorClient.getInstance().isAllowedMicAccessCached()) {
                                    Log.d(TAG, "beforeHookedMethod: AudioRecord#native_read_in_direct_buffer() is not allowed");
                                    //param.args[0] = ByteBuffer.allocate(((ByteBuffer)param.args[0]).capacity());
                                    ByteBuffer bb = ((ByteBuffer)param.args[0]);
                                    bb.put(new byte[bb.capacity()]);

                                    bbTemp = bb.duplicate();

                                    param.setResult(1);
                                }
                            }

                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);
                                if (!RpProcessMonitorClient.getInstance().isAllowedMicAccessCached()) {
                                    Log.d(TAG, "afterHookedMethod: AudioRecord#native_read_in_direct_buffer() is not allowed");
                                    ByteBuffer bb = ((ByteBuffer)param.args[0]);
                                    bb.clear();
                                    bb.put(bbTemp);
                                    param.setResult(1);
                                }
                            }
                        });
            } catch(NoSuchMethodError ei) {
                Log.d(TAG, "init: AudioRecord#native_read_in_direct_buffer() method not found", ei);
            }
        }

        // Hook AudioRecord#native_read_in_direct_buffer()
        // -----------------------------------------------
        try {
            XposedHelpers.findAndHookMethod(AudioRecord.class,
                    "native_read_in_short_array", short[].class, int.class, int.class, boolean.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (!RpProcessMonitorClient.getInstance().isAllowedMicAccessCached()) {
                                Log.d(TAG, "beforeHookedMethod: AudioRecord#native_read_in_short_array() is not allowed");
                                param.args[0] = new short[]{0};
                            }
                        }
                    });
        } catch (NoSuchMethodError e) {
            try {
                XposedHelpers.findAndHookMethod(AudioRecord.class,
                        "native_read_in_short_array", short[].class, int.class, int.class, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                super.beforeHookedMethod(param);
                                if (!RpProcessMonitorClient.getInstance().isAllowedMicAccessCached()) {
                                    Log.d(TAG, "beforeHookedMethod: AudioRecord#native_read_in_short_array() is not allowed");
                                    param.args[0] = new short[]{0};
                                }
                            }
                        });
            } catch (NoSuchMethodError ei) {
                Log.d(TAG, "init: AudioRecord#native_read_in_short_array() method not found", ei);
            }
        }
    }

    public void notifyMicAccessUpdated() {

    }

}
