package me.tousifosman.appveto_manager.metadata_manager;

import android.support.annotation.NonNull;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public enum RpGroupMetadata {
    RP_INFERENCE_KEYSTROKE("reversepermission_inferance_keystroke", new RpMetadata[]{});

    @Nullable
    private String groupMetaKey;

    @Nullable
    private RpMetadata[] rpMetadataGroup;

    RpGroupMetadata(@NonNull String groupMetaKey, @Nullable RpMetadata[] rpMetadataGroup) {
        this.groupMetaKey = groupMetaKey;
        this.rpMetadataGroup = rpMetadataGroup;
    }

    @Contract(pure = true)
    public String getGroupMetaKey() {
        return this.groupMetaKey;
    }

}
