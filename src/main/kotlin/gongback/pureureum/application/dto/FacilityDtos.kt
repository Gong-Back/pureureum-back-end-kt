package gongback.pureureum.application.dto

import gongback.pureureum.domain.facility.Facility
import gongback.pureureum.domain.facility.FacilityAddress
import gongback.pureureum.domain.facility.FacilityCertificationDoc
import gongback.pureureum.domain.facility.FacilityProgress
import gongback.pureureum.support.constant.Category
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class FacilityReq(
    val category: Category,

    @field:NotBlank
    val name: String,

    @field:NotBlank
    @field:Size(min = 1, max = 20)
    val city: String,

    @field:NotBlank
    @field:Size(min = 1, max = 20)
    val county: String,

    @field:NotBlank
    @field:Size(min = 1, max = 20)
    val district: String,

    @field:NotBlank
    @field:Size(min = 1, max = 100)
    val jibun: String,

    val detail: String,

    @field:NotBlank
    @field:Size(min = 1, max = 20)
    val longitude: String,

    @field:NotBlank
    @field:Size(min = 1, max = 20)
    val latitude: String
) {
    fun toFacility(userId: Long): Facility {
        val progress = when (category) {
            Category.ETC -> FacilityProgress.APPROVED
            else -> FacilityProgress.NOT_APPROVED
        }
        return Facility(
            name,
            FacilityAddress(city, county, district, jibun, detail, longitude, latitude),
            category,
            userId,
            progress
        )
    }
}

data class FacilityRes(
    val id: Long,
    val category: Category,
    val name: String,
    val city: String,
    val county: String,
    val district: String,
    val jibun: String,
    val detail: String,
    val longitude: String,
    val latitude: String
) {
    companion object {
        fun fromFacility(facility: Facility): FacilityRes {
            return FacilityRes(
                facility.id,
                facility.facilityCategory,
                facility.name,
                facility.address.city,
                facility.address.county,
                facility.address.district,
                facility.address.jibun,
                facility.address.detail,
                facility.address.longitude,
                facility.address.latitude
            )
        }
    }
}

data class FacilityResWithProgress(
    val id: Long,
    val category: Category,
    val name: String,
    val city: String,
    val county: String,
    val district: String,
    val jibun: String,
    val detail: String,
    val longitude: String,
    val latitude: String,
    val progress: FacilityProgress
) {
    companion object {
        fun fromFacility(facility: Facility): FacilityResWithProgress = FacilityResWithProgress(
            facility.id,
            facility.facilityCategory,
            facility.name,
            facility.address.city,
            facility.address.county,
            facility.address.district,
            facility.address.jibun,
            facility.address.detail,
            facility.address.longitude,
            facility.address.latitude,
            facility.progress
        )
    }
}

data class FacilityWithDocIds(
    val id: Long,
    val category: Category,
    val name: String,
    val city: String,
    val county: String,
    val district: String,
    val jibun: String,
    val detail: String,
    val longitude: String,
    val latitude: String,
    val fileIds: List<Long>
) {
    companion object {
        fun fromFacility(facility: Facility, docIds: List<Long>): FacilityWithDocIds = FacilityWithDocIds(
            facility.id,
            facility.facilityCategory,
            facility.name,
            facility.address.city,
            facility.address.county,
            facility.address.district,
            facility.address.jibun,
            facility.address.detail,
            facility.address.longitude,
            facility.address.latitude,
            docIds
        )
    }
}

data class FacilityCertificationDocDto(
    val fileKey: String,
    val contentType: String,
    val originalFileName: String
) {
    fun toEntity(): FacilityCertificationDoc = FacilityCertificationDoc(
        fileKey,
        contentType,
        originalFileName
    )
}
