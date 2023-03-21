package gongback.pureureum.application.exception

import gongback.pureureum.application.dto.ErrorCode

open class PureureumException(message: String? = null, cause: Throwable? = null, val errorCode: ErrorCode?) :
    RuntimeException(errorCode?.message ?: message, cause)

class SmsSendException(cause: Throwable? = null, errorCode: ErrorCode) :
    PureureumException(errorCode.message, cause, errorCode)
