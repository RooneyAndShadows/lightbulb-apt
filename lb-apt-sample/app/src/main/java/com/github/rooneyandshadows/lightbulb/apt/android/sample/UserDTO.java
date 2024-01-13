package com.github.rooneyandshadows.lightbulb.apt.android.sample;

import android.os.Parcel;
import com.github.rooneyandshadows.lightbulb.apt.android.core.parcelable.BlankParcelable;
import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbParcelable;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.UUID;


@LightbulbParcelable
public class UserDTO extends BlankParcelable {
    private UUID uuid;
    private String email;
    private String firstName;
    private String lastName;
    private Date dateRegistered;
    private boolean active;
    private Object obj;

    public UserDTO(UUID uuid, String email, String firstName, String lastName, Date dateRegistered, boolean active) {
        this.uuid = uuid;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateRegistered = dateRegistered;
        this.active = active;
    }

    public UserDTO(@NotNull Parcel parcel) {
        super(parcel);
        this.active = false;
    }
}