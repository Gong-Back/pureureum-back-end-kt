package gongback.pureureum.presentation.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import gongback.pureureum.application.FacilityService
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.partWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParts
import org.springframework.restdocs.snippet.Attributes
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import support.FACILITY_CATEGORY
import support.accessToken
import support.createAccessToken
import support.createFacilityReq
import support.createFacilityRes
import support.createFacilityResWithProgress
import support.createMockCertificationDoc
import support.test.ControllerTestHelper
import java.nio.charset.StandardCharsets

@WebMvcTest(FacilityRestController::class)
class FacilityRestControllerTest : ControllerTestHelper() {
    @MockkBean
    private lateinit var facilityService: FacilityService

    @Test
    fun `시설 정보 등록 성공`() {
        every { facilityService.registerFacility(any(), any(), any()) } just runs

        val facilityReq = createFacilityReq()
        val facilityReqStr = jacksonObjectMapper().writeValueAsString(facilityReq)
        val facilityInfo =
            MockMultipartFile(
                "facilityReq",
                "facilityReq",
                "application/json",
                facilityReqStr.toByteArray(StandardCharsets.UTF_8)
            )
        val certificationDoc = createMockCertificationDoc()

        mockMvc.multipart("/api/v1/facilities/register") {
            accessToken(createAccessToken())
            file(facilityInfo)
            file(certificationDoc)
        }.andExpect {
            status { isOk() }
        }.andDo {
            createDocument(
                "register-facility-success",
                requestParts(
                    partWithName("facilityReq")
                        .description(
                            "시설 정보 (json)\n" +
                                "category: 시설 카테고리\n + " +
                                "name: 시설 이름\n + " +
                                "city: 시설 주소(시)\n + " +
                                "county: 시설 주소(군)\n + " +
                                "district: 시설 주소(구)\n + " +
                                "detail : 시설 주소(상세 주소)\n + "
                        )
                        .attributes(
                            Attributes.Attribute(
                                EXAMPLE,
                                "{\"category\": \"sample\"}\n + " +
                                    "{\"name\": \"sample\"}\n + " +
                                    "{\"city\": \"sample\"}\n + " +
                                    "{\"county\": \"sample\"}\n + " +
                                    "{\"district\": \"sample\"}\n + " +
                                    "{\"detail\": \"sample\"})\n + "
                            )
                        )
                        .attributes(
                            Attributes.Attribute(
                                LENGTH,
                                "city, county, district, detail - 길이 제한 (1~20)"
                            )
                        ),
                    partWithName("certificationDoc")
                        .description("인증 서류 (파일 리스트)")
                        .optional()
                )
            )
        }
    }

    @Test
    fun `시설 정보 등록 실패 - 형식에 맞지 않은 정보`() {
        val invalidFacilityReq = createFacilityReq(
            category = FACILITY_CATEGORY,
            name = "",
            city = "invalidCityInvalidCity",
            county = "invalidCountyInvalidCounty",
            district = "invalidDistrictInvalidDistrict",
            detail = "invalidDetailInvalidDetail"
        )
        val invalidFacilityReqStr = jacksonObjectMapper().writeValueAsString(invalidFacilityReq)
        val invalidFacilityInfo =
            MockMultipartFile(
                "facilityReq",
                "facilityReq",
                "application/json",
                invalidFacilityReqStr.toByteArray(StandardCharsets.UTF_8)
            )

        val certificationDoc = createMockCertificationDoc()

        mockMvc.multipart("/api/v1/facilities/register") {
            accessToken(createAccessToken())
            file(invalidFacilityInfo)
            file(certificationDoc)
        }.andExpect {
            status { isBadRequest() }
        }.andDo {
            createDocument(
                "register-facility-fail",
                requestParts(
                    partWithName("facilityReq")
                        .description(
                            "시설 정보 (json)\n" +
                                "category: 형식에 맞지 않은 시설 카테고리\n + " +
                                "name: 형식에 맞지 않은 시설 이름\n + " +
                                "city: 형식에 맞지 않은 시설 주소(시)\n + " +
                                "county: 형식에 맞지 않은 시설 주소(군)\n + " +
                                "district: 형식에 맞지 않은 시설 주소(구)\n + " +
                                "detail : 형식에 맞지 않은 시설 주소(상세 주소)\n + "
                        )
                        .attributes(
                            Attributes.Attribute(
                                EXAMPLE,
                                "{\"category\": \"invalidSample\"}\n + " +
                                    "{\"name\": \"invalidSample\"}\n + " +
                                    "{\"city\": \"invalidSample\"}\n + " +
                                    "{\"county\": \"invalidSample\"}\n + " +
                                    "{\"district\": \"invalidSample\"}\n + " +
                                    "{\"detail\": \"invalidSample\"})\n + "
                            )
                        )
                        .attributes(
                            Attributes.Attribute(
                                LENGTH,
                                "city, county, district, detail - 길이 제한 (1~20)"
                            )
                        ),
                    partWithName("certificationDoc")
                        .description("인증 서류 (파일 리스트)")
                        .optional()
                )
            )
        }
    }

