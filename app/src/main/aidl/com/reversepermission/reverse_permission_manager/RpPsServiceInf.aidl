// RpPsServiceInf.aidl
package com.reversepermission.reverse_permission_manager;

import com.reversepermission.reverse_permission_manager.RpPsServiceCallbackInf;

// Declare any non-default types here with import statements

interface RpPsServiceInf {



    String getCurFocusApp();
    String getCurFocusActivity();
    void setCurFocusApp(String appName, String activityName);
    void setServiceStatus(boolean flag);

    boolean isServiceActive();

    boolean isAllowSensor(int sensorType);
    boolean isAllowCamera();
    boolean isAllowedMic();

    void registerCallback(RpPsServiceCallbackInf callbackInf);
}
