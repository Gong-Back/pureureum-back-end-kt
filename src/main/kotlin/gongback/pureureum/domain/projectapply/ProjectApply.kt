package gongback.pureureum.domain.projectapply

import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "project_apply",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_project_id_user_id", columnNames = ["project_id", "user_id"])
    ]
)
class ProjectApply(

    @Column(nullable = false, name = "project_id")
    val projectId: Long,

    @Column(nullable = false, name = "user_id")
    val userId: Long
) : BaseEntity() {

    @Column(length = 20, nullable = false)
    @Enumerated(value = EnumType.STRING)
    val applyStatus: ProjectApplyStatus = ProjectApplyStatus.WAITING
}
