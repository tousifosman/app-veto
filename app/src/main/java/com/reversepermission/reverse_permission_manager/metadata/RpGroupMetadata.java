package com.reversepermission.reverse_permission_manager.metadata;

import android.support.annotation.NonNull;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public enum RpGroupMetadata {
    RP_INFERENCE_KEYSTROKE("reversepermission_inferance_keystroke", new RpMetadata[]{});

    @Nullable
    private String groupMetaKey;

    @Nullable
    private RpMetadata[] metadataGroup;

    RpGroupMetadata(@NonNull String groupMetaKey, @Nullable RpMetadata[] metadataGroup) {
        this.groupMetaKey = groupMetaKey;
        this.metadataGroup = metadataGroup;
    }

    @Contract(pure = true)
    public String getGroupMetaKey() {
        return this.groupMetaKey;
    }

}
