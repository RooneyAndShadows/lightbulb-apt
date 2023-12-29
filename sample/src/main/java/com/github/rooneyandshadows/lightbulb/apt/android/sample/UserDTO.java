package com.github.rooneyandshadows.lightbulb.apt.android.sample;

import android.util.SparseArray;
import com.github.rooneyandshadows.lightbulb.apt.android.core.parcelable.BlankParcelable;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.LightbulbParcelable;

import java.time.OffsetDateTime;
import java.util.*;

@LightbulbParcelable
public class UserDTO extends BlankParcelable {
    public Date date;
    public OffsetDateTime offsetDateTime;
    public UUID uuid;
    public List<String> list;
    public SparseArray<String> arr;
    public UserDTO dto;

    public void setDate(Date date) {
        this.date = date;
    }

    public void setOffsetDateTime(OffsetDateTime offsetDateTime) {
        this.offsetDateTime = offsetDateTime;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public void setArr(SparseArray<String> arr) {
        this.arr = arr;
    }

    public void setDto(UserDTO dto) {
        this.dto = dto;
    }

    public Date getDate() {
        return date;
    }

    public OffsetDateTime getOffsetDateTime() {
        return offsetDateTime;
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<String> getList() {
        return list;
    }

    public SparseArray<String> getArr() {
        return arr;
    }

    public UserDTO getDto() {
        return dto;
    }
}
