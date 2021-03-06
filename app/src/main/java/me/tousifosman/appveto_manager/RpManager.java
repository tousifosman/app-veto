package me.tousifosman.appveto_manager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import me.tousifosman.appveto_manager.metadata_manager.RpGroupMetadata;
import me.tousifosman.appveto_manager.metadata_manager.RpMetadata;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
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
                    for (RpGroupMetadata rpGroupMetadata : RpGroupMetadata.values()) {
                        if (keySet.contains(rpGroupMetadata.getGroupMetaKey())) {
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

    public RpMetadata.MetadataModel getAllVetoMetaKeysOfPackage(String packageName) throws PackageManager.NameNotFoundException {
        ApplicationInfo applicationInfo = contextWeakReference.get().getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);

        Set<String> allKeys = RpMetadata.getAllMetaKeys();
        Set<String> allGroupKeys = RpGroupMetadata.getAllGroupMetaKeys();

        Set<RpMetadata> packageKeys = new HashSet<>();
        Set<RpGroupMetadata> packageGroupKeys = new HashSet<>();

        for (String key: applicationInfo.metaData.keySet()) {
            if (allKeys.contains(key)) {
                packageKeys.add(RpMetadata.keyToMetadata(key));
            } else if (allGroupKeys.contains(key)) {
                packageGroupKeys.add(RpGroupMetadata.keyToGroupMetadata(key));
            }
        }

        return new RpMetadata.MetadataModel(packageKeys.toArray(new RpMetadata[0]), packageGroupKeys.toArray(new RpGroupMetadata[0]));
    }

    public int[] getAllSensorVetoMetaKeysOfPackage(String packageName) throws PackageManager.NameNotFoundException {

        Set<Integer> sensorSet = new HashSet<>();
        int maxSensorValue = 0;

        RpMetadata.MetadataModel metadataModel = getAllVetoMetaKeysOfPackage(packageName);

        for (RpMetadata metadata: metadataModel.metadata) {
            if (metadata.getType() != null) {
                sensorSet.add(metadata.getType());
                if (metadata.getType() > maxSensorValue) {
                    maxSensorValue = metadata.getType();
                }
            }
        }

        int[] sensorAccessMap = new int[maxSensorValue + 1];

        for (Integer sensorValue: sensorSet.toArray(new Integer[0])) {
            sensorAccessMap[sensorValue] = 1;
        }

        return sensorAccessMap;
    }

}
