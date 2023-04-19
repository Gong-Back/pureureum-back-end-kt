package support

import gongback.pureureum.application.dto.ProjectFileRes
import gongback.pureureum.application.dto.ProjectRegisterReq
import gongback.pureureum.application.dto.ProjectRes
import gongback.pureureum.domain.facility.Facility
import gongback.pureureum.domain.project.Project
import gongback.pureureum.domain.project.ProjectFile
import gongback.pureureum.domain.project.ProjectFileType
import gongback.pureureum.domain.project.ProjectInformation
import gongback.pureureum.domain.project.ProjectPayment
import gongback.pureureum.domain.project.ProjectPaymentType
import gongback.pureureum.domain.project.ProjectStatus
import org.springframework.mock.web.MockMultipartFile
import java.time.LocalDate

const val PROJECT_TITLE = "testTitle"
const val PROJECT_INTRODUCTION = "testIntroduction"
const val PROJECT_CONTENT = "testContent"
const val PROJECT_START_DATE = "2023-03-10"
const val PROJECT_END_DATE = "2023-03-15"
const val PROJECT_TOTAL_RECRUITS = 10

fun createProject(
    userId: Long = 0L
): Project {
    return Project(
        ProjectInformation(
            title = PROJECT_TITLE,
            introduction = PROJECT_INTRODUCTION,
            content = PROJECT_CONTENT,
            projectStartDate = LocalDate.parse(PROJECT_START_DATE),
            projectEndDate = LocalDate.parse(PROJECT_END_DATE),
            totalRecruits = PROJECT_TOTAL_RECRUITS
        ),
        ProjectStatus.RUNNING,
        userId,
        0L,
        ProjectPaymentType.NONE,
        listOf(
            ProjectFile("project/test1.png", "png", "test1", ProjectFileType.THUMBNAIL),
            ProjectFile("project/test2.png", "png", "test2", ProjectFileType.COMMON)
        )
    )
}

fun createProjectWithPayment(): Project {
    return Project(
        ProjectInformation(
            title = PROJECT_TITLE,
            introduction = PROJECT_INTRODUCTION,
            content = PROJECT_CONTENT,
            projectStartDate = LocalDate.parse(PROJECT_START_DATE),
            projectEndDate = LocalDate.parse(PROJECT_END_DATE),
            totalRecruits = PROJECT_TOTAL_RECRUITS
        ),
        ProjectStatus.RUNNING,
        1L,
        1L,
        ProjectPaymentType.DEPOSIT,
        listOf(
            ProjectFile("project/test1.png", "png", "test1", ProjectFileType.THUMBNAIL),
            ProjectFile("project/test2.png", "png", "test2", ProjectFileType.COMMON)
        ),
        listOf(ProjectPayment(100000, "환불 정책", "예금주(계좌번호)"))
    )
}

fun createProjectRegisterReq(): ProjectRegisterReq {
    return ProjectRegisterReq(
        title = PROJECT_TITLE,
        introduction = PROJECT_INTRODUCTION,
        content = PROJECT_CONTENT,
        projectStartDate = LocalDate.parse(PROJECT_START_DATE),
        projectEndDate = LocalDate.parse(PROJECT_END_DATE),
        totalRecruits = PROJECT_TOTAL_RECRUITS,
        paymentType = ProjectPaymentType.NONE,
        facilityId = 1L
    )
}

fun createProjectResWithoutPayment(
    project: Project = createProject(),
    facility: Facility = createFacility()
): ProjectRes = ProjectRes(
    project,
    facility,
    listOf(
        ProjectFileRes("signedUrl", ProjectFileType.THUMBNAIL),
        ProjectFileRes("signedUrl", ProjectFileType.COMMON)
    )
)

fun createProjectResWithPayment(): ProjectRes = ProjectRes(
    createProjectWithPayment(),
    createFacility(),
    listOf(
        ProjectFileRes("signedUrl", ProjectFileType.THUMBNAIL),
        ProjectFileRes("signedUrl", ProjectFileType.COMMON)
    )
)

fun createMockProjectFile(
    name: String,
    originalFilename: String,
    contentType: String,
    content: String
): MockMultipartFile = MockMultipartFile(name, originalFilename, contentType, content.toByteArray())
