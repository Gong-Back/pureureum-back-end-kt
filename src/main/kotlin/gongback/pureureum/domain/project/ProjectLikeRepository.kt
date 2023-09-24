package gongback.pureureum.domain.project

import org.springframework.data.jpa.repository.JpaRepository

fun ProjectLikeRepository.existsByProjectAndUserId(project: Project, userId: Long): Boolean =
    findByProjectAndUserId(project, userId) != null

interface ProjectLikeRepository : JpaRepository<ProjectLike, Long> {
    fun findByProjectAndUserId(project: Project, userId: Long): ProjectLike?
    fun deleteByProjectAndUserId(project: Project, id: Long)
}
