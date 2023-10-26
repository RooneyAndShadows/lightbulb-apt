package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin

import com.android.build.gradle.api.AndroidBasePlugin
import com.github.rooneyandshadows.lightbulb.annotation_processors.MyTransformation
import com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.tasks.TransformationTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.create

class HelloWorldPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        var configured = false

        project.plugins.withType(AndroidBasePlugin::class.java) {
            configured = true
            //configureHilt(project)
        }

        val extension = project.extensions.create<HelloWorldExtension>("greeting")
        val task = project.tasks.register("hello", SayHelloTask::class.java, extension)
        val task1 = project.tasks.register("transform1", TransformationTask::class.java)
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        // sourceSets.asMap["main"]!!.java

        /*myCustomIndexerTask.addSourceSetContainer(sourceSets)
        project.subprojects.forEach { sp ->
            sp.whenPluginAdded(JavaPlugin) { jp ->
                def javaPluginConvention = sp.getConvention().getPlugin(JavaPluginConvention)
                def sourceSets = javaPluginConvention.getSourceSets()
                myCustomIndexerTask.addSourceSetContainer(sourceSets)
            }
        }*/
        task1.get().apply {
            from(sourceSets.asMap["main"]!!.output)
            transformation = MyTransformation()
        }


        //task transform4(type: TransformationTask) {
//    classpath += configurations.compile

        // from sourceSets.main.output

        //transformation = new MyTransformation()
//}


        project.tasks.getByName("compileJava").doLast {
            task1.get().exec()
        }
        project.tasks.getByName("compileJava").doLast {
            task.get().greet()
        }
    }
}