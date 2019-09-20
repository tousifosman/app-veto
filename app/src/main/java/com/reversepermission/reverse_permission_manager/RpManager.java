package com.reversepermission.reverse_permission_manager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.reversepermission.reverse_permission_manager.metadata.RpGroupMetadata;
import com.reversepermission.reverse_permission_manager.metadata.RpMetadata;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 26 Mar, 2019
 * @author Tousif Osman
 * @version 1
 */
public class RpManager {

    private static RpManager instance;

    @NonNull
    private WeakReference<Context> contextWeakReference;

    /**
     * Hidden public constructor.
     */
    private RpManager(Context context) {
        contextWeakReference = new WeakReference<>(context);
    }

    public static RpManager bindWith(Context context) {
        if (instance == null) {
            instance = new RpManager(context);
            return instance;
        }

        instance.contextWeakReference = new WeakReference<>(context);
        return instance;
    }

    @NonNull
    public List<ApplicationInfo> getAllRvPackageAppInfo() {

        List<ApplicationInfo> appInfoList = contextWeakReference.get().getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        List<ApplicationInfo> rvAppInfoList = new LinkedList<>();

        for (ApplicationInfo appInfo: appInfoList) {
            if (RpMetadata.containsAnyRpMetaData(appInfo)) {
                rvAppInfoList.add(appInfo);
            }
        }

        return rvAppInfoList;
    }

    /**
     * Check if the the meta string of the parameter metaData ({@link RpMetadata}) is present in the Manifest.xml
     * file of the Application corresponding to the parameter packageName ({@link String}).
     *
     * ***This method also checks if there is any group  mataData in the App's Manifest.xml that corresponds
     * to the metaData.
     *
     * @param packageName
     * @param metaData
     * @return
     */
    public boolean isReversePermissionInPackage(String packageName, RpMetadata metaData) {
        try {
            ApplicationInfo applicationInfo = contextWeakReference.get().getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if (metaData.isInAppInfo(applicationInfo)){
                return true;
            } else if (applicationInfo.metaData != null){
                Set<String> keySet = applicationInfo.metaData.keySet();
                if (keySet != null) {
                    for (RpGroupMetadata groupMetadata: RpGroupMetadata.values()) {
                        if (keySet.contains(groupMetadata.getGroupMetaKey())) {
                            return true;
                        }
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

}
