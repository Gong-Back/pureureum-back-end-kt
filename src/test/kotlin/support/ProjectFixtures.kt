package support

import gongback.pureureum.application.dto.ProjectFileRes
import gongback.pureureum.application.dto.ProjectPartPageRes
import gongback.pureureum.application.dto.ProjectPartRes
import gongback.pureureum.application.dto.ProjectRegisterReq
import gongback.pureureum.application.dto.ProjectRes
import gongback.pureureum.application.dto.ProjectfileDto
import gongback.pureureum.domain.facility.Facility
import gongback.pureureum.domain.facility.FacilityAddress
import gongback.pureureum.domain.project.Project
import gongback.pureureum.domain.project.ProjectFileType
import gongback.pureureum.domain.project.ProjectInformation
import gongback.pureureum.domain.project.ProjectPayment
import gongback.pureureum.domain.project.ProjectPaymentType
import gongback.pureureum.domain.project.ProjectStatus
import gongback.pureureum.domain.projectapply.ProjectApply
import gongback.pureureum.domain.user.User
import gongback.pureureum.domain.user.UserInformation
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

const val PROJECT_FILE_KEY1 = "project/test1.png"
const val PROJECT_FILE_CONTENT_TYPE = "image/png"
const val PROJECT_FILE_ORIGINAL_FILE_NAME1 = "test1"
const val PROJECT_FILE_KEY2 = "project/test2.png"
const val PROJECT_FILE_ORIGINAL_FILE_NAME2 = "test2"

fun createProject(
    userId: Long = 0L,
    title: String = PROJECT_TITLE,
    category: Category = PROJECT_CATEGORY,
    projectStartDate: String = PROJECT_START_DATE,
    projectEndDate: String = PROJECT_END_DATE,
    facilityId: Long = 0L
): Project {
    return Project(
        ProjectInformation(
            title = title,
            introduction = PROJECT_INTRODUCTION,
            content = PROJECT_CONTENT,
            projectStartDate = LocalDate.parse(projectStartDate),
            projectEndDate = LocalDate.parse(projectEndDate),
            totalRecruits = PROJECT_TOTAL_RECRUITS
        ),
        ProjectStatus.RUNNING,
        userId,
        facilityId,
        ProjectPaymentType.NONE,
        category,
        listOf(
            createProjectFileDto(
                PROJECT_FILE_KEY1,
                PROJECT_FILE_CONTENT_TYPE,
                PROJECT_FILE_ORIGINAL_FILE_NAME1,
                ProjectFileType.THUMBNAIL
            ).toEntity(),
            createProjectFileDto(
                PROJECT_FILE_KEY2,
                PROJECT_FILE_CONTENT_TYPE,
                PROJECT_FILE_ORIGINAL_FILE_NAME2,
                ProjectFileType.COMMON
            ).toEntity()
        )
    )
}

fun createProjectFileDto(
    fileKey: String = PROJECT_FILE_KEY1,
    contentType: String = PROJECT_FILE_CONTENT_TYPE,
    originalFilename: String = PROJECT_FILE_ORIGINAL_FILE_NAME1,
    fileType: ProjectFileType = ProjectFileType.COMMON
): ProjectfileDto = ProjectfileDto(fileKey, contentType, originalFilename, fileType)

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
            createProjectFileDto(
                PROJECT_FILE_KEY1,
                PROJECT_FILE_CONTENT_TYPE,
                PROJECT_FILE_ORIGINAL_FILE_NAME1,
                ProjectFileType.THUMBNAIL
            ).toEntity(),
            createProjectFileDto(
                PROJECT_FILE_KEY2,
                PROJECT_FILE_CONTENT_TYPE,
                PROJECT_FILE_ORIGINAL_FILE_NAME2,
                ProjectFileType.COMMON
            ).toEntity()
        ),
        listOf(ProjectPayment(100000, "환불 정책", "예금주(계좌번호)"))
    )
}

