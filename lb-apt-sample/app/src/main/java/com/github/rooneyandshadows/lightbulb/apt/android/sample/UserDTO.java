package com.github.rooneyandshadows.lightbulb.apt.android.sample;

import com.github.rooneyandshadows.lightbulb.apt.android.core.parcelable.BlankParcelable;
import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbParcelable;

import java.util.UUID;


@LightbulbParcelable
public class UserDTO extends BlankParcelable {
    public UUID uuid;
    public String text;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}