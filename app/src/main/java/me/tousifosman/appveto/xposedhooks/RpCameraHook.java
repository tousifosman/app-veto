package me.tousifosman.appveto.xposedhooks;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;

import me.tousifosman.appveto_manager.RpProcessMonitorClient;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executor;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class RpCameraHook {

    private static final String TAG = RpCameraHook.class.getSimpleName();

    private static RpCameraHook instance;

    private final HashSet<CameraCaptureSession> cameraCaptureSessions = new HashSet<>();
    private final HashSet<Camera> legacyCameras = new HashSet<>();

    public static RpCameraHook getInstance() {
        if (instance == null) instance = new RpCameraHook();
        return instance;
    }

    /**
     * Initialize hooks for camera. Must be called from implementation of
     * {@link de.robv.android.xposed.IXposedHookLoadPackage#handleLoadPackage(XC_LoadPackage.LoadPackageParam)}
     * method.
     */
    static void init(@NotNull final XC_LoadPackage.LoadPackageParam lpparam) throws ClassNotFoundException {

        // -----------------------------------------------------------------------------------------
        // Hook Legacy Camera API
        // -----------------------------------------------------------------------------------------

        // Hook Camera#open() method
        // -------------------------
        /*XposedHelpers.findAndHookMethod(Camera.class, "open", int.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {

                Log.d(TAG, "beforeHookedMethod: Camera#open() method hooked");

                Camera camera = (Camera) handleLegacyRequest(param);
                getInstance().legacyCameras.add(camera);

                Log.d(TAG, "beforeHookedMethod: " + Arrays.toString(getInstance().legacyCameras.toArray()));

                return camera;
            }
        });*/
        XposedHelpers.findAndHookMethod(Camera.class, "open", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if (!RpProcessMonitorClient.getInstance().isAllowedCameraAccessCached()) {
                    Log.d(TAG, "beforeHookedMethod: Camera#open() method not allowed");
                    param.setThrowable(new Exception("Camera is not Allowed to be Access by current Focus App"));
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                getInstance().legacyCameras.add((Camera) param.getResult());
                Log.d(TAG, "afterHookedMethod: " + Arrays.toString(getInstance().legacyCameras.toArray()));
            }
        });

        // Hook Camera#release() method
        // This is method is called by app when it is done using the camera to release the camera
        // --------------------------------------------------------------------------------------
        XposedHelpers.findAndHookMethod(Camera.class, "release", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.d(TAG, "beforeHookedMethod: Camera#release() method hooked");
                getInstance().legacyCameras.remove(param.thisObject);

                Log.d(TAG, "beforeHookedMethod: " + Arrays.toString(getInstance().legacyCameras.toArray()));
            }
        });

        // Hook Camera#takePicture() method
        // --------------------------------
        XposedHelpers.findAndHookMethod(Camera.class, "takePicture",
                Camera.ShutterCallback.class, Camera.PictureCallback.class, Camera.PictureCallback.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        if (!RpProcessMonitorClient.getInstance().isAllowedCameraAccessCached()) {
                            Log.d(TAG, "beforeHookedMethod: Camera#open() method not allowed");
                            param.setThrowable(new Exception("Camera is not Allowed to be Access by current Focus App"));
                        }
                    }
                });

        // Hook Camera#startPreview() method
        // ---------------------------------
        XposedHelpers.findAndHookMethod(Camera.class, "startPreview", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if (!RpProcessMonitorClient.getInstance().isAllowedCameraAccessCached()) {
                    Log.d(TAG, "beforeHookedMethod: Camera#startPreview() method not allowed");
                    param.setThrowable(new Exception("Camera is not Allowed to be Access by current Focus App"));
                }
            }
        });

        // -----------------------------------------------------------------------------------------
        // Hook Camera API2
        // -----------------------------------------------------------------------------------------

        // Hook CaptureRequest.class
        // -------------------------
        final Class captureRequestClass = Class.forName("android.hardware.camera2.CaptureRequest");

        // Hook CaptureRequest Constructor
        // -------------------------------
        /* Interception of constructor is not needed at this point.
        XposedHelpers.findAndHookConstructor("android.hardware.camera2.CaptureRequest", lpparam.classLoader,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        handleCaptureRequestCreate(param.thisObject, captureRequestClass);
                    }
                });


        XposedHelpers.findAndHookConstructor("android.hardware.camera2.CaptureRequest", lpparam.classLoader,
                captureRequestClass, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        handleCaptureRequestCreate(param.thisObject, captureRequestClass);
                    }
                });

        Class cameraMetadataNativeClass = Class.forName("android.hardware.camera2.impl.CameraMetadataNative");
        XposedHelpers.findAndHookConstructor("android.hardware.camera2.CaptureRequest", lpparam.classLoader,
                cameraMetadataNativeClass,*//* boolean.class, int.class, String.class, new Set<String>,*//*
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        handleCaptureRequestCreate(param.thisObject, captureRequestClass);
                    }
                });*/


        // We are suing the Implementation class because Xposed cannot hook abstract methods.
        // Methods we want to hook are abstract in CameraCaptureSession.class
        Class cameraCaptureSessionImplClass = Class.forName("android.hardware.camera2.impl.CameraCaptureSessionImpl");

        // Hook CaptureSessionImpl#CaptureSessionImpl() Constructor
        // --------------------------------------------------------
        Class cameraDeviceImplClass = Class.forName("android.hardware.camera2.impl.CameraDeviceImpl");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {

                XposedHelpers.findAndHookConstructor(cameraCaptureSessionImplClass,
                        int.class, Surface.class, CameraCaptureSession.StateCallback.class, Executor.class, cameraDeviceImplClass, Executor.class, boolean.class, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);
                                getInstance().cameraCaptureSessions.add((CameraCaptureSession) param.thisObject);
                                Log.d(TAG, "afterHookedMethod: New CameraCaptureSession Created: " + getInstance().cameraCaptureSessions.size());
                            }
                        }
                );

            } catch (NoSuchMethodError ei) {
                try {
                    XposedHelpers.findAndHookConstructor(cameraCaptureSessionImplClass,
                            int.class, List.class, CameraCaptureSession.StateCallback.class, Handler.class, cameraDeviceImplClass, Handler.class, boolean.class, new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    super.afterHookedMethod(param);
                                    getInstance().cameraCaptureSessions.add((CameraCaptureSession) param.thisObject);
                                    Log.d(TAG, "afterHookedMethod: New CameraCaptureSession Created: " + getInstance().cameraCaptureSessions.size());
                                }
                            }
                    );
                } catch (NoSuchMethodError e) {
                    Log.d(TAG, "init: CameraCaptureSessionImp Class Constructor does not exists", ei);
                }
            }


            // Hook CaptureSession#capture() Method
            // ------------------------------------
            XposedHelpers.findAndHookMethod(cameraCaptureSessionImplClass,
                    "capture", CaptureRequest.class, CameraCaptureSession.CaptureCallback.class, Handler.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (!RpProcessMonitorClient.getInstance().isAllowedCameraAccessCached()) {
                                Log.d(TAG, "beforeHookedMethod: Capture is not allowed");
                                param.setThrowable(new CameraAccessException(CameraAccessException.CAMERA_DISABLED));
                            }
                        }
                    });


            // Hook CaptureSession#captureBurst() method
            // -----------------------------------------
            XposedHelpers.findAndHookMethod(cameraCaptureSessionImplClass,
                    "captureBurst", List.class, CameraCaptureSession.CaptureCallback.class, Handler.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (!RpProcessMonitorClient.getInstance().isAllowedCameraAccessCached()) {
                                Log.d(TAG, "beforeHookedMethod: CameraCaptureSessionImpl#captureBurst() is not allowed");
                                param.setThrowable(new CameraAccessException(CameraAccessException.CAMERA_DISABLED));
                            }
                        }
                    });


            // Hook CaptureSession#setRepeatingRequest() method
            // ------------------------------------------------
            XposedHelpers.findAndHookMethod(cameraCaptureSessionImplClass,
                    "setRepeatingRequest", CaptureRequest.class, CameraCaptureSession.CaptureCallback.class, Handler.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (!RpProcessMonitorClient.getInstance().isAllowedCameraAccessCached()) {
                                Log.d(TAG, "beforeHookedMethod: CameraCaptureSessionImpl#setRepeatingRequest() is not allowed");
                                param.setThrowable(new CameraAccessException(CameraAccessException.CAMERA_DISABLED));
                            }
                        }
                    });


            // Hook CaptureSession#setRepeationBurst() method
            // ----------------------------------------------
            XposedHelpers.findAndHookMethod(cameraCaptureSessionImplClass,
                    "setRepeatingBurst", List.class, CameraCaptureSession.CaptureCallback.class, Handler.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (!RpProcessMonitorClient.getInstance().isAllowedCameraAccessCached()) {
                                Log.d(TAG, "beforeHookedMethod: CameraCaptureSessionImpl#setRepeatingBurst() is not allowed");
                                param.setThrowable(new CameraAccessException(CameraAccessException.CAMERA_DISABLED));
                            }
                        }
                    });
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void abortRepeatingCaptureRequest() {
        for (CameraCaptureSession captureSession : cameraCaptureSessions.toArray(
                new CameraCaptureSession[cameraCaptureSessions.size()])) {
            try {
                captureSession.abortCaptures();
            } catch (Exception e) {
                Log.e(TAG, "abortRepeatingCaptureRequest: ", e);
                cameraCaptureSessions.remove(captureSession);
            }
        }
    }

    private void startLegacyCameraPreview() {
        for (Camera camera : legacyCameras) {
            try {
                camera.startPreview();
            } catch (Exception e) {
                Log.d(TAG, "startLegacyCameraPreview: Error occurred while trying to Start Preview", e);
                legacyCameras.remove(camera);
            }
        }
        Log.d(TAG, "startLegacyCameraPreview: Legacy Camera Preview Started");
    }

    private void stopLegacyCameraPreview() {
        for (Camera camera : legacyCameras) {
            try {
                camera.stopPreview();
            } catch (Exception e) {
                Log.e(TAG, "stopLegacyCameraPreview: ", e);
                legacyCameras.remove(camera);
            }
        }
        Log.d(TAG, "stopLegacyCameraPreview: Legacy Camera Preview Stopped");
    }

    /**
     * Is called from {@link RpProcessMonitorClient#updateAllowedCameraAccess(Context)} app focus
     * when changes and the client receives updated camera access response from Service.
     */
    public void notifyCameraAccessUpdated() {
        if (RpProcessMonitorClient.getInstance().isAllowedCameraAccessCached()) {
            startLegacyCameraPreview();
            Log.d(TAG, "notifyCameraAccessUpdated: Camera Preview Started");
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                abortRepeatingCaptureRequest();
            }
            stopLegacyCameraPreview();
            Log.d(TAG, "notifyCameraAccessUpdated: Repeating Capture Stopped");
        }
    }
}
