package gongback.pureureum.application.dto

import gongback.pureureum.domain.facility.Facility
import gongback.pureureum.domain.facility.FacilityAddress
import gongback.pureureum.domain.facility.FacilityProgress
import gongback.pureureum.support.constant.Category
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class FacilityReq(
    @NotNull
    val category: Category,

    @NotBlank
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
    @field:Size(min = 1, max = 20)
    val detail: String
) {
    fun toFacility(userId: Long): Facility {
        val progress = when (category) {
            Category.ETC -> FacilityProgress.APPROVED
            else -> FacilityProgress.NOT_APPROVED
        }
        return Facility(
            name,
            FacilityAddress(city, county, district, detail),
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
    val detail: String
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
                facility.address.detail
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
    val detail: String,
    val progress: FacilityProgress
) {
    companion object {
        fun fromFacility(facility: Facility): FacilityResWithProgress {
            return FacilityResWithProgress(
                facility.id,
                facility.facilityCategory,
                facility.name,
                facility.address.city,
                facility.address.county,
                facility.address.district,
                facility.address.detail,
                facility.progress
            )
        }
    }
}

data class FacilityWithDocIds(
    val id: Long,
    val category: Category,
    val name: String,
    val city: String,
    val county: String,
    val district: String,
    val detail: String,
    val fileIds: List<Long>
) {
    companion object {
        fun fromFacility(facility: Facility, docIds: List<Long>): FacilityWithDocIds {
            return FacilityWithDocIds(
                facility.id,
                facility.facilityCategory,
                facility.name,
                facility.address.city,
                facility.address.county,
                facility.address.district,
                facility.address.detail,
                docIds
            )
        }
    }
}
