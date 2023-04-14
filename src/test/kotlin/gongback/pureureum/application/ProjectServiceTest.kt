package gongback.pureureum.application

import gongback.pureureum.domain.facility.FacilityRepository
import gongback.pureureum.domain.project.ProjectRepository
import gongback.pureureum.domain.project.getProjectById
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.getUserByEmail
import gongback.pureureum.support.enum.FileType
import io.kotest.assertions.throwables.shouldNotThrowAnyUnit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import support.createFacility
import support.createMockProjectFile
import support.createProject
import support.createProjectRegisterReq
import support.createProjectResWithoutPayment
import support.createUser

class ProjectServiceTest : BehaviorSpec({
    val projectRepository = mockk<ProjectRepository>()
    val facilityRepository = mockk<FacilityRepository>()
    val userRepository = mockk<UserRepository>()
    val uploadService = mockk<UploadService>()
    val projectService = ProjectService(userRepository, projectRepository, facilityRepository, uploadService)

    Given("사용자 이메일과 프로젝트 정보") {
        val user = createUser()
        val projectRegisterReq = createProjectRegisterReq()
        val fileKey = "test-key"

        When("프로젝트 정보가 유효하고, 프로젝트 사진들이 없을 경우") {
            val email = user.email
            val project = projectRegisterReq.toEntityWithInfo(userId = user.id, facilityId = 0L)
            every { userRepository.getUserByEmail(any()) } returns user
            every { projectRepository.save(any()) } returns project

            Then("정상적으로 저장된다.") {
                shouldNotThrowAnyUnit { projectService.registerProject(email, projectRegisterReq, null) }
            }
        }

        When("프로젝트 정보가 유효하고, 프로젝트 사진들이 있을 경우") {
            val email = user.email
            val project = createProjectRegisterReq().toEntityWithInfo(userId = user.id, facilityId = 0L)
            val projectFileList = listOf(
                createMockProjectFile("THUMBNAIL", "test1", "image/png", "sample"),
                createMockProjectFile("COMMON", "test2", "image/png", "sample")
            )

            every { userRepository.getUserByEmail(any()) } returns user
            every { uploadService.validateFileName(any()) } returns "OriginalFilename"
            every { uploadService.getAnyContentType(any()) } returns "image/png"
            every { uploadService.uploadFile(any(), FileType.PROJECT, any()) } returns fileKey
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
            val projectRes = createProjectResWithoutPayment(project, facility)
            every { projectRepository.getProjectById(projectId) } returns project
            every { facilityRepository.getReferenceById(project.facilityId) } returns facility
            every { uploadService.getFileUrl(any()) } returns "signedUrl"

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
            every { uploadService.deleteFile(any()) } just runs
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
            every { uploadService.deleteFile(any()) } just runs
            every { projectRepository.delete(project) } just runs

            Then("예외 발생") {
                shouldThrow<PureureumException> { projectService.deleteProject(projectId, email) }
            }
        }
    }
})
