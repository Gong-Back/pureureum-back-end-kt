package gongback.pureureum.domain.project

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.time.LocalDate

/**
 * title: 제목
 * introduction: 한 줄 소개
 * content: 프로젝트 내용
 * projectStartDate: 프로젝트 시작 시간
 * projectEndDate: 프로젝트 종료 시간
 * likeCount: 좋아요 수
 * totalRecruits: 총 모집 인원
 * recruits: 현재 모집 인원
 * minAge: 나이 제한(최소)
 * maxAge: 나이 제한(최대)
 * projectAddress: 주소
 * guide: 찾아오시는 길 안내
 * notice: 유의 사항
 */
@Embeddable
data class ProjectInformation(
    @Column(nullable = false, length = 20)
    val title: String,

    @Column(nullable = false, length = 200)
    val introduction: String,

    @Column(nullable = false, length = 500)
    val content: String,

    @Column(nullable = false)
    val projectStartDate: LocalDate,

    @Column(nullable = false)
    val projectEndDate: LocalDate,

    @Column(nullable = false)
    val likeCount: Int = 0,

    @Column(nullable = false)
    val totalRecruits: Int,

    @Column(nullable = false)
    val recruits: Int = 0,

    @Column(nullable = false)
    val minAge: Int = -1,

    @Column(nullable = false)
    val maxAge: Int = -1,

    @Column(nullable = true, length = 500)
    val guide: String? = null,

    @Column(nullable = true, length = 500)
    val notice: String? = null
)
