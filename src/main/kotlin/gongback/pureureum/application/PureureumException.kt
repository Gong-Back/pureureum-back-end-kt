package gongback.pureureum.application

import gongback.pureureum.application.dto.ErrorCode

open class PureureumException(message: String? = null, cause: Throwable? = null, val errorCode: ErrorCode) :
    RuntimeException(message ?: errorCode.message, cause)

class SmsSendException(cause: Throwable? = null) :
    PureureumException(cause = cause, errorCode = ErrorCode.SMS_SEND_FAILED)

class OAuthAuthenticationException(cause: Throwable? = null) :
    PureureumException(cause = cause, errorCode = ErrorCode.OAUTH_AUTHENTICATION_FAIL)

class S3Exception(cause: Throwable? = null) :
    PureureumException(cause = cause, errorCode = ErrorCode.S3_UPLOAD_FAILED)

class FileHandlingException(cause: Throwable?) :
    PureureumException(message = cause?.message, cause = cause, errorCode = ErrorCode.FILE_HANDLING_FAILED)
