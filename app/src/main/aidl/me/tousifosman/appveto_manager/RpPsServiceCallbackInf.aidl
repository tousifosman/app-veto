// RpPsServiceCallbackInf.aidl
package me.tousifosman.appveto_manager;

// Declare any non-default types here with import statements

interface RpPsServiceCallbackInf {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    int getProcessId();
    String getClientPackageName();
    void onFocusAppChanged();
}
