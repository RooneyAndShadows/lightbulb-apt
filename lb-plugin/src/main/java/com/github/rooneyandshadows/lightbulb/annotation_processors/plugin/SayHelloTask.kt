package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class SayHelloTask @Inject constructor(private val sayHelloExtension: HelloWorldExtension) : DefaultTask() {


    init {
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val from = sourceSets.asMap["main"]!!.output
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