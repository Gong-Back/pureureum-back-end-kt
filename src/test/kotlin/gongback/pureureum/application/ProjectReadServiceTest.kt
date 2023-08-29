package gongback.pureureum.application

import gongback.pureureum.domain.facility.FacilityRepository
import gongback.pureureum.domain.facility.getFacilityById
import gongback.pureureum.domain.project.ProjectRepository
import gongback.pureureum.domain.project.getProjectById
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.getUserById
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import support.PROJECT_CATEGORY
import support.PROJECT_THUMBNAIL_KEY
import support.SEARCH_TYPE_POPULAR
import support.createDifferentCategoryProject
import support.createFacility
import support.createProject
import support.createProjectResWithoutPayment
import support.createSameCategoryProject
import support.createUser

class ProjectReadServiceTest : BehaviorSpec({
    val fileService = mockk<FileService>()
    val userRepository = mockk<UserRepository>()
    val projectRepository = mockk<ProjectRepository>()
    val facilityRepository = mockk<FacilityRepository>()
    val projectReadService = ProjectReadService(fileService, userRepository, projectRepository, facilityRepository)

    Given("프로젝트 ID") {
        val projectId = 0L

        When("프로젝트 ID에 맞는 프로젝트가 있을 경우") {
            val owner = createUser()
            val project = createProject()
            val facility = createFacility()
            val projectRes = createProjectResWithoutPayment(project, facility.address)

            every { projectRepository.getProjectById(projectId) } returns project
            every { facilityRepository.findFacilityById(project.facilityId) } returns facility
            every { fileService.getFileUrl(any()) } returns "signedUrl"
            every { userRepository.findUserById(any()) } returns owner

            Then("정상적으로 조회된다.") {
                projectReadService.getProject(projectId) shouldBe projectRes
            }
        }
    }

    Given("검색 조건, 카테고리, 페이지 조건") {
        val searchType = SEARCH_TYPE_POPULAR
        val pageable = Pageable.ofSize(10)

        When("모든 프로젝트에 대한 페이지 조회 일 경우") {
            val category = null
            val facility = createFacility()
            val projectOwner = createUser()
            val projects = createDifferentCategoryProject(facility, projectOwner)

            every {
                projectRepository.getRunningProjectsByCategoryOrderedSearchType(
                    searchType,
                    category,
                    pageable
                )
            } returns PageImpl(projects, pageable, projects.size.toLong())

            every { facilityRepository.getFacilityById(any()) } returns facility

            every { userRepository.getUserById(any()) } returns projectOwner

            every { fileService.getFileUrl(any()) } returns PROJECT_THUMBNAIL_KEY

            Then("모든 카테고리의 프로젝트 Page 조회 성공") {
                projectReadService.getRunningProjectPartsByTypeAndCategory(searchType, category, pageable).size shouldBe 3
            }
        }

        When("카테고리가 지정된 경우") {
            val category = PROJECT_CATEGORY
            val facility = createFacility()
            val projectOwner = createUser()
            val projects = createSameCategoryProject(facility, projectOwner)

            every {
                projectRepository.getRunningProjectsByCategoryOrderedSearchType(searchType, category, pageable)
            } returns PageImpl(projects, pageable, projects.size.toLong())

            every { facilityRepository.getFacilityById(any()) } returns facility

            every { userRepository.getUserById(any()) } returns projectOwner

            every { fileService.getFileUrl(any()) } returns PROJECT_THUMBNAIL_KEY

            Then("같은 카테고리의 프로젝트 Page 조회 성공") {
                projectReadService.getRunningProjectPartsByTypeAndCategory(searchType, category, pageable)
                    .projectList[0].projectCategory shouldBe category
            }
        }
    }
})
