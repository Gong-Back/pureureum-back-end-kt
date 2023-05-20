package gongback.pureureum.domain.dashboardqnaboard

import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "dashboard_qna_board_comment")
class DashboardQnABoardComment(
    @Column(nullable = false, name = "user_id")
    val userId: Long,

    @Column(length = 200, nullable = false)
    val content: String,

    @Column(nullable = false)
    val depth: Int,

    @Column(nullable = false)
    val parentCommentId: Long
) : BaseEntity()
