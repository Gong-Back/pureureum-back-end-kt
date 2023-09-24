package gongback.pureureum.domain.projectapply

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

fun ProjectApplyRepository.existsByProjectIdAndUserId(projectId: Long, userId: Long): Boolean =
    findByProjectIdAndUserId(projectId, userId) != null

fun ProjectApplyRepository.countByProjectId(projectId: Long): Int =
    findByProjectId(projectId).size

interface ProjectApplyRepository : JpaRepository<ProjectApply, Long> {
    fun findByProjectIdAndUserId(projectId: Long, userId: Long): ProjectApply?

    @Lock(value = LockModeType.PESSIMISTIC_READ)
    @Query("select pa from ProjectApply pa where pa.projectId = :projectId")
    fun findByProjectId(@Param("projectId") projectId: Long): List<ProjectApply>
}
