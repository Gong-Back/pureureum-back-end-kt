package gongback.pureureum.security

open class JwtException(message: String) : RuntimeException(message)

class JwtNotExistsException(message: String = "Jwt가 존재하지 않습니다") : JwtException(message)

class JwtNotValidException(message: String = "Jwt가 유효하지 않습니다") : JwtException(message)

class JwtExpiredException(message: String = "Jwt가 만료되었습니다") : JwtException(message)
