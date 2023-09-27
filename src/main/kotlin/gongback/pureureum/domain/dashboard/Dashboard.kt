package gongback.pureureum.domain.dashboard

import gongback.pureureum.domain.dashboardbulletinboard.DashboardBulletinBoard
import gongback.pureureum.domain.dashboardcalendar.DashboardCalendar
import gongback.pureureum.domain.dashboardgallery.DashboardGallery
import gongback.pureureum.domain.dashboardnotice.DashboardNotice
import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "dashboard")
class Dashboard(

    @Column(nullable = false, name = "project_id")
    val projectId: Long,

    @Column(nullable = false, length = 1)
    val isDeleted: Boolean = false,

    members: List<DashboardMember> = emptyList(),

    bulletinBoards: List<DashboardBulletinBoard> = emptyList(),

    bulletinNotices: List<DashboardNotice> = emptyList(),

    calendars: List<DashboardCalendar> = emptyList(),

    galleries: List<DashboardGallery> = emptyList()
) : BaseEntity() {
    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE], orphanRemoval = true)
    @JoinColumn(
        name = "dashboard_id",
        nullable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "fk_dashboard_member_dashboard_id_ref_dashboard_id")
    )
    private val _members: MutableList<DashboardMember> = members.toMutableList()

    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE], orphanRemoval = true)
    @JoinColumn(
        name = "dashboard_id",
        nullable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "fk_dashboard_bulletin_board_dashboard_id_ref_dashboard_id")
    )
    private val _bulletinBoards: MutableList<DashboardBulletinBoard> = bulletinBoards.toMutableList()

    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE], orphanRemoval = true)
    @JoinColumn(
        name = "dashboard_id",
        nullable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "fk_dashboard_notice_dashboard_id_ref_dashboard_id")
    )
    private val _bulletinNotices: MutableList<DashboardNotice> = bulletinNotices.toMutableList()

    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE], orphanRemoval = true)
    @JoinColumn(
        name = "dashboard_id",
        nullable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "fk_dashboard_calendar_dashboard_id_ref_dashboard_id")
    )
    private val _calendars: MutableList<DashboardCalendar> = calendars.toMutableList()

    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE], orphanRemoval = true)
    @JoinColumn(
        name = "dashboard_id",
        nullable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "fk_dashboard_gallery_dashboard_id_ref_dashboard_id")
    )
    private val _galleries: MutableList<DashboardGallery> = galleries.toMutableList()

    val members: MutableList<DashboardMember>
        get() = _members

    val bulletinBoards: MutableList<DashboardBulletinBoard>
        get() = _bulletinBoards

    val bulletinNotices: MutableList<DashboardNotice>
        get() = _bulletinNotices

    val calendars: MutableList<DashboardCalendar>
        get() = _calendars

    val galleries: MutableList<DashboardGallery>
        get() = _galleries
}
