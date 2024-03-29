
package gongback.pureureum.presentation.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import gongback.pureureum.application.FacilityReadService
import gongback.pureureum.application.FacilityWriteService
import gongback.pureureum.application.FileHandlingException
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
import support.createAccessToken
import support.createCertificationDocDto
import support.createFacilityReq
import support.createFacilityRes
import support.createFacilityResWithProgress
import support.createMockCertificationDoc
import support.test.ControllerTestHelper
import support.token
import java.nio.charset.StandardCharsets

@WebMvcTest(FacilityRestController::class)
class FacilityRestControllerTest : ControllerTestHelper() {
    @MockkBean
    private lateinit var facilityReadService: FacilityReadService

    @MockkBean
    private lateinit var facilityWriteService: FacilityWriteService

    @Test
    fun `시설 정보 등록 성공`() {
        val certificationDocDto = createCertificationDocDto()
        every { facilityWriteService.registerFacility(any(), any()) } returns 1L
        every { facilityWriteService.uploadCertificationDocs(any()) } returns listOf(certificationDocDto)
        every { facilityWriteService.saveFacilityFiles(any(), any()) } just runs

        val facilityReq = createFacilityReq()
        val facilityReqStr = jacksonObjectMapper().writeValueAsString(facilityReq)
        val facilityInfo =
            MockMultipartFile(
                "facilityReq",
                "facilityReq",
                "application/json",
                facilityReqStr.toByteArray(StandardCharsets.UTF_8)
            )
        val certificationDocs = createMockCertificationDoc()

        mockMvc.multipart("/api/v1/facilities/register") {
            token(createAccessToken())
            file(facilityInfo)
            file(certificationDocs)
        }.andExpect {
            status { isCreated() }
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
                                "jibun: 시설 주소(지번)\n + " +
                                "detail : 시설 주소(상세 주소) - optional\n + " +
                                "longitude : 시설 주소(경도)\n + " +
                                "latitude : 시설 주소(위도)\n + "
                        )
                        .attributes(
                            Attributes.Attribute(
                                EXAMPLE,
                                "{\"category\": \"청년 농활\"}\n + " +
                                    "{\"name\": \"어느 농가\"}\n + " +
                                    "{\"city\": \"경기도\"}\n + " +
                                    "{\"county\": \"파주시\"}\n + " +
                                    "{\"district\": \"XX동\"}\n + " +
                                    "{\"jibun\": \"100번지 YYY 마을 회관\"})\n + " +
                                    "{\"detail\": \"ZZZ 단지 앞\"}\n + " +
                                    "{\"longitude\": \"100.123\"}\n + " +
                                    "{\"latitude\": \"200.213\"}\n + "
                            )
                        )
                        .attributes(
                            Attributes.Attribute(
                                LENGTH,
                                "city, county, district, longitude, latitude - 길이 제한 (1~20), " +
                                    "jibun - 길이 제한 (1~100)"
                            )
                        ),
                    partWithName("certificationDocs")
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

        mockMvc.multipart("/api/v1/facilities/register") {
            token(createAccessToken())
            file(invalidFacilityInfo)
        }.andExpect {
            status { isBadRequest() }
        }.andDo {
            createDocument(
                "register-facility-invalid-request-fail",
                requestParts(
                    partWithName("facilityReq")
                        .description(
                            "시설 정보 (json)\n" +
                                "category: 형식에 맞지 않은 시설 카테고리\n + " +
                                "name: 형식에 맞지 않은 시설 이름\n + " +
                                "city: 형식에 맞지 않은 시설 주소(시)\n + " +
                                "county: 형식에 맞지 않은 시설 주소(군)\n + " +
                                "district: 형식에 맞지 않은 시설 주소(구)\n + " +
                                "jibun: 형식에 맞지 않은 시설 주소(지번)\n + " +
                                "detail : 형식에 맞지 않은 시설 주소(상세 주소) - optional\n + " +
                                "longitude : 형식에 맞지 않은 시설 주소(경도)\n + " +
                                "latitude : 형식에 맞지 않은 시설 주소(위도)\n + "
                        )
                        .attributes(
                            Attributes.Attribute(
                                EXAMPLE,
                                "{\"category\": \"invalidSample\"}\n + " +
                                    "{\"name\": \"invalidSample\"}\n + " +
                                    "{\"city\": \"invalidSample\"}\n + " +
                                    "{\"county\": \"invalidSample\"}\n + " +
                                    "{\"district\": \"invalidSample\"}\n + " +
                                    "{\"jibun\": \"invalidSample\"}\n + " +
                                    "{\"detail\": \"invalidSample\"})\n + " +
                                    "{\"longitude\": \"invalidSample\"}\n + " +
                                    "{\"latitude\": \"invalidSample\"}\n + "
                            )
                        )
                        .attributes(
                            Attributes.Attribute(
                                LENGTH,
                                "city, county, district, longitude, latitude - 길이 제한 (1~20)" +
                                    "jibun - 길이 제한 (1~100)"
                            )
                        ),
                    partWithName("certificationDocs")
                        .description("인증 서류 (파일 리스트)")
                        .optional()
                )
            )
        }
    }

