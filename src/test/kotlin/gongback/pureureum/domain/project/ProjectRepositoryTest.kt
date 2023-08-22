package gongback.pureureum.domain.project

import gongback.pureureum.support.constant.Category
import gongback.pureureum.support.constant.SearchType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import java.util.stream.IntStream
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

        val savedProject1 = projectRepository.save(project1)
        val savedProject2 = projectRepository.save(project2)
        val savedProject3 = projectRepository.save(project3)
        val savedProject4 = projectRepository.save(project4)

        expect("카테고리가 null일 경우 모든 프로젝트가 조회된다.") {
            val category = null
            val searchType = SearchType.LATEST
            val pageable = Pageable.ofSize(10)
            val result = projectRepository.getRunningProjectsByCategoryOrderedSearchType(searchType, category, pageable)
            result.content.size shouldBe 4
            result.content.map {
                it.id
            } shouldBe listOf(savedProject4.id, savedProject3.id, savedProject2.id, savedProject1.id)
        }

        expect("카테고리가 null이 아닐 경우 해당 프로젝트가 조회된다.") {
            val category = Category.ETC
            val searchType = SearchType.LATEST
            val pageable = Pageable.ofSize(10)
            val result = projectRepository.getRunningProjectsByCategoryOrderedSearchType(searchType, category, pageable)
            result.content.size shouldBe 1
            result.content.map {
                it.id
            } shouldBe listOf(savedProject4.id)
        }
    }

    context("검색 조건에 따른 프로젝트 조회") {
        val project1 = createProject(
            projectEndDate = "2023-03-18"
        ).apply { IntStream.rangeClosed(0, 5).forEach { _ -> this.addLikeCount() } }

        val project2 = createProject(
            projectEndDate = "2023-03-12"
        )

        val project3 = createProject(
            projectEndDate = "2023-03-15"
        ).apply { IntStream.rangeClosed(0, 10).forEach { _ -> this.addLikeCount() } }

        val savedProject1 = projectRepository.save(project1)
        val savedProject2 = projectRepository.save(project2)
        val savedProject3 = projectRepository.save(project3)

        expect("검색 조건이 LATEST라면 최신순으로 조회된다") {
            val category = null
            val searchType = SearchType.LATEST
            val pageable = Pageable.ofSize(10)
            val result = projectRepository.getRunningProjectsByCategoryOrderedSearchType(searchType, category, pageable)
            result.content.size shouldBe 3
            result.content.map {
                it.id
            } shouldBe listOf(savedProject3.id, savedProject2.id, savedProject1.id)
        }

        expect("검색 조건이 POPULAR라면 인기순으로 조회된다") {
            val category = null
            val searchType = SearchType.POPULAR
            val pageable = Pageable.ofSize(10)
            val result = projectRepository.getRunningProjectsByCategoryOrderedSearchType(searchType, category, pageable)
            result.content.size shouldBe 3
            result.content.map {
                it.id
            } shouldBe listOf(savedProject3.id, savedProject1.id, savedProject2.id)
        }

        expect("검색 조건이 DEADLINE라면 마감임박순으로 조회된다") {
            val category = null
            val searchType = SearchType.DEADLINE
            val pageable = Pageable.ofSize(10)
            val result = projectRepository.getRunningProjectsByCategoryOrderedSearchType(searchType, category, pageable)
            result.content.size shouldBe 3
            result.content.map {
                it.id
            } shouldBe listOf(savedProject2.id, savedProject3.id, savedProject1.id)
        }
    }
})
