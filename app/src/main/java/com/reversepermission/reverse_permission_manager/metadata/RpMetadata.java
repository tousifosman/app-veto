package com.reversepermission.reverse_permission_manager.metadata;

import android.content.pm.ApplicationInfo;
import android.hardware.Sensor;
import android.support.annotation.NonNull;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public enum RpMetadata {

    RP_SENOR_ALL("reversepermission_sensor_all", Sensor.TYPE_ALL),
    RP_SENSOR_MAGNETIC_FIELD("reversepermission_sensor_magnetic_field", Sensor.TYPE_MAGNETIC_FIELD),
    RP_SENSOR_ACCELEROMETER("reversepermission_sensor_accelerometer", Sensor.TYPE_ACCELEROMETER),
    RP_SENSOR_GYROSCOPE("reversepermission_sensor_gyroscope", Sensor.TYPE_GYROSCOPE),
    RP_SENSOR_LIGHT("reversepermission_sensor_light", Sensor.TYPE_LIGHT),
    RP_SENSOR_PROXIMITY("reversepermission_sensor_proximity", Sensor.TYPE_PROXIMITY),
    RP_SENSOR_GRAVITY("reversepermission_sensor_gravity", Sensor.TYPE_GRAVITY),

    // Simply add any sensor key (Sensor#Type_*) and string tag and it will work!!

    RP_SENSOR_CAMERA("reversepermission_camera"),
    RP_SENSOR_MIC("reversepermission_mic");

    @Nullable
    private String metaKey;

    private Integer type;

    @Nullable
    private static Set<String> metaKeySet;

    @Nullable
    private static HashMap<Integer, RpMetadata> typeMap;

    RpMetadata(String metaKey) {
        this.metaKey  = metaKey;
    }

    RpMetadata(String metaKey, int type) {
        this.metaKey  = metaKey;
        this.type = type;
    }

    @Contract(pure = true)
    public String getMetaKey() {
        return this.metaKey;
    }

    @Contract(pure = true)
    public int getType() {
        return this.type;
    }

    /**
     * Checks if the instance of {@link RpMetadata} is present in Manifest.xml corresponding the
     * parameter appInfo.
     * @param appInfo
     * @return Returns {@link true} if Metadata is present in the appInfo {@link false} otherwise.
     */
    public boolean isInAppInfo(@NonNull ApplicationInfo appInfo) {
        if (appInfo.metaData != null) {
            Set<String> appMetaKeySet = appInfo.metaData.keySet();
            return appMetaKeySet != null && appMetaKeySet.contains(metaKey);
        }
        return false;
    }

    public static boolean containsAnyRpMetaData(@NotNull ApplicationInfo appInfo) {
        if (appInfo.metaData !=null) {
            Set<String> appMetaKeySet = appInfo.metaData.keySet();
            return appMetaKeySet != null && appMetaKeySet.containsAll(getAllMetaKeys());
        }
        return false;
    }

    @Nullable
    public static RpMetadata typeToMetaData(int type) {
        if (typeMap == null) {
            typeMap = new HashMap<>();
            for (RpMetadata rvMetaData: RpMetadata.values())
                if (rvMetaData.type != null)
                    typeMap.put(rvMetaData.type, rvMetaData);
        }
        return typeMap.get(type);
    }

    @NonNull
    private static Set<String> getAllMetaKeys() {
        if (metaKeySet == null) {
            metaKeySet = new HashSet<>();
            for (RpMetadata rvMetaData: RpMetadata.values())
                metaKeySet.add(rvMetaData.metaKey);
        }
        return metaKeySet;
    }

}