    @Test
    fun `시설 정보 등록 실패 - 파일 처리 중 오류가 발생했을 경우`() {
        every { facilityWriteService.registerFacility(any(), any()) } returns 1L
        every { facilityWriteService.uploadCertificationDocs(any()) } throws FileHandlingException(null)
        every { facilityWriteService.deleteFacility(any()) } just runs

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
            token(createAccessToken())
            file(facilityInfo)
            file(invalidCertificationDoc)
        }.andExpect {
            status { is5xxServerError() }
        }.andDo {
            createDocument(
                "register-facility-file-handling-fail",
                requestParts(
                    partWithName("facilityReq")
                        .description(
                            "시설 정보 (json)\n" +
                                "category: 시설 카테고리\n + " +
                                "name: 시설 이름\n + " +
                                "city: 시설 주소(시)\n + " +
                                "county: 시설 주소(군)\n + " +
                                "district: 시설 주소(구)\n + " +
                                "jibun: 시설 주소(지번)\n + " +
                                "detail : 시설 주소(상세 주소) - optional\n + " +
                                "longitude : 시설 주소(경도)\n + " +
                                "latitude : 시설 주소(위도)\n + "
                        )
                        .attributes(
                            Attributes.Attribute(
                                EXAMPLE,
                                "{\"category\": \"청년 농활\"}\n + " +
                                    "{\"name\": \"어느 농가\"}\n + " +
                                    "{\"city\": \"경기도\"}\n + " +
                                    "{\"county\": \"파주시\"}\n + " +
                                    "{\"district\": \"XX동\"}\n + " +
                                    "{\"jibun\": \"100번지 YYY 마을 회관\"})\n + " +
                                    "{\"detail\": \"ZZZ 단지 앞\"}\n + " +
                                    "{\"longitude\": \"100.123\"}\n + " +
                                    "{\"latitude\": \"200.213\"}\n + "
                            )
                        )
                        .attributes(
                            Attributes.Attribute(
                                LENGTH,
                                "city, county, district, longitude, latitude - 길이 제한 (1~20), " +
                                    "jibun - 길이 제한 (1~100)"
                            )
                        ),
                    partWithName("certificationDocs")
                        .description("형식에 맞지 않은 인증 서류 (파일 리스트)")
                        .optional()
                )
            )
        }
    }

    @Test
    fun `시설 정보 조회 성공 - 카테고리별 조회`() {
        val facilityRes = listOf(createFacilityRes())
        every { facilityReadService.getApprovedFacilityByCategory(any(), any()) } returns facilityRes

        mockMvc.get("/api/v1/facilities/me") {
            token(createAccessToken())
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
                    fieldWithPath("data[0].jibun").description("시설 주소 (지번)"),
                    fieldWithPath("data[0].detail").description("시설 주소 (상세 주소)").optional(),
                    fieldWithPath("data[0].longitude").description("시설 주소 (경도)"),
                    fieldWithPath("data[0].latitude").description("시설 주소 (위도)")
                )
            )
        }
    }

    @Test
    fun `시설 정보 조회 성공 - 진행 정보 포함 조회`() {
        val facilityResWithProgress = listOf(createFacilityResWithProgress())
        every { facilityReadService.getAllFacilities(any()) } returns facilityResWithProgress

        mockMvc.get("/api/v1/facilities/all") {
            token(createAccessToken())
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
                    fieldWithPath("data[0].jibun").description("시설 주소 (지번)"),
                    fieldWithPath("data[0].detail").description("시설 주소 (상세 주소)"),
                    fieldWithPath("data[0].longitude").description("시설 주소 (경도)"),
                    fieldWithPath("data[0].latitude").description("시설 주소 (위도)"),
                    fieldWithPath("data[0].progress").description("진행 정보")
                )
            )
        }
    }
}
