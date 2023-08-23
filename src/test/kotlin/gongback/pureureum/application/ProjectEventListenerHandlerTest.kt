package gongback.pureureum.application

import gongback.pureureum.domain.project.ProjectRepository
import gongback.pureureum.domain.project.event.ProjectCreateEvent
import gongback.pureureum.domain.project.event.ProjectDeleteEvent
import gongback.pureureum.domain.project.getProjectById
import gongback.pureureum.support.constant.Category
import io.kotest.assertions.throwables.shouldNotThrowAnyUnit
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
import support.createFileReq
import support.createMockProjectFile
import support.createProjectRegisterReq
import support.createUser

class ProjectEventListenerHandlerTest : BehaviorSpec({
    val fileService = mockk<FileService>()
    val projectRepository = mockk<ProjectRepository>()
    val projectEventListenerHandler = ProjectEventListenerHandler(fileService, projectRepository)

    Given("프로젝트 생성 이벤트") {
        val user = createUser()
        val project = createProjectRegisterReq().toEntityWithInfo(
            userId = user.id,
            facilityId = 0L,
            projectCategory = Category.FARMING_HEALING
        )
        val projectFiles = listOf(
            createMockProjectFile("projectFiles", PROJECT_FILE_ORIGINAL_FILE_NAME1, PROJECT_FILE_ORIGINAL_FILE_NAME1, "sample")
        )
        val projectCreateEvent = ProjectCreateEvent(project.id, createFileReq(projectFiles))

        When("올바른 프로젝트 파일 정보와 프로젝트 엔티티 정보라면") {
            every { fileService.validateAnyContentType(any()) } returns PROJECT_FILE_CONTENT_TYPE
            every { fileService.validateFileName(any()) } returns PROJECT_FILE_ORIGINAL_FILE_NAME1
            every { fileService.uploadFile(any(), any()) } returns PROJECT_FILE_KEY1
            every { projectRepository.getProjectById(any()) } returns project

            projectEventListenerHandler.handleProjectCreate(projectCreateEvent)

            Then("프로젝트에 대한 사진 정보를 저장한다") {
                verify(exactly = 0) { projectRepository.deleteById(projectCreateEvent.projectId) }
            }
        }

        When("프로젝트 파일에 대한 올바르지 않은 컨텐츠 타입이 들어왔을 경우") {
            clearMocks(fileService, projectRepository)

            every { fileService.validateAnyContentType(any()) } throws IllegalArgumentException("파일 형식이 유효하지 않습니다")
            every { projectRepository.deleteById(any()) } just runs

            projectEventListenerHandler.handleProjectCreate(projectCreateEvent)

            Then("저장되었던 프로젝트에 대한 정보를 제거한다") {
                verify(exactly = 1) { projectRepository.deleteById(projectCreateEvent.projectId) }
            }
        }

        When("프로젝트 파일에 대한 파일 이름이 올바르지 않을 경우") {
            clearMocks(fileService, projectRepository)

            every { fileService.validateAnyContentType(any()) } returns PROJECT_FILE_CONTENT_TYPE
            every { fileService.validateFileName(any()) } throws IllegalArgumentException("원본 파일 이름이 존재하지 않습니다")
            every { projectRepository.deleteById(any()) } just runs

            projectEventListenerHandler.handleProjectCreate(projectCreateEvent)

            Then("저장되었던 프로젝트에 대한 정보를 제거한다") {
                verify(exactly = 1) { projectRepository.deleteById(projectCreateEvent.projectId) }
            }
        }

        When("파일 업로드 도중 실패했을 경우") {
            clearMocks(fileService, projectRepository)

            every { fileService.validateAnyContentType(any()) } returns PROJECT_FILE_CONTENT_TYPE
            every { fileService.validateFileName(any()) } returns PROJECT_FILE_ORIGINAL_FILE_NAME1
            every { fileService.uploadFile(any(), any()) } throws S3Exception()
            every { projectRepository.deleteById(any()) } just runs

            projectEventListenerHandler.handleProjectCreate(projectCreateEvent)

            Then("저장되었던 프로젝트에 대한 정보를 제거한다") {
                verify(exactly = 1) { projectRepository.deleteById(projectCreateEvent.projectId) }
            }
        }
    }

    Given("프로젝트 삭제 이벤트") {
        val projectFileKeys = listOf(
            PROJECT_FILE_KEY1
        )
        val projectDeleteEvent = ProjectDeleteEvent(projectFileKeys)

        When("올바른 파일에 대한 키 정보라면") {
            every { fileService.deleteFile(any()) } just runs

            Then("프로젝트에 대한 사진 정보를 제거한다") {
                shouldNotThrowAnyUnit { projectEventListenerHandler.handleProjectDelete(projectDeleteEvent) }
            }
        }
    }
})
