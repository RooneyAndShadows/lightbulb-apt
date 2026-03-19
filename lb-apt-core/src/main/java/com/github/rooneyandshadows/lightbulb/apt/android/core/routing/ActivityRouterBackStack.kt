package com.github.rooneyandshadows.lightbulb.apt.android.core.routing

import android.os.Parcel
import android.os.Parcelable
import com.github.rooneyandshadows.lightbulb.apt.android.core.utils.ParcelUtils
import java.util.HashMap

class ActivityRouterBackStack() : Parcelable {
    private var stack: MutableList<String>
    private var stackIndex: MutableMap<String, Int> = mutableMapOf()

    init {
        stack = mutableListOf()
    }

    constructor(parcel: Parcel) : this() {
        stack = ParcelUtils.readStringList(parcel)!!
        stackIndex = ParcelUtils.readHashMap(parcel, String::class.java, Int::class.java)
    }

    fun getScreenPosition(screenName: String): Int {
        return stackIndex.getOrDefault(screenName, -1)
    }

    fun hasScreen(screenName: String): Boolean {
        return stackIndex.containsKey(screenName)
    }

    fun getEntriesCount(): Int {
        return stack.size
    }

    fun add(screenName: String) {
        stack.add(screenName)
        stackIndex.putIfAbsent(screenName, stack.size - 1)
    }

    fun removeAt(position: Int): Boolean {
        if (position >= stack.size) {
            return false
        }

        val removed = stack.removeAt(position)
        stackIndex.remove(removed)
        for (i in position until stack.size) {
            stackIndex[stack[i]] = i
        }

        return true
    }

    fun pop(): String? {
        if (stack.isEmpty())
            return null
        val screenName = stack.removeLast()
        stackIndex.remove(screenName)

        return screenName
    }

    fun getCurrent(): String? {
        val last = stack.size - 1
        return getAt(last)
    }

    fun getAt(position: Int): String? {
        return stack.getOrNull(position)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        ParcelUtils.writeStringArrayList(parcel, ArrayList(stack))
        ParcelUtils.writeHashMap(parcel, HashMap(stackIndex))
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ActivityRouterBackStack> {
        override fun createFromParcel(parcel: Parcel): ActivityRouterBackStack {
            return ActivityRouterBackStack(parcel)
        }

        override fun newArray(size: Int): Array<ActivityRouterBackStack?> {
            return arrayOfNulls(size)
        }
    }
}