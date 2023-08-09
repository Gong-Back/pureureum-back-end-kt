package support

import gongback.pureureum.application.dto.ProjectFileRes
import gongback.pureureum.application.dto.ProjectPartPageRes
import gongback.pureureum.application.dto.ProjectPartRes
import gongback.pureureum.application.dto.ProjectRegisterReq
import gongback.pureureum.application.dto.ProjectRes
import gongback.pureureum.domain.facility.FacilityAddress
import gongback.pureureum.domain.project.Project
import gongback.pureureum.domain.project.ProjectFile
import gongback.pureureum.domain.project.ProjectFileType
import gongback.pureureum.domain.project.ProjectInformation
import gongback.pureureum.domain.project.ProjectPayment
import gongback.pureureum.domain.project.ProjectPaymentType
import gongback.pureureum.domain.project.ProjectStatus
import gongback.pureureum.support.constant.Category
import gongback.pureureum.support.constant.SearchType
import org.springframework.mock.web.MockMultipartFile
import java.time.LocalDate
import java.util.stream.IntStream

const val PROJECT_TITLE = "testTitle"
const val PROJECT_INTRODUCTION = "testIntroduction"
const val PROJECT_CONTENT = "testContent"
const val PROJECT_START_DATE = "2023-03-10"
const val PROJECT_END_DATE = "2023-03-15"
const val PROJECT_TOTAL_RECRUITS = 10
val PROJECT_CATEGORY = Category.FARMING_HEALING
val SEARCH_TYPE_POPULAR = SearchType.POPULAR
const val PROJECT_THUMBNAIL_KEY = "profile/thumbnail-key"

fun createProject(
    userId: Long = 0L,
    title: String = PROJECT_TITLE,
    category: Category = PROJECT_CATEGORY
): Project {
    return Project(
        ProjectInformation(
            title = title,
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
        category,
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
        PROJECT_CATEGORY,
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
        facilityId = 1L,
        projectCategory = PROJECT_CATEGORY
    )
}

fun createProjectResWithoutPayment(
    project: Project = createProject(),
    facilityAddress: FacilityAddress = createFacility().address
): ProjectRes = ProjectRes(
    project,
    facilityAddress,
    listOf(
        ProjectFileRes("signedUrl", ProjectFileType.THUMBNAIL),
        ProjectFileRes("signedUrl", ProjectFileType.COMMON)
    )
)

fun createProjectResWithPayment(): ProjectRes = ProjectRes(
    createProjectWithPayment(),
    createFacility().address,
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

fun createProjectPartPageRes(projects: List<Project>): ProjectPartPageRes {
    val facility = createFacility()
    val projectPartResList = projects.map { createProjectPartRes(it, facility.address) }.toList()

    return ProjectPartPageRes(0, 1, projectPartResList.size, projectPartResList)
}

fun createProjectPartRes(project: Project, facilityAddress: FacilityAddress) =
    ProjectPartRes(project, facilityAddress, ProjectFileRes("signedUrl", ProjectFileType.THUMBNAIL))

fun createSameCategoryProject(): List<Project> {
    val project1 =
        createProject(title = "testTitle1").apply { IntStream.rangeClosed(0, 10).forEach { _ -> this.addLikeCount() } }
    val project2 =
        createProject(title = "testTitle2").apply { IntStream.rangeClosed(0, 5).forEach { _ -> this.addLikeCount() } }
    return listOf(project1, project2)
}

fun createDifferentCategoryProject(): List<Project> {
    val project1 =
        createProject(title = "testTitle1").apply { IntStream.rangeClosed(0, 10).forEach { _ -> this.addLikeCount() } }
    val project2 =
        createProject(title = "testTitle2", category = Category.ETC).apply {
            IntStream.rangeClosed(0, 5).forEach { _ -> this.addLikeCount() }
        }
    val project3 = createProject(title = "testTitle3", category = Category.FARMING_EXPERIENCE)
    return listOf(project1, project2, project3)
}
