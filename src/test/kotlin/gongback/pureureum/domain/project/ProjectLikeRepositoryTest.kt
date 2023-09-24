package gongback.pureureum.domain.project

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import support.createProject
import support.createProjectLike
import support.createUser
import support.test.BaseTests.RepositoryTest

@RepositoryTest
class ProjectLikeRepositoryTest(
    private val projectRepository: ProjectRepository,
    private val projectLikeRepository: ProjectLikeRepository
) : ExpectSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    context("프로젝트와 사용자 정보에 따른 좋아요 존재 여부") {
        val user = createUser()
        val project = createProject()
        val savedProject = projectRepository.save(project)
        val projectLike = createProjectLike(savedProject, user.id)

        expect("좋아요가 존재한다") {
            projectLikeRepository.save(projectLike)
            val result = projectLikeRepository.existsByProjectAndUserId(savedProject, user.id)
            result shouldBe true
        }

        expect("좋아요가 존재하지 않는다") {
            projectLikeRepository.deleteByProjectAndUserId(savedProject, user.id)
            val result = projectLikeRepository.existsByProjectAndUserId(savedProject, user.id)
            result shouldBe false
        }
    }

    context("프로젝트 아이디와 사용자 아이디에 따른 좋아요 삭제") {
        val user = createUser()
        val project = createProject()
        val savedProject = projectRepository.save(project)
        val projectLike = createProjectLike(savedProject, user.id)
        projectLikeRepository.save(projectLike)

        expect("좋아요가 삭제된다") {
            projectLikeRepository.deleteByProjectAndUserId(savedProject, user.id)
            val result = projectLikeRepository.existsByProjectAndUserId(savedProject, user.id)
            result shouldBe false
        }
    }
})
