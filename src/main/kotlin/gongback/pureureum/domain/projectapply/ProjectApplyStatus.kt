package gongback.pureureum.domain.projectapply

enum class ProjectApplyStatus(
    val description: String
) {
    WAITING("신청 대기 상태"), APPROVED("신청 승인 완료")
}
