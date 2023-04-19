package gongback.pureureum.application.dto

import gongback.pureureum.domain.facility.Facility
import gongback.pureureum.domain.facility.FacilityAddress
import gongback.pureureum.domain.facility.FacilityCategory
import gongback.pureureum.domain.project.Project
import gongback.pureureum.domain.project.ProjectFile
import gongback.pureureum.domain.project.ProjectFileType
import gongback.pureureum.domain.project.ProjectInformation
import gongback.pureureum.domain.project.ProjectPayment
import gongback.pureureum.domain.project.ProjectPaymentType
import gongback.pureureum.domain.project.ProjectStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
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
    @field:Positive
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
    val facilityId: Long
) {
    fun toEntityWithInfo(
        facilityId: Long,
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
    val projectCategory: FacilityCategory,
    val projectStatus: ProjectStatus,
    val paymentType: ProjectPaymentType,
    val projectFiles: List<ProjectFileRes>?,
    val projectPayment: ProjectPaymentRes?
) {
    constructor(
        project: Project,
        facility: Facility,
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
            facility.address,
            project.guide,
            project.notice
        ),
        projectCategory = facility.category,
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
    val facilityAddress: FacilityAddress,
    val guide: String?,
    val notice: String?
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
