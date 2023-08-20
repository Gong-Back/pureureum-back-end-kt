package gongback.pureureum.application

import gongback.pureureum.domain.project.ProjectRepository
import gongback.pureureum.domain.project.getProjectById
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.getUserByEmail
import gongback.pureureum.support.constant.Category
import io.kotest.assertions.throwables.shouldNotThrowAnyUnit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.spyk
import org.springframework.context.ApplicationEventPublisher
import support.createMockProjectFile
import support.createProject
import support.createProjectRegisterReq
import support.createUser

class ProjectWriteServiceTest : BehaviorSpec({
    val userRepository = mockk<UserRepository>()
    val projectRepository = mockk<ProjectRepository>()
    val applicationEventPublisher = spyk<ApplicationEventPublisher>()
    val projectWriteService = ProjectWriteService(userRepository, projectRepository, applicationEventPublisher)

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
            every { applicationEventPublisher.publishEvent(any()) } just runs

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

    Given("프로젝트 ID, 사용자 Email") {
        val projectId = 0L
        val email = "testEmail"

        When("프로젝트 생성자와 삭제 요청 사용자가 같을 경우") {
            val user = createUser()
            val project = createProject()
            every { userRepository.getUserByEmail(email) } returns user
            every { projectRepository.getProjectById(projectId) } returns project
            every { projectRepository.delete(project) } just runs
            every { applicationEventPublisher.publishEvent(any()) } returns Unit

            Then("정상적으로 삭제") {
                shouldNotThrowAnyUnit { projectWriteService.deleteProject(projectId, email) }
            }
        }

        When("프로젝트 생성자와 삭제 요청 사용자가 다를 경우") {
            val user = createUser()
            val project = createProject(userId = 1L)
            every { userRepository.getUserByEmail(email) } returns user
            every { projectRepository.getProjectById(projectId) } returns project
            every { applicationEventPublisher.publishEvent(any()) } just runs
            every { projectRepository.delete(project) } just runs

            Then("예외 발생") {
                shouldThrow<PureureumException> { projectWriteService.deleteProject(projectId, email) }
            }
        }
    }
})
