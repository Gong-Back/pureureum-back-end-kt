package gongback.pureureum.domain.user

enum class Role(
    val description: String
) {
    ROLE_USER("유저"), ROLE_ADMIN("관리자")
}
