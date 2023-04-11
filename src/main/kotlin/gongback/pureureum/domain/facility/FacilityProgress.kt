package gongback.pureureum.domain.facility

enum class FacilityProgress(private val description: String) {
    NOT_APPROVED("승인 대기"), APPROVED("승인 완료")
}
