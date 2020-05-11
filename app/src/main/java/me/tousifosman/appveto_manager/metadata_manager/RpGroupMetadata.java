package me.tousifosman.appveto_manager.metadata_manager;

import android.support.annotation.NonNull;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public enum RpGroupMetadata {
    RP_INFERENCE_KEYSTROKE("appveto_inferance_keystroke", new RpMetadata[]{});

    private static Set<String> groupMetaKeySet;

    @Nullable
    private String groupMetaKey;

    @Nullable
    private RpMetadata[] rpMetadataGroup;

    private static HashMap<String, RpGroupMetadata> groupMap;

    RpGroupMetadata(@NonNull String groupMetaKey, @Nullable RpMetadata[] rpMetadataGroup) {
        this.groupMetaKey = groupMetaKey;
        this.rpMetadataGroup = rpMetadataGroup;
    }

    @Contract(pure = true)
    public String getGroupMetaKey() {
        return this.groupMetaKey;
    }

    @NonNull
    public static Set<String> getAllGroupMetaKeys() {
        if (groupMetaKeySet == null) {
            groupMetaKeySet = new HashSet<>();
            for (RpGroupMetadata groupMetadata: RpGroupMetadata.values()) {
                groupMetaKeySet.add(groupMetadata.groupMetaKey);
            }
        }
        return groupMetaKeySet;
    }

    public static RpGroupMetadata keyToGroupMetadata(String key) {
        if (groupMap == null) {
            groupMap = new HashMap<>();
            for (RpGroupMetadata groupMetadata: RpGroupMetadata.values()) {
                groupMap.put(groupMetadata.groupMetaKey, groupMetadata);
            }
        }
        return groupMap.get(key);
    }

}
