package com.github.rooneyandshadows.lightbulb.apt.android.sample;

import android.os.Parcel;
import com.github.rooneyandshadows.lightbulb.apt.android.core.parcelable.BlankParcelable;
import com.github.rooneyandshadows.lightbulb.apt.annotations.IgnoreParcel;
import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbParcelable;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;


@LightbulbParcelable
public class UserDTO extends BlankParcelable {
    private final UUID uuid;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final Date dateRegistered;
    private final boolean active;
    @IgnoreParcel
    private Object ignoredObject = null;

    public UserDTO(UUID uuid, String email, String firstName, String lastName, Date dateRegistered, boolean active) {
        this.uuid = uuid;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateRegistered = dateRegistered;
        this.active = active;
    }

    public void setIgnoredObject(Object ignoredObject) {
        this.ignoredObject = ignoredObject;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Date getDateRegistered() {
        return dateRegistered;
    }

    public boolean isActive() {
        return active;
    }

    public Object getIgnoredObject() {
        return ignoredObject;
    }
}