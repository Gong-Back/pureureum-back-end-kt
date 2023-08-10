package gongback.pureureum.application.dto

import gongback.pureureum.domain.facility.FacilityAddress
import gongback.pureureum.domain.project.Project
import gongback.pureureum.domain.project.ProjectFile
import gongback.pureureum.domain.project.ProjectFileType
import gongback.pureureum.domain.project.ProjectInformation
import gongback.pureureum.domain.project.ProjectPayment
import gongback.pureureum.domain.project.ProjectPaymentType
import gongback.pureureum.domain.project.ProjectStatus
import gongback.pureureum.domain.user.UserInformation
import gongback.pureureum.support.constant.Category
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.data.domain.Page
import java.time.LocalDate

data class ProjectRegisterReq(
    @field:NotBlank
    @field:Size(min = 1, max = 200)
    val title: String,

    @field:NotBlank
    @field:Size(min = 1, max = 200)
    val introduction: String,

    @field:NotBlank
    @field:Size(min = 1, max = 500)
    val content: String,

    @field:NotNull
    val projectStartDate: LocalDate,

    @field:NotNull
    val projectEndDate: LocalDate,

    @field:NotNull
    val totalRecruits: Int,

    val minAge: Int = -1,
    val maxAge: Int = -1,

    val guide: String? = null,
    val notice: String? = null,

    @field:NotNull
    val paymentType: ProjectPaymentType,

    val amount: Int = 0,
    val refundInstruction: String? = null,
    val depositInformation: String? = null,

    @field:NotNull
    val facilityId: Long,

    @field:NotNull
    val projectCategory: Category
) {
    fun toEntityWithInfo(
        facilityId: Long,
        projectCategory: Category,
        projectFileList: List<ProjectFile> = emptyList(),
        userId: Long
    ) = Project(
        projectInformation = ProjectInformation(
            title = title,
            introduction = introduction,
            content = content,
            projectStartDate = projectStartDate,
            projectEndDate = projectEndDate,
            totalRecruits = totalRecruits,
            minAge = minAge,
            maxAge = maxAge,
            guide = guide,
            notice = notice
        ),
        projectStatus = ProjectStatus.RUNNING,
        projectCategory = projectCategory,
        userId = userId,
        facilityId = facilityId,
        paymentType = paymentType,
        projectFiles = projectFileList,
        payments = when (paymentType) {
            ProjectPaymentType.NONE -> emptyList()
            else -> listOf(ProjectPayment(amount, refundInstruction, depositInformation))
        }
    )
}

data class ProjectRes(
    val projectInformation: ProjectInformationRes,
    val projectCategory: Category,
    val projectStatus: ProjectStatus,
    val paymentType: ProjectPaymentType,
    val projectFiles: List<ProjectFileRes>?,
    val projectPayment: ProjectPaymentRes?
) {
    constructor(
        project: Project,
        address: FacilityAddress,
        projectFileRes: List<ProjectFileRes>
    ) : this(
        projectInformation = ProjectInformationRes(
            project.title,
            project.introduction,
            project.content,
            project.projectStartDate,
            project.projectEndDate,
            project.likeCount,
            project.recruits,
            project.totalRecruits,
            project.minAge,
            project.maxAge,
            FacilityAddressRes(address),
            project.guide,
            project.notice
        ),
        projectCategory = project.projectCategory,
        projectStatus = project.projectStatus,
        paymentType = project.paymentType,
        projectFiles = projectFileRes,
        projectPayment = project.payments?.let {
            ProjectPaymentRes(
                it.amount,
                it.refundInstruction,
                it.depositInformation
            )
        }
    )
}

data class FacilityAddressRes(
    val city: String,
    val county: String,
    val district: String,
    val jibun: String,
    val detail: String,
    val longitude: String,
    val latitude: String
) {
    constructor(facilityAddress: FacilityAddress) : this(
        facilityAddress.city,
        facilityAddress.county,
        facilityAddress.district,
        facilityAddress.jibun,
        facilityAddress.detail,
        facilityAddress.longitude,
        facilityAddress.latitude
    )
}

/**
 * 프로젝트 전체 정보 Dto
 */
data class ProjectInformationRes(
    val title: String,
    val introduction: String,
    val content: String,
    val projectStartDate: LocalDate,
    val projectEndDate: LocalDate,
    val likeCount: Int = 0,
    val recruits: Int = 0,
    val totalRecruits: Int,
    val minAge: Int = -1,
    val maxAge: Int = -1,
    val facilityAddress: FacilityAddressRes,
    val guide: String?,
    val notice: String?
)

data class ProjectPartInformationRes(
    val id: Long,
    val title: String,
    val likeCount: Int,
    val projectStartDate: LocalDate,
    val projectEndDate: LocalDate,
    val recruits: Int = 0,
    val totalRecruits: Int,
    val facilityAddress: FacilityAddress,
    val ownerName: String
)

data class ProjectPaymentRes(
    val amount: Int = 0,
    val refundInstruction: String? = null,
    val depositInformation: String? = null
)

data class ProjectFileRes(
    val projectFileUrl: String,
    val projectFileType: ProjectFileType
)

/**
 * 프로젝트 일부 정보가 들어있는 PageDto
 */
data class ProjectPartPageRes(
    val page: Int,
    val totalPages: Int,
    val size: Int,
    val projectList: List<ProjectPartRes>
) {
    constructor(
        page: Int,
        projectPartResPage: Page<ProjectPartRes>
    ) : this(
        page,
        projectPartResPage.totalPages,
        projectPartResPage.content.size,
        projectPartResPage.content.toList()
    )
}

/**
 * 프로젝트 일부 정보 Dto
 */
data class ProjectPartRes(
    val projectPartInformation: ProjectPartInformationRes,
    val projectCategory: Category,
    val thumbnailFileRes: ProjectFileRes?
) {
    constructor(
        project: Project,
        address: FacilityAddress,
        thumbnailFileRes: ProjectFileRes?,
        owner: UserInformation
    ) : this(
        ProjectPartInformationRes(
            project.id,
            project.title,
            project.likeCount,
            project.projectStartDate,
            project.projectEndDate,
            project.recruits,
            project.totalRecruits,
            address,
            owner.name
        ),
        project.projectCategory,
        thumbnailFileRes
    )
}
