package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks

import com.github.rooneyandshadows.lightbulb.annotation_processors.MyTransformation
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.transformation.Transformation
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction

abstract class TransformationsTask : DefaultTask() {
    private val transformations: MutableList<Transformation> = mutableListOf()

    init {


        transformations.add(Transformation(project,MyTransformation(),MyTransformation())
        println("lightbulb:SayHello task has been created.")
    }

    override fun getGroup(): String {
        return "lightbulb"
    }

    @TaskAction
    fun greet() {
        val ext = sayHelloExtension
        println("=================================")
        println(ext.message)
    }

    override fun doLast(action: Action<in Task>): Task {
        return super.doLast(action)
    }


}