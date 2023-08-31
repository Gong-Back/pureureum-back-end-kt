package gongback.pureureum.application.util

import gongback.pureureum.application.FileHandlingException

class FileErrorHandler {
    companion object {
        fun <T> throwFileHandlingExceptionIfFail(operation: () -> T): T =
            runCatching {
                operation()
            }.getOrElse {
                throw FileHandlingException(it)
            }
    }
}
