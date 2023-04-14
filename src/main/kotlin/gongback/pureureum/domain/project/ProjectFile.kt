package gongback.pureureum.domain.project

import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Entity
class ProjectFile(
    @Column(nullable = false)
    val fileKey: String,

    @Column(nullable = false)
    val contentType: String,

    @Column(nullable = false)
    val originalFileName: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val projectFileType: ProjectFileType = ProjectFileType.COMMON
) : BaseEntity()
