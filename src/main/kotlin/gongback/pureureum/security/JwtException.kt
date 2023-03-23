package gongback.pureureum.security

open class JwtException(val code: Int = 401, message: String) : RuntimeException(message)

class JwtNotExistsException(code: Int = 401, message: String = "Jwt가 존재하지 않습니다") : JwtException(code, message)

class JwtNotValidException(code: Int = 401, message: String = "Jwt가 유효하지 않습니다") : JwtException(code, message)

class JwtExpiredException(code: Int = 420, message: String = "Jwt가 만료되었습니다") : JwtException(code, message)
