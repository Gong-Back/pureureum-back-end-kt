package gongback.pureureum.domain.project

import gongback.pureureum.support.constant.Category
import gongback.pureureum.support.constant.SearchType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Pageable
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
            shouldThrow<NoSuchElementException> { projectRepository.getProjectById(100L) }
        }
    }

    context("카테고리에 따른 프로젝트 조회") {
        val project1 = createProject()
        val project2 = createProject()
        val project3 = createProject()
        val project4 = createProject(category = Category.ETC)
        projectRepository.saveAll(listOf(project1, project2, project3, project4))

        expect("카테고리가 null일 경우 모든 프로젝트가 조회된다.") {
            val category = null
            val searchType = SearchType.POPULAR
            val pageable = Pageable.ofSize(10)
            val result = projectRepository.getRunningProjectsByCategoryOrderedSearchType(searchType, category, pageable)
            result.content.size shouldBe 4
        }

        expect("카테고리가 null이 아닐 경우 해당 프로젝트가 조회된다.") {
            val category = Category.ETC
            val searchType = SearchType.POPULAR
            val pageable = Pageable.ofSize(10)
            val result = projectRepository.getRunningProjectsByCategoryOrderedSearchType(searchType, category, pageable)
            result.content.size shouldBe 1
        }
    }
})
