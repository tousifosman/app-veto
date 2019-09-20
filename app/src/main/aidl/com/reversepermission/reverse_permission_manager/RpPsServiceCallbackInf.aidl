// RpPsServiceCallbackInf.aidl
package com.reversepermission.reverse_permission_manager;

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
