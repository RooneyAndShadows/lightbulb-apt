package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin

import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class HelloWorldPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        var configured = false

        project.plugins.withType(AndroidBasePlugin::class.java) {
            configured = true
            //configureHilt(project)
        }


        //val extension = project.extensions.create<HelloWorldExtension>("greeting")
        //val task = project.tasks.register("hello", SayHelloTask::class.java, extension)
        //project.tasks.getByName("preBuild").doFirst {
        //    task.get().greet()
        //}
    }
}