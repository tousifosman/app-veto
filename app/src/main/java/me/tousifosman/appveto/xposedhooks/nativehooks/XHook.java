package me.tousifosman.appveto.xposedhooks.nativehooks;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.content.pm.ApplicationInfo;

/**
 * Created by caikelun on 18/01/2018.
 */

public class XHook {

    private static final String TAG = "Xhook";

    private static final XHook ourInstance = new XHook();
    private static boolean inited = false;

    public static XHook getInstance() {
        return ourInstance;
    }

    private XHook() {}

    /**
     * Check if xhook has inited.
     * @return true if xhook has inited, false otherwise.
     */
    public synchronized boolean isInited() {
        return inited;
    }

    /**
     * Init xhook.
     * @param ctx The application context.
     * @return true if successful, false otherwise.
     */
    public synchronized boolean init(Context ctx) {
        if(inited) {
            return true;
        }
        Log.d(TAG, "init: Trying to init xhook -> " + ctx.getPackageName());
        /*try {
            //System.loadLibrary("xhook");
            Log.d(TAG, "init: Trying to load default location -> " + getLibDefaultLocation(ctx));
            System.load(getLibDefaultLocation(ctx));
            inited = true;
        } catch (Throwable e) {
            Log.e(TAG, "init: Could not load native library from default directory", e);
            try {
                Log.d(TAG, "init: Trying to load secondary location -> " + getLibLocation(ctx));
                System.load(getLibLocation(ctx));
                //System.loadLibrary("rpxhook");
                inited = true;
            } catch (Throwable ex) {
                Log.e(TAG, "failed to load native library", ex);
            }
        }*/

        try {
            Log.d(TAG, "init: Trying to load 64 bit library -> " + getLibLocation64());
            // System.load(getLibLocation64());
            System.loadLibrary("rpxhook");
            inited = true;
            return inited;
        } catch (Throwable e) {
            Log.e(TAG, "init: Could not load 64 bit library. ", e);
        }

        /*try {
            Log.d(TAG, "init: Trying to load 32 bit library -> " + getLibLocation32());
            System.load(getLibLocation32());
            inited = true;
        } catch (Throwable e) {
            Log.e(TAG, "init: Could not load 32 bit library. ", e);
        }*/
        return inited;
    }

    /**
     * Re-hook after System.loadLibrary() and System.load().
     * @param async true if to refresh in async mode; otherwise, refresh in sync mode.
     * @return 0 if successful, false otherwise.
     */
    public synchronized void refresh(boolean async) {
        if(!inited) {
            return;
        }

        try {
            NativeHandler.getInstance().refresh(async);
        } catch (Throwable ex) {
            ex.printStackTrace();
            Log.e("xhook", "xhook native refresh failed");
        }
    }

    /**
     * Clear all cache.
     */
    public synchronized void clear() {
        if(!inited) {
            return;
        }

        try {
            NativeHandler.getInstance().clear();
        } catch (Throwable ex) {
            ex.printStackTrace();
            Log.e("xhook", "xhook native clear failed");
        }
    }

    /**
     * Enable/disable the debug log to logcat. (disabled by default)
     * @param flag the bool flag.
     */
    public synchronized void enableDebug(boolean flag) {
        if(!inited) {
            return;
        }

        try {
            NativeHandler.getInstance().enableDebug(flag);
        } catch (Throwable ex) {
            ex.printStackTrace();
            Log.e("xhook", "xhook native enableDebug failed");
        }
    }

    /**
     * Enable/disable the segmentation fault protection. (enabled by default)
     * @param flag the bool flag.
     */
    public synchronized void enableSigSegvProtection(boolean flag) {
        if (!inited) {
            return;
        }

        try {
            NativeHandler.getInstance().enableSigSegvProtection(flag);
        } catch (Throwable ex) {
            ex.printStackTrace();
            Log.e("xhook", "xhook native enableSigSegvProtection failed");
        }
    }

    public static String getLibLocation(Context context) throws PackageManager.NameNotFoundException {

        String[] nativeDirPathComponents = context.getApplicationInfo().nativeLibraryDir.split("/");
        String libDirComponent = nativeDirPathComponents[nativeDirPathComponents.length - 1];

        return context.getPackageManager()
                .getPackageInfo("me.tousifosman.appveto", 0)
                .applicationInfo.nativeLibraryDir + "/../" + libDirComponent + "/librpxhook.so";
    }

    public static String getLibDefaultLocation(Context context) throws PackageManager.NameNotFoundException {
        return context.getPackageManager()
                .getPackageInfo("me.tousifosman.appveto", 0)
                .applicationInfo.nativeLibraryDir + "/librpxhook.so";
    }

    public static String getLibLocation64() {
        return "/data/local/tmp/me.tousifosman.appveto/lib/arm64-v8a/librpxhook.so";
    }

    public static String getLibLocation32() {
        return "/data/local/tmp/me.tousifosman.appveto/lib/armeabi-v7a/librpxhook.so";
    }
}
