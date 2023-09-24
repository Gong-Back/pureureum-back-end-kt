package gongback.pureureum.domain.project

import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class ProjectLike(
    val userId: Long,

    @ManyToOne
    @JoinColumn(name = "project_id")
    val project: Project
) : BaseEntity()
