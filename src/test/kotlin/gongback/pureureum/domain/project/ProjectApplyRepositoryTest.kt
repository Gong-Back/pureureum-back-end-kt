package gongback.pureureum.domain.project

import gongback.pureureum.domain.projectapply.ProjectApplyRepository
import gongback.pureureum.domain.projectapply.countByProjectId
import gongback.pureureum.domain.projectapply.existsByProjectIdAndUserId
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
    private val projectApplyRepository: ProjectApplyRepository
) : ExpectSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    context("프로젝트와 사용자 정보에 따른 존재 여부 확인") {
        val user = createUser()
        val project = createProject(userId = user.id)
        val projectApply = createProjectApply(projectId = project.id, userId = user.id)
        projectApplyRepository.save(projectApply)

        expect("이미 프로젝트 신청 정보가 존재하면 true를 반환한다.") {
            val result = projectApplyRepository.existsByProjectIdAndUserId(project.id, user.id)
            result shouldBe true
        }

        expect("프로젝트 신청 정보가 존재하지 않으면 false를 반환한다") {
            val result = projectApplyRepository.existsByProjectIdAndUserId(project.id, 100L)
            result shouldBe false
        }
    }

    context("프로젝트 아이디에 따른 신청 개수 확인") {
        val user = createUser()
        val projectApply1 = createProjectApply(projectId = 1L, userId = user.id)
        val projectApply2 = createProjectApply(projectId = 2L, userId = user.id)
        projectApplyRepository.saveAll(listOf(projectApply1, projectApply2))

        expect("프로젝트 아이디에 해당하는 프로젝트 신청 정보의 개수를 반환한다.") {
            val result = projectApplyRepository.countByProjectId(1L)
            result shouldBe 1
        }
    }
})
