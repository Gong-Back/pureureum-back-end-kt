package gongback.pureureum.application

import gongback.pureureum.domain.project.ProjectFileType
import gongback.pureureum.domain.project.ProjectRepository
import gongback.pureureum.domain.project.getProjectById
import gongback.pureureum.domain.projectapply.ProjectApplyRepository
import gongback.pureureum.domain.projectapply.countByProjectId
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.getUserByEmail
import gongback.pureureum.support.constant.Category
import io.kotest.assertions.throwables.shouldNotThrowAnyUnit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import support.PROJECT_FILE_CONTENT_TYPE
import support.PROJECT_FILE_KEY1
import support.PROJECT_FILE_ORIGINAL_FILE_NAME1
import support.createMockProjectFile
import support.createProjectApply
import support.createProjectFileDto
import support.createProjectRegisterReq
import support.createUser

class ProjectWriteServiceTest : BehaviorSpec({
    val fileService = mockk<FileService>()
    val userRepository = mockk<UserRepository>()
    val projectRepository = mockk<ProjectRepository>()
    val projectApplyRepository = mockk<ProjectApplyRepository>()
    val projectWriteService = ProjectWriteService(fileService, userRepository, projectRepository, projectApplyRepository)

    Given("사용자 이메일과 프로젝트 정보") {
        val user = createUser()
        val project = createProjectRegisterReq().toEntityWithInfo(
            userId = user.id,
            facilityId = 0L,
            projectCategory = Category.FARMING_HEALING
        )

        When("올바른 사용자 정보와 프로젝트 정보가 들어왔다면") {
            val email = user.email

            every { userRepository.getUserByEmail(any()) } returns user
            every { projectRepository.save(any()) } returns project
            every { projectApplyRepository.save(any()) } returns createProjectApply(project.id, user.id)

            Then("정상적으로 저장된다.") {
                projectWriteService.registerProject(email, createProjectRegisterReq()) shouldBe project.id
            }
        }
    }

    Given("프로젝트 파일들") {
        val originalFileName = PROJECT_FILE_ORIGINAL_FILE_NAME1
        val contentType = PROJECT_FILE_CONTENT_TYPE
        val fileKey = PROJECT_FILE_KEY1

        val projectFiles = listOf(
            createMockProjectFile("projectFiles", originalFileName, contentType, "sample")
        )
        val createProjectFileDto =
            createProjectFileDto(fileKey, contentType, originalFileName, ProjectFileType.THUMBNAIL)

        When("올바른 프로젝트 파일 정보라면") {
            every { fileService.validateAnyContentType(any()) } returns contentType
            every { fileService.validateFileName(any()) } returns originalFileName
            every { fileService.uploadFile(any(), any()) } returns fileKey

            Then("파일을 업로드하고, 프로젝트 파일 엔티티를 반환한다") {
                projectWriteService.uploadProjectFiles(projectFiles) shouldBe listOf(createProjectFileDto)
            }
        }

        When("프로젝트 파일에 대한 올바르지 않은 컨텐츠 타입이 들어왔을 경우") {
            every { fileService.validateAnyContentType(any()) } throws IllegalArgumentException("파일 형식이 유효하지 않습니다")

            Then("예외가 발생한다") {
                shouldThrow<FileHandlingException> {
                    projectWriteService.uploadProjectFiles(projectFiles)
                }
            }
        }

        When("프로젝트 파일에 대한 파일 이름이 올바르지 않을 경우") {
            every { fileService.validateAnyContentType(any()) } returns contentType
            every { fileService.validateFileName(any()) } throws IllegalArgumentException("원본 파일 이름이 존재하지 않습니다")

            Then("예외가 발생한다") {
                shouldThrow<FileHandlingException> {
                    projectWriteService.uploadProjectFiles(projectFiles)
                }
            }
        }

        When("파일 업로드 도중 실패했을 경우") {
            every { fileService.validateAnyContentType(any()) } returns contentType
            every { fileService.validateFileName(any()) } returns originalFileName
            every { fileService.uploadFile(any(), any()) } throws S3Exception()

            Then("예외가 발생한다") {
                shouldThrow<FileHandlingException> {
                    projectWriteService.uploadProjectFiles(projectFiles)
                }
            }
        }
    }

    Given("저장된 프로젝트 아이디, 프로젝트 파일 엔티티") {
        val user = createUser()
        val project = createProjectRegisterReq().toEntityWithInfo(
            userId = user.id,
            facilityId = 0L,
            projectCategory = Category.FARMING_HEALING
        )

        val projectFileDto = createProjectFileDto()
        val projectFileDtos = listOf(projectFileDto)
        val projectFiles = listOf(projectFileDto.toEntity())

        When("올바른 프로젝트 아이디와 프로젝트 파일 정보가 들어왔다면") {
            every { projectRepository.getProjectById(any()) } returns project

            Then("프로젝트 파일 정보를 저장한다") {
                shouldNotThrowAnyUnit { projectWriteService.saveProjectFiles(project.id, projectFileDtos) }
                project.projectFiles shouldBe projectFiles
            }
        }
    }

    Given("프로젝트 아이디") {
        val user = createUser()
        val project = createProjectRegisterReq().toEntityWithInfo(
            userId = user.id,
            facilityId = 0L,
            projectCategory = Category.FARMING_HEALING
        )

        When("올바른 프로젝트 아이디가 들어왔다면") {
            every { projectRepository.deleteById(any()) } just runs

            Then("프로젝트 정보를 제거한다") {
                shouldNotThrowAnyUnit { projectWriteService.deleteProject(project.id) }
            }
        }
    }

    Given("프로젝트 아이디, 사용자 이메일") {
        val email = "testEmail"
        val user = createUser()
        val project = createProjectRegisterReq().toEntityWithInfo(
            userId = user.id,
            facilityId = 0L,
            projectCategory = Category.FARMING_HEALING
        )

        When("프로젝트 생성자와 삭제 요청 사용자가 같을 경우") {
            every { userRepository.getUserByEmail(email) } returns user
            every { projectRepository.getProjectById(project.id) } returns project
            every { projectRepository.delete(project) } just runs

            Then("프로젝트 정보를 삭제한다") {
                shouldNotThrowAnyUnit { projectWriteService.deleteProject(project.id, email) }
            }
        }

        When("프로젝트 생성자와 삭제 요청 사용자가 다를 경우") {
            val diffUserProject = createProjectRegisterReq().toEntityWithInfo(
                userId = 100L,
                facilityId = 0L,
                projectCategory = Category.FARMING_HEALING
            )

            every { userRepository.getUserByEmail(email) } returns user
            every { projectRepository.getProjectById(project.id) } returns diffUserProject
            every { projectRepository.delete(project) } just runs

            Then("예외가 발생한다") {
                shouldThrow<PureureumException> { projectWriteService.deleteProject(project.id, email) }
            }
        }

        When("기존에 대상 프로젝트, 사용자에 대한 신청 정보가 존재하지 않는다면") {
            val projectApply = createProjectApply(project.id, user.id)

            every { projectRepository.getProjectById(any()) } returns project
            every { userRepository.getUserByEmail(any()) } returns user
            every { projectApplyRepository.findByProjectIdAndUserId(any(), any()) } returns null
            every { projectApplyRepository.countByProjectId(any()) } returns 5
            every { projectApplyRepository.save(any()) } returns projectApply

            Then("프로젝트 신청 정보를 저장한다") {
                shouldNotThrowAnyUnit { projectWriteService.applyProject(project.id, user.email) }
            }
        }

        When("기존에 대상 프로젝트, 사용자에 대한 신청 정보가 존재한다면") {
            val projectApply = createProjectApply(project.id, user.id)

            every { projectRepository.getProjectById(any()) } returns project
            every { userRepository.getUserByEmail(any()) } returns user
            every { projectApplyRepository.findByProjectIdAndUserId(any(), any()) } returns projectApply

            Then("예외가 발생한다") {
                shouldThrow<PureureumException> { projectWriteService.applyProject(project.id, user.email) }
            }
        }

        When("프로젝트의 총 모집 인원이 가득찼다면") {
            every { projectRepository.getProjectById(any()) } returns project
            every { userRepository.getUserByEmail(any()) } returns user
            every { projectApplyRepository.findByProjectIdAndUserId(any(), any()) } returns null
            every { projectApplyRepository.countByProjectId(any()) } returns 10

            Then("예외가 발생한다") {
                shouldThrow<PureureumException> { projectWriteService.applyProject(project.id, user.email) }
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