fun createProjectRegisterReq(
    title: String = PROJECT_TITLE,
    introduction: String = PROJECT_INTRODUCTION,
    content: String = PROJECT_CONTENT,
    startDate: LocalDate = LocalDate.parse(PROJECT_START_DATE),
    endDate: LocalDate = LocalDate.parse(PROJECT_END_DATE),
    totalRecruits: Int = PROJECT_TOTAL_RECRUITS,
    paymentType: ProjectPaymentType = ProjectPaymentType.NONE,
    facilityId: Long = 1L,
    category: Category = PROJECT_CATEGORY
): ProjectRegisterReq {
    return ProjectRegisterReq(
        title = title,
        introduction = introduction,
        content = content,
        projectStartDate = startDate,
        projectEndDate = endDate,
        totalRecruits = totalRecruits,
        paymentType = paymentType,
        facilityId = facilityId,
        projectCategory = category
    )
}

fun createProjectResWithoutPayment(
    project: Project = createProject(),
    facilityAddress: FacilityAddress = createFacility().address,
    owner: User = createUser()
): ProjectRes = ProjectRes(
    project,
    facilityAddress,
    listOf(
        ProjectFileRes("signedUrl", ProjectFileType.THUMBNAIL),
        ProjectFileRes("signedUrl", ProjectFileType.COMMON)
    ),
    owner.information
)

fun createProjectResWithPayment(): ProjectRes = ProjectRes(
    createProjectWithPayment(),
    createFacility().address,
    listOf(
        ProjectFileRes("signedUrl", ProjectFileType.THUMBNAIL),
        ProjectFileRes("signedUrl", ProjectFileType.COMMON)
    ),
    createUser().information
)

fun createMockProjectFile(
    name: String,
    originalFilename: String,
    contentType: String,
    content: String
): MockMultipartFile = MockMultipartFile(name, originalFilename, contentType, content.toByteArray())

fun createProjectPartPageRes(projects: List<Project>, projectOwner: User): ProjectPartPageRes {
    val facility = createFacility()
    val projectPartResList =
        projects.map { createProjectPartRes(it, facility.address, projectOwner.information) }
            .toList()

    return ProjectPartPageRes(0, 1, projectPartResList.size, projectPartResList)
}

fun createProjectPartRes(
    project: Project,
    facilityAddress: FacilityAddress,
    projectOwner: UserInformation
) =
    ProjectPartRes(
        project,
        facilityAddress,
        ProjectFileRes("signedUrl", ProjectFileType.THUMBNAIL),
        projectOwner
    )

fun createSameCategoryProject(facility: Facility, projectOwner: User): List<Project> {
    val project1 =
        createProject(
            title = "testTitle1",
            facilityId = facility.id,
            userId = projectOwner.id
        ).apply { IntStream.rangeClosed(0, 10).forEach { _ -> this.addLikeCount() } }
    val project2 =
        createProject(
            title = "testTitle2",
            facilityId = facility.id,
            userId = projectOwner.id
        ).apply { IntStream.rangeClosed(0, 5).forEach { _ -> this.addLikeCount() } }
    return listOf(project1, project2)
}

fun createDifferentCategoryProject(facility: Facility, projectOwner: User): List<Project> {
    val project1 =
        createProject(
            title = "testTitle1",
            facilityId = facility.id,
            userId = projectOwner.id
        ).apply { IntStream.rangeClosed(0, 10).forEach { _ -> this.addLikeCount() } }
    val project2 =
        createProject(
            title = "testTitle2",
            facilityId = facility.id,
            userId = projectOwner.id,
            category = Category.ETC
        ).apply {
            IntStream.rangeClosed(0, 5).forEach { _ -> this.addLikeCount() }
        }
    val project3 = createProject(title = "testTitle3", category = Category.FARMING_EXPERIENCE)
    return listOf(project1, project2, project3)
}

fun createProjectApply(
    projectId: Long = 0L,
    userId: Long = 0L
): ProjectApply {
    return ProjectApply(projectId, userId)
}
