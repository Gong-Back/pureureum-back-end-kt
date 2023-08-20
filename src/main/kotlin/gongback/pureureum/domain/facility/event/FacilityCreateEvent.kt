package gongback.pureureum.domain.facility.event

import gongback.pureureum.application.dto.FileReq

data class FacilityCreateEvent(
    val facilityId: Long,
    val certificationDoc: List<FileReq>
)
