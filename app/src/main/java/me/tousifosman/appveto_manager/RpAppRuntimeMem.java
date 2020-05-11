package me.tousifosman.appveto_manager;

public class RpAppRuntimeMem {

    private static RpAppRuntimeMem instance;

    private RpProcessUtils.CurrentFocusedApp currentFocusedApp;
    public boolean flagFocus;

    public static RpAppRuntimeMem getInstance() {
        if (instance == null)
            instance = new RpAppRuntimeMem();
        return instance;
    }

    /*public void updateCurrentFocusApp() {
        currentFocusedApp = RpProcessUtils.getCurrentFocusedApp();
    }*/

    public RpProcessUtils.CurrentFocusedApp getCurrentFocusedApp() {
        return currentFocusedApp;
    }
}
