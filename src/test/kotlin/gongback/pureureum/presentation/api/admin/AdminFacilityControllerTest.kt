package gongback.pureureum.presentation.api.admin

import com.ninjasquad.springmockk.MockkBean
import gongback.pureureum.application.FacilityReadService
import gongback.pureureum.application.FacilityWriteService
import gongback.pureureum.domain.facility.FacilityProgress
import gongback.pureureum.presentation.api.ApiResponse
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import support.createFacilityRes
import support.createFacilityWithDocIds
import support.test.ControllerTestHelper

@WebMvcTest(AdminFacilityController::class)
class AdminFacilityControllerTest : ControllerTestHelper() {
    @MockkBean
    private lateinit var facilityReadService: FacilityReadService

    @MockkBean
    private lateinit var facilityWriteService: FacilityWriteService

    @Test
    fun `카테고리별 승인되지 않은 시설 조회 성공`() {
        val facilityRes = listOf(createFacilityRes())
        every { facilityReadService.getNotApprovedFacilitiesByCategory(any()) } returns facilityRes

        mockMvc.get("/admin/facility/all") {
            param("category", "YOUTH_FARMING")
        }.andExpect {
            status { isOk() }
            content { ApiResponse.ok(facilityRes) }
        }.andDo {
            createDocument(
                "get-not-approved-facilities-by-category-success",
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("messages").description("응답 메시지"),
                    fieldWithPath("data[0].id").description("시설 아이디"),
                    fieldWithPath("data[0].category").description("시설 카테고리"),
                    fieldWithPath("data[0].name").description("시설 이름"),
                    fieldWithPath("data[0].city").description("시설 주소 (시)"),
                    fieldWithPath("data[0].county").description("시설 주소 (군)"),
                    fieldWithPath("data[0].district").description("시설 주소 (구)"),
                    fieldWithPath("data[0].jibun").description("시설 주소 (지번)"),
                    fieldWithPath("data[0].detail").description("시설 주소 (상세 주소)"),
                    fieldWithPath("data[0].longitude").description("시설 주소 (상세 경도)"),
                    fieldWithPath("data[0].latitude").description("시설 주소 (상세 위도)")
                )
            )
        }
    }

    @Test
    fun `시설 정보 단건 조회 성공`() {
        val facilityWithDocIds = createFacilityWithDocIds(fileIds = listOf(1L, 2L))
        every { facilityReadService.getFacilityById(any()) } returns facilityWithDocIds

        mockMvc.get("/admin/facility/{id}", 1L) {
        }.andExpect {
            status { isOk() }
            content { ApiResponse.ok(facilityWithDocIds) }
        }.andDo {
            createDocument(
                "get-facility-by-id-success",
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("messages").description("응답 메시지"),
                    fieldWithPath("data.id").description("시설 아이디"),
                    fieldWithPath("data.category").description("시설 카테고리"),
                    fieldWithPath("data.name").description("시설 이름"),
                    fieldWithPath("data.city").description("시설 주소 (시)"),
                    fieldWithPath("data.county").description("시설 주소 (군)"),
                    fieldWithPath("data.district").description("시설 주소 (구)"),
                    fieldWithPath("data.jibun").description("시설 주소 (지번)"),
                    fieldWithPath("data.detail").description("시설 주소 (상세 주소)"),
                    fieldWithPath("data.longitude").description("시설 주소 (상세 경도)"),
                    fieldWithPath("data.latitude").description("시설 주소 (상세 위도)"),
                    fieldWithPath("data.fileIds").description("인증 서류 아이디")
                )
            )
        }
    }

    @Test
    fun `시설 진행 상태 업데이트 - 단건`() {
        every { facilityWriteService.updateFacilityProgress(any(), any()) } just runs

        mockMvc.post("/admin/facility/update/{id}", 1L) {
            param("progress", FacilityProgress.REJECTED.name)
        }.andExpect {
            status { isNoContent() }
        }.andDo {
            createDocument(
                "update-facility-progress-success"
            )
        }
    }

    @Test
    fun `시설 진행 상태 업데이트 - 다건`() {
        every { facilityWriteService.updateFacilitiesProgress(any(), any()) } just runs

        mockMvc.post("/admin/facility/update") {
            param("ids", "1,2,3")
            param("progress", FacilityProgress.APPROVED.name)
        }.andExpect {
            status { isNoContent() }
        }.andDo {
            createDocument(
                "update-facilities-progress-success"
            )
        }
    }
}
