package gongback.pureureum.presentation.api

import com.ninjasquad.springmockk.MockkBean
import gongback.pureureum.application.DashboardReadService
import gongback.pureureum.application.PureureumException
import gongback.pureureum.application.dto.ErrorCode
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.get
import support.createAccessToken
import support.createDashboardMembersRes
import support.test.ControllerTestHelper
import support.token

@WebMvcTest(DashboardController::class)
class DashboardRestControllerTest : ControllerTestHelper() {

    @MockkBean
    private lateinit var dashboardReadService: DashboardReadService

    @Test
    fun `대시보드 멤버 조회 성공`() {
        val dashboardMembersRes = createDashboardMembersRes()
        every { dashboardReadService.getDashboardMembers(any(), any()) } returns dashboardMembersRes

        mockMvc.get("/api/v1/dashboards/{dashboardId}", 1) {
            token(createAccessToken())
        }.andExpect {
            status { isOk() }
            content { ApiResponse.ok(dashboardMembersRes) }
        }.andDo {
            createDocument(
                "get-dashboard-members-success",
                requestHeaders(
                    headerWithName(HttpHeaders.AUTHORIZATION).description("Valid-Access-token")
                ),
                responseFields(
                    PayloadDocumentation.fieldWithPath("code").description("응답 코드"),
                    PayloadDocumentation.fieldWithPath("messages").description("응답 메시지"),
                    PayloadDocumentation.fieldWithPath("data[0].id").description("사용자 아이디"),
                    PayloadDocumentation.fieldWithPath("data[0].name").description("사용자 이름"),
                    PayloadDocumentation.fieldWithPath("data[0].role").description("사용자 역할 (MANAGER / MEMBER)"),
                    PayloadDocumentation.fieldWithPath("data[0].profileUrl").description("사용자 프로필 이미지 URL")
                )
            )
        }
    }

    @Test
    fun `대시보드 멤버 조회 실패 - 해당 대시보드에 참여하지 않은 사용자일 경우`() {
        every { dashboardReadService.getDashboardMembers(any(), any()) } throws PureureumException(message = "대시보드에 참여하지 않은 사용자입니다", errorCode = ErrorCode.FORBIDDEN)

        mockMvc.get("/api/v1/dashboards/{dashboardId}", 1) {
            token(createAccessToken())
        }.andExpect {
            status { isForbidden() }
        }.andDo {
            createDocument(
                "get-dashboard-members-fail-forbidden",
                requestHeaders(
                    headerWithName(HttpHeaders.AUTHORIZATION).description("Valid-Access-token")
                )
            )
        }
    }
}
