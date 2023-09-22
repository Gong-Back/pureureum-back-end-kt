package gongback.pureureum.application

import gongback.pureureum.domain.facility.FacilityRepository
import gongback.pureureum.domain.facility.getFacilityById
import gongback.pureureum.domain.project.ProjectRepository
import gongback.pureureum.domain.project.getProjectById
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.getUserByEmail
import gongback.pureureum.domain.user.getUserById
import gongback.pureureum.support.constant.Category
import gongback.pureureum.support.constant.FileType
import io.kotest.assertions.throwables.shouldNotThrowAnyUnit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import support.PROJECT_CATEGORY
import support.PROJECT_THUMBNAIL_KEY
import support.SEARCH_TYPE_POPULAR
import support.createDifferentCategoryProject
import support.createFacility
import support.createMockProjectFile
import support.createProject
import support.createProjectRegisterReq
import support.createProjectResWithoutPayment
import support.createSameCategoryProject
import support.createUser
import java.time.LocalDate

class ProjectServiceTest : BehaviorSpec({
    val projectRepository = mockk<ProjectRepository>()
    val facilityRepository = mockk<FacilityRepository>()
    val userRepository = mockk<UserRepository>()
    val fileService = mockk<FileService>()
    val projectService = ProjectService(userRepository, projectRepository, facilityRepository, fileService)

    Given("사용자 이메일과 프로젝트 정보") {
        val user = createUser()
        val projectRegisterReq = createProjectRegisterReq()
        val fileKey = "test-key"

        When("프로젝트 정보가 유효하고, 프로젝트 사진들이 없을 경우") {
            val email = user.email
            val project = projectRegisterReq.toEntityWithInfo(
                userId = user.id,
                facilityId = 0L,
                projectCategory = Category.FARMING_HEALING
            )
            every { userRepository.getUserByEmail(any()) } returns user
            every { projectRepository.save(any()) } returns project

            Then("정상적으로 저장된다.") {
                shouldNotThrowAnyUnit { projectService.registerProject(email, projectRegisterReq, null) }
            }
        }

        When("프로젝트 정보가 유효하고, 프로젝트 사진들이 있을 경우") {
            val email = user.email
            val project = createProjectRegisterReq().toEntityWithInfo(
                userId = user.id,
                facilityId = 0L,
                projectCategory = Category.FARMING_HEALING
            )
            val projectFileList = listOf(
                createMockProjectFile("THUMBNAIL", "test1", "image/png", "sample"),
                createMockProjectFile("COMMON", "test2", "image/png", "sample")
            )

            every { userRepository.getUserByEmail(any()) } returns user
            every { fileService.validateFileName(any()) } returns "OriginalFilename"
            every { fileService.getAnyContentType(any()) } returns "image/png"
            every { fileService.uploadFile(any(), FileType.PROJECT, any()) } returns fileKey
            every { projectRepository.save(any()) } returns project
            Then("정상적으로 저장된다.") {
                shouldNotThrowAnyUnit {
                    projectService.registerProject(
                        email,
                        createProjectRegisterReq(),
                        projectFileList
                    )
                }
            }
        }
    }

    Given("프로젝트 ID") {
        val projectId = 0L

        When("프로젝트 ID에 맞는 프로젝트가 있을 경우") {
            val project = createProject()
            val facility = createFacility()
            val projectRes = createProjectResWithoutPayment(project, facility.address)
            every { projectRepository.getProjectById(projectId) } returns project
            every { facilityRepository.findFacilityById(project.facilityId) } returns facility
            every { fileService.getFileUrl(any()) } returns "signedUrl"

            Then("정상적으로 조회된다.") {
                projectService.getProject(projectId) shouldBe projectRes
            }
        }
    }

    Given("프로젝트 ID, 사용자 Email") {
        val projectId = 0L
        val email = "testEmail"

        When("프로젝트 생성자와 삭제 요청 사용자가 같을 경우") {
            val user = createUser()
            val project = createProject()
            every { userRepository.getUserByEmail(email) } returns user
            every { projectRepository.getProjectById(projectId) } returns project
            every { fileService.deleteFile(any()) } just runs
            every { projectRepository.delete(project) } just runs

            Then("정상적으로 삭제") {
                shouldNotThrowAnyUnit { projectService.deleteProject(projectId, email) }
            }
        }

        When("프로젝트 생성자와 삭제 요청 사용자가 다를 경우") {
            val user = createUser()
            val project = createProject(userId = 1L)
            every { userRepository.getUserByEmail(email) } returns user
            every { projectRepository.getProjectById(projectId) } returns project
            every { fileService.deleteFile(any()) } just runs
            every { projectRepository.delete(project) } just runs

            Then("예외 발생") {
                shouldThrow<PureureumException> { projectService.deleteProject(projectId, email) }
            }
        }
    }

    Given("검색 조건, 카테고리, 페이지 조건") {
        val searchType = SEARCH_TYPE_POPULAR
        val pageable = Pageable.ofSize(10)

        When("모든 프로젝트에 대한 페이지 조회 일 경우") {
            val category = null
            val facility = createFacility()
            val projectOwner = createUser()
            val projects = createDifferentCategoryProject(facility, projectOwner)

            every {
                projectRepository.getRunningProjectsByCategoryOrderedSearchType(
                    searchType,
                    category,
                    pageable,
                    LocalDate.now()
                )
            } returns PageImpl(projects, pageable, projects.size.toLong())

            every { facilityRepository.getFacilityById(any()) } returns facility

            every { userRepository.getUserById(any()) } returns projectOwner

            every { fileService.getFileUrl(any()) } returns PROJECT_THUMBNAIL_KEY

            Then("모든 카테고리의 프로젝트 Page 조회 성공") {
                projectService.getRunningProjectPartsByTypeAndCategory(searchType, category, pageable).size shouldBe 3
            }
        }

        When("카테고리가 지정된 경우") {
            val category = PROJECT_CATEGORY
            val facility = createFacility()
            val projectOwner = createUser()
            val projects = createSameCategoryProject(facility, projectOwner)

            every {
                projectRepository.getRunningProjectsByCategoryOrderedSearchType(
                    searchType,
                    category,
                    pageable,
                    LocalDate.now()
                )
            } returns PageImpl(projects, pageable, projects.size.toLong())

            every { facilityRepository.getFacilityById(any()) } returns facility

            every { userRepository.getUserById(any()) } returns projectOwner

            every { fileService.getFileUrl(any()) } returns PROJECT_THUMBNAIL_KEY

            Then("같은 카테고리의 프로젝트 Page 조회 성공") {
                projectService.getRunningProjectPartsByTypeAndCategory(searchType, category, pageable)
                    .projectList[0].projectCategory shouldBe category
            }
        }
    }
})
