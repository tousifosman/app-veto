package me.tousifosman.appveto_manager.metadata_manager;

import android.content.pm.ApplicationInfo;
import android.hardware.Sensor;
import android.support.annotation.NonNull;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Tousif Osman
 * Responsible for creating individual meta-data.
 */
public enum RpMetadata {

    RP_SENOR_ALL("appveto_sensor_all", Sensor.TYPE_ALL),
    RP_SENSOR_MAGNETIC_FIELD("appveto_sensor_magnetic_field", Sensor.TYPE_MAGNETIC_FIELD),
    RP_SENSOR_ACCELEROMETER("appveto_sensor_accelerometer", Sensor.TYPE_ACCELEROMETER),
    RP_SENSOR_GYROSCOPE("appveto_sensor_gyroscope", Sensor.TYPE_GYROSCOPE),
    RP_SENSOR_LIGHT("appveto_sensor_light", Sensor.TYPE_LIGHT),
    RP_SENSOR_PROXIMITY("appveto_sensor_proximity", Sensor.TYPE_PROXIMITY),
    RP_SENSOR_GRAVITY("appveto_sensor_gravity", Sensor.TYPE_GRAVITY),
    RP_SENSOR_STEP_COUNTER("appveto_sensor_step_counter", Sensor.TYPE_STEP_COUNTER),

    // Simply add any sensor key (Sensor#Type_*) and string tag and it will work!!

    RP_SENSOR_CAMERA("appveto_camera"),
    RP_SENSOR_MIC("appveto_mic");

    @Nullable
    private String metaKey;

    private Integer type;

    @Nullable
    private static Set<String> metaKeySet;

    @Nullable
    private static HashMap<String, RpMetadata> map;

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
    public Integer getType() {
        return this.type;
    }

    /**
     * Checks if the instance of {@link RpMetadata} is present in Manifest.xml corresponding the
     * parameter appInfo.
     * @param appInfo
     * @return Returns {@link true} if RpMetadata is present in the appInfo {@link false} otherwise.
     */
    public boolean isInAppInfo(@NonNull ApplicationInfo appInfo) {
        if (appInfo.metaData != null) {
            Set<String> appMetaKeySet = appInfo.metaData.keySet();
            return appMetaKeySet != null && appMetaKeySet.contains(metaKey);
        }
        return false;
    }

    /**
     * Checks if the passed {@link ApplicationInfo} object of some App has any AppVeto meta-data.
     * @param appInfo
     * @return
     */
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
    public static Set<String> getAllMetaKeys() {
        if (metaKeySet == null) {
            metaKeySet = new HashSet<>();
            for (RpMetadata metaData: RpMetadata.values()) {
                metaKeySet.add(metaData.metaKey);
            }
        }
        return metaKeySet;
    }

    public static RpMetadata keyToMetadata(String key) {
        if (map == null) {
            map = new HashMap<>();
            for (RpMetadata metadata: RpMetadata.values()) {
                map.put(metadata.metaKey, metadata);
            }
        }
        return map.get(key);
    }

    public static class MetadataModel {
        public RpMetadata[] metadata;
        public RpGroupMetadata[] groupMetadata;

        public MetadataModel(RpMetadata[] metadata, RpGroupMetadata[] groupMetadata) {
            this.metadata = metadata;
            this.groupMetadata = groupMetadata;
        }

        /*public static MetadataModel createEmptyModel() {
            MetadataModel metadataModel  = new MetadataModel();
            meta
            return metadataModel;
        }*/
    }
}
