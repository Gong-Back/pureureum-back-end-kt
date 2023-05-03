package gongback.pureureum.domain.project

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.query.spec.OrderSpec
import com.linecorp.kotlinjdsl.query.spec.predicate.PredicateSpec
import com.linecorp.kotlinjdsl.querydsl.CriteriaQueryDsl
import com.linecorp.kotlinjdsl.querydsl.expression.column
import com.linecorp.kotlinjdsl.singleQuery
import gongback.pureureum.support.constant.Category
import gongback.pureureum.support.constant.SearchType
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

fun ProjectRepository.getProjectById(id: Long): Project =
    findProjectById(id) ?: throw IllegalArgumentException("요청하신 프로젝트 정보를 찾을 수 없습니다")

interface ProjectRepository : JpaRepository<Project, Long>, CustomProjectRepository {
    fun findProjectById(id: Long): Project?
}

interface CustomProjectRepository {

    fun getRunningProjectsByCategoryOrderedSearchType(
        type: SearchType,
        category: Category?,
        pageable: Pageable
    ): Page<Project>
}

@Repository
internal class ProjectRepositoryImpl(private val queryFactory: QueryFactory) : CustomProjectRepository {
    override fun getRunningProjectsByCategoryOrderedSearchType(
        type: SearchType,
        category: Category?,
        pageable: Pageable
    ): Page<Project> {
        val projects = queryFactory.listQuery {
            select(entity(Project::class))
            from(entity(Project::class))
            where(dynamicAndProjectStatus(ProjectStatus.RUNNING).and(dynamicAndCategory(category)))
            offset(pageable.offset.toInt())
            limit(pageable.pageSize)
            orderBy(dynamicOrderSearchType(type))
        }

        val total = queryFactory.singleQuery {
            select(count(column(Project::id)))
            from(entity(Project::class))
            where(dynamicAndProjectStatus(ProjectStatus.RUNNING).and(dynamicAndCategory(category)))
        }

        return PageImpl(projects, pageable, total)
    }

    private fun <T> CriteriaQueryDsl<T>.dynamicOrderSearchType(type: SearchType): List<OrderSpec> =
        when (type) {
            SearchType.POPULAR -> listOf(column(Project::likeCount).desc(), column(Project::id).desc())
            SearchType.LATEST -> listOf(column(Project::id).desc())
        }

    private fun <T> CriteriaQueryDsl<T>.dynamicAndProjectStatus(projectStatus: ProjectStatus?): PredicateSpec =
        and(projectStatus?.run { column(Project::projectStatus).equal(this) })

    private fun <T> CriteriaQueryDsl<T>.dynamicAndCategory(category: Category?): PredicateSpec =
        and(category?.run { column(Project::projectCategory).equal(this) })
}
