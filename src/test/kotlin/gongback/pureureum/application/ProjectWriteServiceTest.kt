package gongback.pureureum.application

import gongback.pureureum.domain.project.ProjectRepository
import gongback.pureureum.domain.project.getProjectById
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.getUserByEmail
import gongback.pureureum.support.constant.Category
import io.kotest.assertions.throwables.shouldNotThrowAnyUnit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import support.PROJECT_FILE_CONTENT_TYPE
import support.PROJECT_FILE_KEY1
import support.PROJECT_FILE_ORIGINAL_FILE_NAME1
import support.createMockProjectFile
import support.createProject
import support.createProjectRegisterReq
import support.createUser

class ProjectWriteServiceTest : BehaviorSpec({
    val userRepository = mockk<UserRepository>()
    val projectRepository = mockk<ProjectRepository>()
    val fileService = mockk<FileService>()
    val projectWriteService = ProjectWriteService(userRepository, projectRepository, fileService)

    Given("사용자 이메일과 프로젝트 정보") {
        val user = createUser()
        val projectRegisterReq = createProjectRegisterReq()

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
                shouldNotThrowAnyUnit { projectWriteService.registerProject(email, projectRegisterReq, null) }
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
            every { projectRepository.save(any()) } returns project

            Then("정상적으로 저장된다.") {
                shouldNotThrowAnyUnit {
                    projectWriteService.registerProject(
                        email,
                        createProjectRegisterReq(),
                        projectFileList
                    )
                }
            }
        }
    }

    Given("저장된 프로젝트 아이디, 프로젝트 파일들") {
        val user = createUser()
        val project = createProjectRegisterReq().toEntityWithInfo(
            userId = user.id,
            facilityId = 0L,
            projectCategory = Category.FARMING_HEALING
        )
        val projectId = project.id
        val projectFiles = listOf(
            createMockProjectFile("projectFiles", PROJECT_FILE_ORIGINAL_FILE_NAME1, PROJECT_FILE_ORIGINAL_FILE_NAME1, "sample")
        )

        When("올바른 프로젝트 파일 정보와 프로젝트 엔티티 정보라면") {
            every { fileService.validateAnyContentType(any()) } returns PROJECT_FILE_CONTENT_TYPE
            every { fileService.validateFileName(any()) } returns PROJECT_FILE_ORIGINAL_FILE_NAME1
            every { fileService.uploadFile(any(), any()) } returns PROJECT_FILE_KEY1
            every { projectRepository.getProjectById(any()) } returns project

            projectWriteService.saveProjectFiles(projectId, projectFiles)

            Then("프로젝트에 대한 사진 정보를 저장한다") {
                verify(exactly = 0) { projectRepository.deleteById(projectId) }
            }
        }

        When("프로젝트 파일에 대한 올바르지 않은 컨텐츠 타입이 들어왔을 경우") {
            clearMocks(fileService, projectRepository)

            every { fileService.validateAnyContentType(any()) } throws IllegalArgumentException("파일 형식이 유효하지 않습니다")
            every { projectRepository.deleteById(any()) } just runs

            Then("저장되었던 프로젝트에 대한 정보를 제거하고, 예외가 발생한다") {
                shouldThrow<FileHandlingException> {
                    projectWriteService.saveProjectFiles(projectId, projectFiles)
                }
                verify(exactly = 1) { projectRepository.deleteById(projectId) }
            }
        }

        When("프로젝트 파일에 대한 파일 이름이 올바르지 않을 경우") {
            clearMocks(fileService, projectRepository)

            every { fileService.validateAnyContentType(any()) } returns PROJECT_FILE_CONTENT_TYPE
            every { fileService.validateFileName(any()) } throws IllegalArgumentException("원본 파일 이름이 존재하지 않습니다")
            every { projectRepository.deleteById(any()) } just runs

            Then("저장되었던 프로젝트에 대한 정보를 제거하고, 예외가 발생한다") {
                shouldThrow<FileHandlingException> {
                    projectWriteService.saveProjectFiles(projectId, projectFiles)
                }
                verify(exactly = 1) { projectRepository.deleteById(projectId) }
            }
        }

        When("파일 업로드 도중 실패했을 경우") {
            clearMocks(fileService, projectRepository)

            every { fileService.validateAnyContentType(any()) } returns PROJECT_FILE_CONTENT_TYPE
            every { fileService.validateFileName(any()) } returns PROJECT_FILE_ORIGINAL_FILE_NAME1
            every { fileService.uploadFile(any(), any()) } throws S3Exception()
            every { projectRepository.deleteById(any()) } just runs

            Then("저장되었던 프로젝트에 대한 정보를 제거하고, 예외가 발생한다") {
                shouldThrow<FileHandlingException> {
                    projectWriteService.saveProjectFiles(projectId, projectFiles)
                }
                verify(exactly = 1) { projectRepository.deleteById(projectId) }
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
            every { projectRepository.delete(project) } just runs

            Then("정상적으로 삭제") {
                shouldNotThrowAnyUnit { projectWriteService.deleteProject(projectId, email) }
            }
        }

        When("프로젝트 생성자와 삭제 요청 사용자가 다를 경우") {
            val user = createUser()
            val project = createProject(userId = 1L)
            every { userRepository.getUserByEmail(email) } returns user
            every { projectRepository.getProjectById(projectId) } returns project
            every { projectRepository.delete(project) } just runs

            Then("예외 발생") {
                shouldThrow<PureureumException> { projectWriteService.deleteProject(projectId, email) }
            }
        }
    }

    Given("삭제할 프로젝트 파일 키 리스트") {
        val projectFileKeys = listOf(
            PROJECT_FILE_KEY1
        )

        When("올바른 파일에 대한 키 정보라면") {
            every { fileService.deleteFile(any()) } just runs

            Then("프로젝트에 대한 사진 정보를 제거한다") {
                shouldNotThrowAnyUnit { projectWriteService.deleteProjectFiles(projectFileKeys) }
            }
        }
    }
})