    @Test
    fun `시설 정보 등록 실패 - 원본 파일 이름이 비어있을 경우`() {
        every { facilityService.registerFacility(any(), any(), any()) } throws IllegalArgumentException("원본 파일 이름이 비어있습니다")

        val facilityReq = createFacilityReq()
        val facilityReqStr = jacksonObjectMapper().writeValueAsString(facilityReq)
        val facilityInfo =
            MockMultipartFile(
                "facilityReq",
                "facilityReq",
                "application/json",
                facilityReqStr.toByteArray(StandardCharsets.UTF_8)
            )

        val invalidCertificationDoc = createMockCertificationDoc(originalFileName = "")

        mockMvc.multipart("/api/v1/facilities/register") {
            accessToken(createAccessToken())
            file(facilityInfo)
            file(invalidCertificationDoc)
        }.andExpect {
            status { isBadRequest() }
        }.andDo {
            createDocument(
                "register-facility-original-file-name-empty-fail",
                requestParts(
                    partWithName("facilityReq")
                        .description(
                            "시설 정보 (json)\n" +
                                "category: 시설 카테고리\n + " +
                                "name: 시설 이름\n + " +
                                "city: 시설 주소(시)\n + " +
                                "county: 시설 주소(군)\n + " +
                                "district: 시설 주소(구)\n + " +
                                "detail : 시설 주소(상세 주소)\n + "
                        )
                        .attributes(
                            Attributes.Attribute(
                                EXAMPLE,
                                "{\"category\": \"sample\"}\n + " +
                                    "{\"name\": \"sample\"}\n + " +
                                    "{\"city\": \"sample\"}\n + " +
                                    "{\"county\": \"sample\"}\n + " +
                                    "{\"district\": \"sample\"}\n + " +
                                    "{\"detail\": \"sample\"})\n + "
                            )
                        )
                        .attributes(
                            Attributes.Attribute(
                                LENGTH,
                                "city, county, district, detail - 길이 제한 (1~20)"
                            )
                        ),
                    partWithName("certificationDoc")
                        .description("형식에 맞지 않은 인증 서류 (파일 리스트)")
                        .optional()
                )
            )
        }
    }

    @Test
    fun `시설 정보 조회 성공 - 카테고리별 조회`() {
        val facilityRes = listOf(createFacilityRes())
        every { facilityService.getFacilityByCategory(any(), any()) } returns facilityRes

        mockMvc.get("/api/v1/facilities/me") {
            accessToken(createAccessToken())
            param("category", "FARMING_HEALING")
        }.andExpect {
            status { isOk() }
            content { ApiResponse.ok(facilityRes) }
        }.andDo {
            createDocument(
                "get-facility-success-by-category",
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("messages").description("응답 메시지"),
                    fieldWithPath("data[0].id").description("시설 아이디"),
                    fieldWithPath("data[0].category").description("시설 카테고리"),
                    fieldWithPath("data[0].name").description("시설 이름"),
                    fieldWithPath("data[0].city").description("시설 주소 (시)"),
                    fieldWithPath("data[0].county").description("시설 주소 (군)"),
                    fieldWithPath("data[0].district").description("시설 주소 (구)"),
                    fieldWithPath("data[0].detail").description("시설 주소 (상세 주소)")
                )
            )
        }
    }

    @Test
    fun `시설 정보 조회 성공 - 진행 정보 포함 조회`() {
        val facilityResWithProgress = listOf(createFacilityResWithProgress())
        every { facilityService.getFacilities(any()) } returns facilityResWithProgress

        mockMvc.get("/api/v1/facilities/all") {
            accessToken(createAccessToken())
        }.andExpect {
            status { isOk() }
            content { ApiResponse.ok(facilityResWithProgress) }
        }.andDo {
            createDocument(
                "get-facility-success-with-progress",
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("messages").description("응답 메시지"),
                    fieldWithPath("data[0].id").description("시설 아이디"),
                    fieldWithPath("data[0].category").description("시설 카테고리"),
                    fieldWithPath("data[0].name").description("시설 이름"),
                    fieldWithPath("data[0].city").description("시설 주소 (시)"),
                    fieldWithPath("data[0].county").description("시설 주소 (군)"),
                    fieldWithPath("data[0].district").description("시설 주소 (구)"),
                    fieldWithPath("data[0].detail").description("시설 주소 (상세 주소)"),
                    fieldWithPath("data[0].progress").description("진행 정보")
                )
            )
        }
    }
}
