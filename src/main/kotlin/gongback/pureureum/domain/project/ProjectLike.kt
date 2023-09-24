package gongback.pureureum.domain.project

import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class ProjectLike(
    val userId: Long,

    @ManyToOne(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE])
    @JoinColumn(name = "project_id")
    val project: Project
) : BaseEntity()
