package gongback.pureureum.domain.project

import gongback.pureureum.support.constant.Category
import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.ForeignKey
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(
    name = "project",
    indexes = [
        Index(name = "idx_project_user_Id", columnList = "userId"),
        Index(name = "idx_project_facility_Id", columnList = "facilityId")
    ]
)
class Project(
    @Embedded
    val projectInformation: ProjectInformation,

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val projectStatus: ProjectStatus = ProjectStatus.RUNNING,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val facilityId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val paymentType: ProjectPaymentType = ProjectPaymentType.NONE,

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val projectCategory: Category,

    projectFiles: List<ProjectFile> = emptyList(),

    payments: List<ProjectPayment> = emptyList()
) : BaseEntity() {
    @Column(nullable = false)
    var likeCount: Int = 0
        protected set

    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.MERGE], orphanRemoval = true)
    @JoinColumn(
        name = "project_id",
        nullable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "fk_project_file_project_id_ref_project_id")
    )
    private val _projectFiles: MutableList<ProjectFile> = projectFiles.toMutableList()
    val projectFiles: MutableList<ProjectFile>
        get() = _projectFiles

    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.MERGE], orphanRemoval = true)
    @JoinColumn(
        name = "project_id",
        nullable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "fk_project_payment_project_id_ref_project_id")
    )
    private val _payments: MutableList<ProjectPayment> = payments.toMutableList()
    val payments: ProjectPayment?
        get() = if (paymentType == ProjectPaymentType.NONE) null else _payments[0]

    val title: String
        get() = projectInformation.title

    val introduction: String
        get() = projectInformation.introduction

    val content: String
        get() = projectInformation.content

    val projectStartDate: LocalDate
        get() = projectInformation.projectStartDate

    val projectEndDate: LocalDate
        get() = projectInformation.projectEndDate

    val totalRecruits: Int
        get() = projectInformation.totalRecruits

    val recruits: Int
        get() = projectInformation.recruits

    val minAge: Int
        get() = projectInformation.minAge

    val maxAge: Int
        get() = projectInformation.maxAge

    val guide: String?
        get() = projectInformation.guide

    val notice: String?
        get() = projectInformation.notice

    fun addLikeCount() {
        this.likeCount = this.likeCount + 1
    }
}
