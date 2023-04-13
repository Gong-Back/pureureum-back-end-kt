package gongback.pureureum.domain.project

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import support.createProject
import support.test.BaseTests.RepositoryTest

@RepositoryTest
class ProjectRepositoryTest(
    private val projectRepository: ProjectRepository
) : ExpectSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    context("프로젝트 ID에 따른 조회") {
        val project = createProject()
        projectRepository.save(project)

        expect("프로젝트가 존재한다") {
            val result = projectRepository.getProjectById(project.id)
            result shouldBe project
        }

        expect("프로젝트가 존재하지 않는다") {
            shouldThrow<IllegalArgumentException> { projectRepository.getProjectById(100L) }
        }
    }
})
