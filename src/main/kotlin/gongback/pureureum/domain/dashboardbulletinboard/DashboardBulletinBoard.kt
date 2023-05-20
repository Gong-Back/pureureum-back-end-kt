package gongback.pureureum.domain.dashboardbulletinboard

import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "dashboard_bulletin_board")
class DashboardBulletinBoard(
    @Column(nullable = false, name = "user_id")
    val userId: Long,

    @Column(length = 50, nullable = false)
    val title: String,

    @Lob
    @Column(nullable = false)
    val content: String,

    bulletinBoardComments: List<DashboardBulletinBoardComment> = emptyList(),

    bulletinBoardFiles: List<DashboardBulletinBoardFile> = emptyList()

) : BaseEntity() {
    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE], orphanRemoval = true)
    @JoinColumn(
        name = "dashboard_bulletin_board_id",
        nullable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "fk_dashboard_bulletin_board_comment_ref_bulletin_board_id")
    )
    private val _bulletinBoardComments: MutableList<DashboardBulletinBoardComment> = bulletinBoardComments.toMutableList()

    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE], orphanRemoval = true)
    @JoinColumn(
        name = "dashboard_bulletin_board_id",
        nullable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "fk_dashboard_bulletin_board_file_ref_bulletin_board_id")
    )
    private val _bulletinBoardFiles: MutableList<DashboardBulletinBoardFile> = bulletinBoardFiles.toMutableList()

    val bulletinBoardComments: MutableList<DashboardBulletinBoardComment>
        get() = _bulletinBoardComments

    val bulletinBoardFiles: MutableList<DashboardBulletinBoardFile>
        get() = _bulletinBoardFiles
}
