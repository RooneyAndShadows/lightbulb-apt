package com.github.rooneyandshadows.lightbulb.annotation_processors.plugin.logger

import java.util.logging.*
import java.util.logging.Level.*


class LoggingUtil {
    companion object {
        private val logger: Logger = Logger.getLogger(LoggingUtil::class.java.name)
        var enabled: Boolean = false

        init {
            val handlerObj: Handler = ConsoleHandler()
            handlerObj.formatter = LoggerFormatter()
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

    internal class LoggerFormatter : Formatter() {
        override fun format(record: LogRecord): String {
            val sb = StringBuilder()
            sb.append(record.level).append(':')
            sb.append(record.message).append('\n')
            return sb.toString()
        }
    }
}