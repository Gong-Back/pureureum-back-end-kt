package gongback.pureureum.security

open class JwtException(message: String? = null) :
    RuntimeException(message)

class JwtNotExistsException(message: String? = null) : JwtException(message)

class JwtNotValidException(message: String? = null) : JwtException(message)

class JwtExpiredException(message: String? = null) : JwtException(message)
