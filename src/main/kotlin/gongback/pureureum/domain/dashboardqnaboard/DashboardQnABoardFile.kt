package gongback.pureureum.domain.dashboardqnaboard

import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "dashboard_qna_board_file")
class DashboardQnABoardFile(
    @Column(nullable = false)
    val fileKey: String,

    @Column(nullable = false)
    val contentType: String,

    @Column(nullable = false)
    val originalFileName: String,

    @Column(length = 30, nullable = false)
    val title: String
) : BaseEntity()
