package com.github.rooneyandshadows.lightbulb.apt.plugin

open class TransformExtension {
    val debugFlag = 1
    val outputFlag = 2
    val dumpFlag = 4
    val debugAndDumpOnly = debugFlag or dumpFlag
    val outputAndDumpAndDebug = outputFlag or dumpFlag or debugFlag
    var flags = outputFlag

    fun isDebugEnabled(): Boolean {
        return flags and debugFlag != 0
    }

    fun isOutputEnabled(): Boolean {
        return flags and outputFlag != 0
    }

    fun isDumpEnabled(): Boolean {
        return flags and dumpFlag != 0
    }
}