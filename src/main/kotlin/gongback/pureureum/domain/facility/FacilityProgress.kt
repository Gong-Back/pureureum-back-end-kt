package gongback.pureureum.domain.facility

enum class FacilityProgress(private val description: String) {
    NOT_APPROVED("승인 대기"),
    PENDING("추가 서류 요청"),
    APPROVED("승인 완료"),
    REJECTED("승인 거절")
}
