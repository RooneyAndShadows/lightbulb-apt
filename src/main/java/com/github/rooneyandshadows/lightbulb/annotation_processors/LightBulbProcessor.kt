package com.github.rooneyandshadows.lightbulb.annotation_processors

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.*
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.AnnotationReader
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.CodeGenerator
import com.google.auto.service.AutoService
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

@AutoService(Processor::class)
class LightBulbProcessor : AbstractProcessor() {
    private var filer: Filer? = null
    private var messager: Messager? = null
    private var elements: Elements? = null
    private var options: Map<String, String>? = null
    private var rootPackage: String? = null

    @Synchronized
    override fun init(processingEnvironment: ProcessingEnvironment) {
        filer = processingEnvironment.filer
        messager = processingEnvironment.messager
        elements = processingEnvironment.elementUtils
        options = processingEnvironment.options
    }

    override fun process(set: Set<TypeElement?>, roundEnvironment: RoundEnvironment): Boolean {
        rootPackage = getRootPackage()
        var processResult: Boolean
        val reader = AnnotationReader(
            messager!!, elements!!
        )
        processResult = reader.obtainAnnotatedClassesWithActivityConfiguration(roundEnvironment)
        processResult = processResult and reader.obtainAnnotatedClassesWithFragmentConfiguration(roundEnvironment)
        processResult = processResult and reader.obtainAnnotatedClassesWithFragmentScreen(roundEnvironment)
        processResult = processResult and reader.obtainAnnotatedFieldsWithBindView(roundEnvironment)
        processResult = processResult and reader.obtainAnnotatedFieldsWithFragmentParameter(roundEnvironment)
        if (!processResult) return false
        val fragmentInfoList = reader.fragmentInfoList
        val activityInfoList = reader.activityInfoList
        val screenGroups = reader.screenGroups
        val generator = CodeGenerator(
            rootPackage!!, filer!!
        )
        generator.generateFragmentBindingClasses(fragmentInfoList)
        activityInfoList.forEach { activityInfo ->
            if (!activityInfo.isRoutingEnabled) return@forEach
            generator.generateRoutingScreens(screenGroups)
            generator.generateRouterClass(activityInfo.className!!, screenGroups)
        }
        return true
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return mutableSetOf(
            ActivityConfiguration::class.java.canonicalName,
            BindView::class.java.canonicalName,
            FragmentConfiguration::class.java.canonicalName,
            FragmentParameter::class.java.canonicalName,
            FragmentScreen::class.java.canonicalName
        )
    }

    override fun getSupportedOptions(): Set<String> {
        return mutableSetOf(ROOT_PACKAGE)

    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    private fun getRootPackage(): String? {
        val rootPackage = options!![ROOT_PACKAGE]
        if (rootPackage == null || rootPackage == "") {
            val className = javaClass.simpleName
            val message =
                "$className: Failed to generate sources.Please provide \"$ROOT_PACKAGE\" argument in annotationProcessorOptions."
            messager!!.printMessage(Diagnostic.Kind.ERROR, message)
        }
        return rootPackage
    }
}