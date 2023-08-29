package gongback.pureureum.domain.projectapply

import gongback.pureureum.domain.project.Project
import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
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

    @Column(nullable = false, name = "user_id")
    val userId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    val project: Project
) : BaseEntity() {

    @Column(length = 20, nullable = false)
    @Enumerated(value = EnumType.STRING)
    val applyStatus: ProjectApplyStatus = ProjectApplyStatus.WAITING
}
