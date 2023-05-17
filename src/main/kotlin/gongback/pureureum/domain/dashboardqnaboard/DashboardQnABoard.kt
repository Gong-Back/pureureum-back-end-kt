package gongback.pureureum.domain.dashboardqnaboard

import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(
    name = "dashboard_qna_board",
    indexes = [
        Index(name = "idx_dashboard_qna_board_user_id", columnList = "user_id"),
        Index(name = "idx_dashboard_qna_board_title", columnList = "title")
    ]
)
class DashboardQnABoard(
    @Column(nullable = false, name = "user_id")
    val userId: Long,

    @Column(length = 50, nullable = false)
    val title: String,

    @Lob
    @Column(nullable = false)
    val content: String,

    qnABoardComments: List<DashboardQnABoardComment> = emptyList(),

    qnABoardFiles: List<DashboardQnABoardFile> = emptyList()

) : BaseEntity() {
    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE], orphanRemoval = true)
    @JoinColumn(
        name = "dashboard_qna_board_id",
        nullable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "fk_dashboard_qna_board_comment_ref_dashboard_qna_board_id")
    )
    private val _qnaBoardComments: MutableList<DashboardQnABoardComment> = qnABoardComments.toMutableList()

    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE], orphanRemoval = true)
    @JoinColumn(
        name = "dashboard_qna_board_id",
        nullable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "fk_dashboard_qna_board_file_ref_dashboard_qna_board_id")
    )
    private val _qnaBoardFiles: MutableList<DashboardQnABoardFile> = qnABoardFiles.toMutableList()

    val bulletinBoardComments: MutableList<DashboardQnABoardComment>
        get() = _qnaBoardComments

    val bulletinBoardFiles: MutableList<DashboardQnABoardFile>
        get() = _qnaBoardFiles
}
