package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.logger

import org.gradle.api.internal.tasks.compile.JavaCompilerArgumentsBuilder.LOGGER
import java.util.logging.ConsoleHandler
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.Level.*
import java.util.logging.Logger


class TransformationLogger {
    companion object {
        private val logger: Logger = Logger.getLogger(TransformationLogger::class.java.name)
        private var enabled: Boolean = false

        fun init(enabled: Boolean) {
            this.enabled = enabled
            val handlerObj: Handler = ConsoleHandler()
            handlerObj.level = ALL
            logger.addHandler(handlerObj)
            logger.level = ALL
            logger.useParentHandlers = false
        }

        fun info(message: String) {
            if (!enabled) return
            logger.info(message)
        }

        fun warning(message: String) {
            if (!enabled) return
            logger.warning(message)
        }

        fun severe(message: String) {
            if (!enabled) return
            logger.severe(message)
        }
    }
}