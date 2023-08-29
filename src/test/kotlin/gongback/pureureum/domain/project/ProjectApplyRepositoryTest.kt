package gongback.pureureum.domain.project

import gongback.pureureum.domain.projectapply.ProjectApplyRepository
import gongback.pureureum.domain.projectapply.existsByProjectAndUserId
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import support.createProject
import support.createProjectApply
import support.createUser
import support.test.BaseTests.RepositoryTest

@RepositoryTest
class ProjectApplyRepositoryTest(
    private val projectApplyRepository: ProjectApplyRepository,
    private val projectRepository: ProjectRepository
) : ExpectSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    context("프로젝트와 사용자 정보에 따른 존재 여부 확인") {
        val user = createUser()
        val project = createProject(userId = user.id)
        val savedProject = projectRepository.save(project)
        val projectApply = createProjectApply(project = savedProject)
        projectApplyRepository.save(projectApply)

        expect("이미 프로젝트 신청 정보가 존재하면 true를 반환한다.") {
            val result = projectApplyRepository.existsByProjectAndUserId(project, user.id)
            result shouldBe true
        }

        expect("프로젝트 신청 정보가 존재하지 않으면 false를 반환한다") {
            val result = projectApplyRepository.existsByProjectAndUserId(project, 100L)
            result shouldBe false
        }
    }
})
