package com.github.rooneyandshadows.lightbulb.apt.android.sample;

import android.annotation.SuppressLint;
import android.os.Parcelable;
import com.github.rooneyandshadows.lightbulb.apt.android.core.parcelable.BlankParcelable;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.LightbulbParcelable;
import kotlin.Suppress;

import java.lang.annotation.ElementType;
import java.util.UUID;

@LightbulbParcelable

public class UserDTO implements Parcelable {
    public UUID uuid;

    public UserDTO(UUID uuid) {
        super();
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
