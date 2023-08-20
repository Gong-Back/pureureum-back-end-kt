package gongback.pureureum.application

import gongback.pureureum.domain.project.ProjectRepository
import gongback.pureureum.domain.project.event.ProjectCreateEvent
import gongback.pureureum.domain.project.event.ProjectDeleteEvent
import gongback.pureureum.domain.project.getProjectById
import gongback.pureureum.support.constant.Category
import io.kotest.assertions.throwables.shouldNotThrowAnyUnit
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
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
            createMockProjectFile("THUMBNAIL", PROJECT_FILE_ORIGINAL_FILE_NAME1, PROJECT_FILE_ORIGINAL_FILE_NAME1, "sample")
        )
        val projectCreateEvent = ProjectCreateEvent(project.id, createFileReq(projectFiles))

        When("올바른 프로젝트 파일 정보와 프로젝트 엔티티 정보라면") {
            every { fileService.validateFileName(any()) } returns PROJECT_FILE_ORIGINAL_FILE_NAME1
            every { fileService.getAnyContentType(any()) } returns PROJECT_FILE_CONTENT_TYPE
            every { fileService.uploadFile(any(), any()) } returns PROJECT_FILE_KEY1
            every { projectRepository.getProjectById(any()) } returns project

            Then("프로젝트에 대한 사진 정보를 저장한다") {
                shouldNotThrowAnyUnit { projectEventListenerHandler.handleProjectCreate(projectCreateEvent) }
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
