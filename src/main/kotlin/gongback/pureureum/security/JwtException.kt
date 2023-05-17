package gongback.pureureum.security

open class JwtException(val code: Int, message: String? = null) : RuntimeException(message)

class JwtNotExistsException(code: Int = 430, message: String = "토큰이 존재하지 않습니다") : JwtException(code, message)

class JwtNotValidException(code: Int = 430, message: String = "유효하지 않은 토큰입니다") : JwtException(code, message)

class JwtExpiredException(code: Int = 431, message: String = "만료된 토큰입니다") : JwtException(code, message)
