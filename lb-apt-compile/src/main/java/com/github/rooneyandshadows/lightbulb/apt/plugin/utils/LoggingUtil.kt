package com.github.rooneyandshadows.lightbulb.apt.plugin.utils

import java.util.logging.*
import java.util.logging.Level.*


class LoggingUtil {
    companion object {
        private val logger: Logger = Logger.getLogger(LoggingUtil::class.java.name)
        var enabled: Boolean = false

        init {
            setupHandler()
            setupLogger()
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

        private fun setupHandler() {
            logger.handlers.forEach { handler ->
                logger.removeHandler(handler)
            }
            val handlerObj: Handler = ConsoleHandler()
            handlerObj.formatter = LoggerFormatter()
            handlerObj.level = ALL
            logger.addHandler(handlerObj)
        }

        private fun setupLogger() {
            logger.level = ALL
            logger.useParentHandlers = false
        }
    }

    internal class LoggerFormatter : Formatter() {
        override fun format(record: LogRecord): String {
            val sb = StringBuilder()
            sb.append(record.level).append(':')
            sb.append(record.message).append('\n')
            return sb.toString()
        }
    }
}