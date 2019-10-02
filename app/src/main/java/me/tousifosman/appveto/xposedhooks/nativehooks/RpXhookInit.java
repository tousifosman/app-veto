package me.tousifosman.appveto.xposedhooks.nativehooks;

import android.support.annotation.NonNull;

public class RpXhookInit {

    private static RpXhookInit instance;

    private RpXhookInit() {}

    @NonNull
    private static RpXhookInit getInstance() {
        if (instance == null)
            instance = new RpXhookInit();
        return instance;
    }

    public static void init() {
        getInstance().getInstance().hookNative();
    }

    private native void hookNative();

}

