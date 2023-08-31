package gongback.pureureum.presentation.api

import com.ninjasquad.springmockk.MockkBean
import gongback.pureureum.application.FileHandlingException
import gongback.pureureum.application.ProjectReadService
import gongback.pureureum.application.ProjectWriteService
import gongback.pureureum.application.PureureumException
import gongback.pureureum.application.dto.ErrorCode
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockMultipartFile
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.partWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParts
import org.springframework.restdocs.snippet.Attributes
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import support.PROJECT_CATEGORY
import support.SEARCH_TYPE_POPULAR
import support.createAccessToken
import support.createDifferentCategoryProject
import support.createFacility
import support.createMockProjectFile
import support.createProjectFileDto
import support.createProjectPartPageRes
import support.createProjectRegisterReq
import support.createProjectResWithPayment
import support.createProjectResWithoutPayment
import support.createSameCategoryProject
import support.createUser
import support.test.ControllerTestHelper
import support.token
import java.nio.charset.StandardCharsets

@WebMvcTest(ProjectRestController::class)
class ProjectRestControllerTest : ControllerTestHelper() {
    @MockkBean
    private lateinit var projectReadService: ProjectReadService

    @MockkBean
    private lateinit var projectWriteService: ProjectWriteService

    @Test
    fun `프로젝트 등록 성공`() {
        val projectFileDto = createProjectFileDto()

        every { projectWriteService.registerProject(any(), any()) } returns 1L
        every { projectWriteService.uploadProjectFiles(any()) } returns listOf(projectFileDto)
        every { projectWriteService.saveProjectFiles(any(), any()) } just runs

        val projectRegisterReq = createProjectRegisterReq()
        val projectRegisterReqStr = objectToString(projectRegisterReq)
        val projectRegisterInfo =
            MockMultipartFile(
                "projectRegisterReq",
                "projectRegisterReq",
                "application/json",
                projectRegisterReqStr.toByteArray(StandardCharsets.UTF_8)
            )
        val projectFile1 = createMockProjectFile("projectFiles", "test1", "image/png", "sample")
        val projectFile2 = createMockProjectFile("projectFiles", "test1", "image/png", "sample")

        mockMvc.multipart("/api/v1/projects") {
            token(createAccessToken())
            file(projectRegisterInfo)
            file(projectFile1)
            file(projectFile2)
        }.andExpect {
            status { isCreated() }
        }.andDo {
            createDocument(
                "register-project-success",
                requestHeaders(
                    headerWithName(HttpHeaders.AUTHORIZATION).description("Valid-Access-token")
                ),
                requestParts(
                    partWithName("projectRegisterReq")
                        .description(
                            "프로젝트 정보 (json)\n" +
                                "title: 프로젝트 제목\n" +
                                "introduction: 프로젝트 한줄 소개\n" +
                                "content: 프로젝트 내용\n" +
                                "projectStartDate: 프로젝트 시작 시간\n" +
                                "projectEndDate: 프로젝트 종료 시간\n" +
                                "totalRecruits : 프로젝트 최대 모집 인원 (제한 없는 경우 -1)\n" +
                                "minAge : 나이 제한(최소) (Optional)\n" +
                                "maxAge : 나이 제한(최대) (Optional)\n" +
                                "guide : 찾아오시는 길 안내(Optional)\n" +
                                "notice : 유의 사항(Optional)\n" +
                                "paymentType : 지불 유형\n" +
                                "amount : 총 금액(지불 유형이 NONE이 아닐 경우) (Optional)\n" +
                                "refundInstruction : 환불 정책(지불 유형이 NONE이 아닐 경우) (Optional)\n" +
                                "depositInformation : 예금 정보(지불 유형이 NONE이 아닐 경우) (Optional)\n" +
                                "facilityId : 시설 등록 ID\n"
                        )
                        .attributes(
                            Attributes.Attribute(
                                EXAMPLE,
                                "{\"projectStartDate\": \"2023-03-10\"}\n" +
                                    "{\"projectEndDate\": \"2023-03-12\"}\n" +
                                    "{\"paymentType\": \"NONE(참가비 없음), DEPOSIT(보증금), ENTRY_FEE(참가비)\"}\n"
                            )
                        )
                        .attributes(
                            Attributes.Attribute(
                                LENGTH,
                                "title, introduction - 길이 제한 (1~200)\n" +
                                    "content - 길이 제한 (1~65535)"
                            )
                        ),
                    partWithName("projectFiles")
                        .description("프로젝트 파일 - 썸네일 이미지의 경우 가장 처음으로 전송 (0번 인덱스로)")
                        .optional()
                )
            )
        }
    }

    @Test
    fun `프로젝트 등록 실패 - 형식에 맞지 않는 정보`() {
        val projectRegisterReq = createProjectRegisterReq(
            title = "",
            introduction = "",
            content = ""
        )
        val projectRegisterReqStr = objectToString(projectRegisterReq)
        val projectRegisterInfo =
            MockMultipartFile(
                "projectRegisterReq",
                "projectRegisterReq",
                "application/json",
                projectRegisterReqStr.toByteArray(StandardCharsets.UTF_8)
            )

        mockMvc.multipart("/api/v1/projects") {
            token(createAccessToken())
            file(projectRegisterInfo)
        }.andExpect {
            status { isBadRequest() }
        }.andDo {
            createDocument(
                "register-project-invalid-request-fail",
                requestHeaders(
                    headerWithName(HttpHeaders.AUTHORIZATION).description("Valid-Access-token")
                ),
                requestParts(
                    partWithName("projectRegisterReq")
                        .description(
                            "프로젝트 정보 (json)\n" +
                                "title: 프로젝트 제목\n" +
                                "introduction: 프로젝트 한줄 소개\n" +
                                "content: 프로젝트 내용\n" +
                                "projectStartDate: 프로젝트 시작 시간\n" +
                                "projectEndDate: 프로젝트 종료 시간\n" +
                                "totalRecruits : 프로젝트 최대 모집 인원 (제한 없는 경우 -1)\n" +
                                "minAge : 나이 제한(최소) (Optional)\n" +
                                "maxAge : 나이 제한(최대) (Optional)\n" +
                                "guide : 찾아오시는 길 안내(Optional)\n" +
                                "notice : 유의 사항(Optional)\n" +
                                "paymentType : 지불 유형\n" +
                                "amount : 총 금액(지불 유형이 NONE이 아닐 경우) (Optional)\n" +
                                "refundInstruction : 환불 정책(지불 유형이 NONE이 아닐 경우) (Optional)\n" +
                                "depositInformation : 예금 정보(지불 유형이 NONE이 아닐 경우) (Optional)\n" +
                                "facilityId : 시설 등록 ID\n"
                        )
                        .attributes(
                            Attributes.Attribute(
                                EXAMPLE,
                                "{\"projectStartDate\": \"2023-03-10\"}\n" +
                                    "{\"projectEndDate\": \"2023-03-12\"}\n" +
                                    "{\"paymentType\": \"NONE(참가비 없음), DEPOSIT(보증금), ENTRY_FEE(참가비)\"}\n"
                            )
                        )
                        .attributes(
                            Attributes.Attribute(
                                LENGTH,
                                "title, introduction - 길이 제한 (1~200)\n" +
                                    "content - 길이 제한 (1~65535)"
                            )
                        ),
                    partWithName("projectFiles")
                        .description("프로젝트 파일 - 썸네일 이미지의 경우 가장 처음으로 전송 (0번 인덱스로)")
                        .optional()
                )
            )
        }
    }

    @Test
    fun `프로젝트 등록 실패 - 파일 처리 중 오류가 발생했을 경우`() {
        every { projectWriteService.registerProject(any(), any()) } returns 1L
        every { projectWriteService.uploadProjectFiles(any()) } throws FileHandlingException(null)
        every { projectWriteService.deleteProject(any()) } just runs

        val projectRegisterReq = createProjectRegisterReq()
        val projectRegisterReqStr = objectToString(projectRegisterReq)
        val projectRegisterInfo =
            MockMultipartFile(
                "projectRegisterReq",
                "projectRegisterReq",
                "application/json",
                projectRegisterReqStr.toByteArray(StandardCharsets.UTF_8)
            )
        val projectFile1 = createMockProjectFile("projectFiles", "test1", "image/png", "sample")
        val projectFile2 = createMockProjectFile("projectFiles", "test1", "image/png", "sample")

        mockMvc.multipart("/api/v1/projects") {
            token(createAccessToken())
            file(projectRegisterInfo)
            file(projectFile1)
            file(projectFile2)
        }.andExpect {
            status { is5xxServerError() }
        }.andDo {
            createDocument(
                "register-project-file-handling-fail",
                requestHeaders(
                    headerWithName(HttpHeaders.AUTHORIZATION).description("Valid-Access-token")
                ),
                requestParts(
                    partWithName("projectRegisterReq")
                        .description(
                            "프로젝트 정보 (json)\n" +
                                "title: 프로젝트 제목\n" +
                                "introduction: 프로젝트 한줄 소개\n" +
                                "content: 프로젝트 내용\n" +
                                "projectStartDate: 프로젝트 시작 시간\n" +
                                "projectEndDate: 프로젝트 종료 시간\n" +
                                "totalRecruits : 프로젝트 최대 모집 인원 (제한 없는 경우 -1)\n" +
                                "minAge : 나이 제한(최소) (Optional)\n" +
                                "maxAge : 나이 제한(최대) (Optional)\n" +
                                "guide : 찾아오시는 길 안내(Optional)\n" +
                                "notice : 유의 사항(Optional)\n" +
                                "paymentType : 지불 유형\n" +
                                "amount : 총 금액(지불 유형이 NONE이 아닐 경우) (Optional)\n" +
                                "refundInstruction : 환불 정책(지불 유형이 NONE이 아닐 경우) (Optional)\n" +
                                "depositInformation : 예금 정보(지불 유형이 NONE이 아닐 경우) (Optional)\n" +
                                "facilityId : 시설 등록 ID\n"
                        )
                        .attributes(
                            Attributes.Attribute(
                                EXAMPLE,
                                "{\"projectStartDate\": \"2023-03-10\"}\n" +
                                    "{\"projectEndDate\": \"2023-03-12\"}\n" +
                                    "{\"paymentType\": \"NONE(참가비 없음), DEPOSIT(보증금), ENTRY_FEE(참가비)\"}\n"
                            )
                        )
                        .attributes(
                            Attributes.Attribute(
                                LENGTH,
                                "title, introduction - 길이 제한 (1~200)\n" +
                                    "content - 길이 제한 (1~65535)"
                            )
                        ),
                    partWithName("projectFiles")
                        .description("프로젝트 파일 - 썸네일 이미지의 경우 가장 처음으로 전송 (0번 인덱스로)")
                        .optional()
                )
            )
        }
    }

    @Test
    fun `프로젝트 정보 조회 성공 - 단일 조회 (참여 금액 정보가 없을 때)`() {
        val projectRes = createProjectResWithoutPayment()
        every { projectReadService.getProject(any()) } returns projectRes

        mockMvc.perform(get("/api/v1/projects/{id}", 1L))
            .andExpect(status().isOk)
            .andDo(
                createPathDocument(
                    "get-project-detail-without-payment-success",
                    pathParameters(
                        parameterWithName("id").description("프로젝트 ID")
                    ),
                    responseFields(
                        fieldWithPath("code").description("응답 코드"),
                        fieldWithPath("messages").description("응답 메시지"),
                        fieldWithPath("data.projectInformation.title").description("프로젝트 제목"),
                        fieldWithPath("data.projectInformation.introduction").description("프로젝트 한 줄 소개"),
                        fieldWithPath("data.projectInformation.content").description("프로젝트 내용"),
                        fieldWithPath("data.projectInformation.projectStartDate").description("프로젝트 시작 날짜"),
                        fieldWithPath("data.projectInformation.projectEndDate").description("프로젝트 종료 날짜"),
                        fieldWithPath("data.projectInformation.likeCount").description("좋아요 수"),
                        fieldWithPath("data.projectInformation.recruits").description("현재 모집된 인원"),
                        fieldWithPath("data.projectInformation.totalRecruits").description("총 모집 인원"),
                        fieldWithPath("data.projectInformation.minAge").description("나이 제한(최소)"),
                        fieldWithPath("data.projectInformation.maxAge").description("나이 제한(최대)"),
                        fieldWithPath("data.projectInformation.facilityAddress.city").description("시설 주소 (시)"),
                        fieldWithPath("data.projectInformation.facilityAddress.county").description("시설 주소 (군)"),
                        fieldWithPath("data.projectInformation.facilityAddress.district").description("시설 주소 (구)"),
                        fieldWithPath("data.projectInformation.facilityAddress.jibun").description("시설 주소 (지번)"),
                        fieldWithPath("data.projectInformation.facilityAddress.detail").description("시설 주소 (상세 정보)"),
                        fieldWithPath("data.projectInformation.facilityAddress.longitude").description("시설 주소 (경도)"),
                        fieldWithPath("data.projectInformation.facilityAddress.latitude").description("시설 주소 (위도)"),
                        fieldWithPath("data.projectInformation.guide").description("찾아오시는 길 안내(최소)"),
                        fieldWithPath("data.projectInformation.notice").description("유의 사항"),
                        fieldWithPath("data.projectInformation.ownerName").description("소유주 이름"),
                        fieldWithPath("data.projectCategory").description("프로젝트 카테고리"),
                        fieldWithPath("data.projectStatus").description("프로젝트 진행 상황"),
                        fieldWithPath("data.paymentType").description("프로젝트 금액 지불 타입"),
                        fieldWithPath("data.projectPayment").description("프로젝트 금액 정보"),
                        fieldWithPath("data.projectFiles[0].projectFileUrl").description("이미지 접근 주소"),
                        fieldWithPath("data.projectFiles[0].projectFileType").description("프로젝트 이미지 타입 (THUMBNAIL: 썸네일, COMMON: 일반")
                    )
                )
            )
    }

    @Test
    fun `프로젝트 정보 조회 성공 - 단일 조회 (참여 금액 정보가 있을 때)`() {
        val projectRes = createProjectResWithPayment()
        every { projectReadService.getProject(any()) } returns projectRes

        mockMvc.perform(get("/api/v1/projects/{id}", 1L))
            .andExpect(status().isOk)
            .andDo(
                createPathDocument(
                    "get-project-detail-with-payment-success",
                    pathParameters(
                        parameterWithName("id").description("프로젝트 ID")
                    ),
                    responseFields(
                        fieldWithPath("code").description("응답 코드"),
                        fieldWithPath("messages").description("응답 메시지"),
                        fieldWithPath("data.projectInformation.title").description("프로젝트 제목"),
                        fieldWithPath("data.projectInformation.introduction").description("프로젝트 한 줄 소개"),
                        fieldWithPath("data.projectInformation.content").description("프로젝트 내용"),
                        fieldWithPath("data.projectInformation.projectStartDate").description("프로젝트 시작 날짜"),
                        fieldWithPath("data.projectInformation.projectEndDate").description("프로젝트 종료 날짜"),
                        fieldWithPath("data.projectInformation.likeCount").description("좋아요 수"),
                        fieldWithPath("data.projectInformation.recruits").description("현재 모집된 인원"),
                        fieldWithPath("data.projectInformation.totalRecruits").description("총 모집 인원"),
                        fieldWithPath("data.projectInformation.minAge").description("나이 제한(최소)"),
                        fieldWithPath("data.projectInformation.maxAge").description("나이 제한(최대)"),
                        fieldWithPath("data.projectInformation.facilityAddress.city").description("시설 주소 (시)"),
                        fieldWithPath("data.projectInformation.facilityAddress.county").description("시설 주소 (군)"),
                        fieldWithPath("data.projectInformation.facilityAddress.district").description("시설 주소 (구)"),
                        fieldWithPath("data.projectInformation.facilityAddress.jibun").description("시설 주소 (지번)"),
                        fieldWithPath("data.projectInformation.facilityAddress.detail").description("시설 주소 (상세 정보)"),
                        fieldWithPath("data.projectInformation.facilityAddress.longitude").description("시설 주소 (경도)"),
                        fieldWithPath("data.projectInformation.facilityAddress.latitude").description("시설 주소 (위도)"),
                        fieldWithPath("data.projectInformation.guide").description("찾아오시는 길 안내(최소)"),
                        fieldWithPath("data.projectInformation.notice").description("유의 사항"),
                        fieldWithPath("data.projectInformation.ownerName").description("소유주 이름"),
                        fieldWithPath("data.projectCategory").description("프로젝트 카테고리"),
                        fieldWithPath("data.projectStatus").description("프로젝트 진행 상황"),
                        fieldWithPath("data.paymentType").description("프로젝트 금액 지불 타입"),
                        fieldWithPath("data.projectFiles[0].projectFileUrl").description("이미지 접근 주소"),
                        fieldWithPath("data.projectFiles[0].projectFileType").description("프로젝트 이미지 타입 (THUMBNAIL: 썸네일, COMMON: 일반"),
                        fieldWithPath("data.projectPayment.amount").description("총 금액"),
                        fieldWithPath("data.projectPayment.refundInstruction").description("환불 정책"),
                        fieldWithPath("data.projectPayment.depositInformation").description("예금주 정보")
                    )
                )
            )
    }

    @Test
    fun `프로젝트 삭제 성공`() {
        every { projectWriteService.deleteProject(any(), any()) } returns listOf("fileKey1")
        every { projectWriteService.deleteProjectFiles(any()) } just runs

        mockMvc.delete("/api/v1/projects/{id}", 1L) {
            token(createAccessToken())
        }.andExpect {
            status { isNoContent() }
        }.andDo {
            createDocument(
                "delete-project-success",
                requestHeaders(
                    headerWithName(HttpHeaders.AUTHORIZATION).description("Valid-Access-token")
                )
            )
        }
    }

    @Test
    fun `프로젝트 삭제 실패 - 권한이 없을 때`() {
        every { projectWriteService.deleteProject(any(), any()) } throws PureureumException(errorCode = ErrorCode.FORBIDDEN)

        mockMvc.delete("/api/v1/projects/{id}", 1L) {
            token(createAccessToken())
        }.andExpect {
            status { isForbidden() }
        }.andDo {
            createDocument(
                "delete-project-fail-no-match-created-by",
                requestHeaders(
                    headerWithName(HttpHeaders.AUTHORIZATION).description("프로젝트 생성자와 다른 ID를 가진 유저의 AccessToken")
                ),
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("messages").description("응답 메시지"),
                    fieldWithPath("data").description("응답 데이터")
                )
            )
        }
    }

    @Test
    fun `메인 페이지에서 페이지 조건과 검색 조건에 따른 프로젝트 페이지 조회 - 성공`() {
        val facility = createFacility()
        val projectOwner = createUser()
        val projects = createSameCategoryProject(facility, projectOwner)
        val response = createProjectPartPageRes(projects, projectOwner)

        every { projectReadService.getRunningProjectPartsByTypeAndCategory(any(), any(), any()) } returns response

        mockMvc.get("/api/v1/projects") {
            param("searchType", SEARCH_TYPE_POPULAR.name)
            param("category", PROJECT_CATEGORY.name)
            param("page", "0")
            param("size", "10")
        }.andExpect {
            status { isOk() }
            content { ApiResponse.ok(response) }
        }.andDo {
            createDocument(
                "get-page-project-part-success",
                queryParameters(
                    parameterWithName("searchType").description("검색 타입"),
                    parameterWithName("category").description("카테고리").optional(),
                    parameterWithName("page").description("페이지 값").optional(),
                    parameterWithName("size").description("한 페이지에서 받을 데이터 개수").optional()
                ),
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("messages").description("응답 메시지"),
                    fieldWithPath("data.page").description("현재 응답 페이지 (0부터 시작)"),
                    fieldWithPath("data.totalPages").description("총 페이지 (1부터 시작)"),
                    fieldWithPath("data.size").description("응답 데이터 개수"),
                    fieldWithPath("data.projectList[0].projectPartInformation.id").description("프로젝트 ID"),
                    fieldWithPath("data.projectList[0].projectPartInformation.title").description("제목"),
                    fieldWithPath("data.projectList[0].projectPartInformation.likeCount").description("좋아요 개수"),
                    fieldWithPath("data.projectList[0].projectPartInformation.projectStartDate").description("프로젝트 시작 시간"),
                    fieldWithPath("data.projectList[0].projectPartInformation.projectEndDate").description("프로젝트 종료 시간"),
                    fieldWithPath("data.projectList[0].projectPartInformation.recruits").description("현재 모집된 인원"),
                    fieldWithPath("data.projectList[0].projectPartInformation.totalRecruits").description("총 모집 인원"),
                    fieldWithPath("data.projectList[0].projectPartInformation.facilityAddress.city").description("시설 주소 (시)"),
                    fieldWithPath("data.projectList[0].projectPartInformation.facilityAddress.county").description("시설 주소 (군)"),
                    fieldWithPath("data.projectList[0].projectPartInformation.facilityAddress.district").description("시설 주소 (구)"),
                    fieldWithPath("data.projectList[0].projectPartInformation.facilityAddress.jibun").description("시설 주소 (지번)"),
                    fieldWithPath("data.projectList[0].projectPartInformation.facilityAddress.detail").description("시설 주소 (상세 정보)"),
                    fieldWithPath("data.projectList[0].projectPartInformation.facilityAddress.longitude").description("시설 주소 (경도)"),
                    fieldWithPath("data.projectList[0].projectPartInformation.facilityAddress.latitude").description("시설 주소 (위도)"),
                    fieldWithPath("data.projectList[0].projectPartInformation.ownerName").description("소유주 이름"),
                    fieldWithPath("data.projectList[0].projectCategory").description("프로젝트 카테고리"),
                    fieldWithPath("data.projectList[0].thumbnailFileRes.projectFileUrl").description("썸네일 URL"),
                    fieldWithPath("data.projectList[0].thumbnailFileRes.projectFileType").description("파일 타입")
                )
            )
        }
    }

    @Test
    fun `메인 페이지에서 검색 조건으로만 프로젝트 페이지 조회 - 성공`() {
        val facility = createFacility()
        val projectOwner = createUser()
        val projects = createDifferentCategoryProject(facility, projectOwner)
        val response = createProjectPartPageRes(projects, projectOwner)

        every { projectReadService.getRunningProjectPartsByTypeAndCategory(any(), any(), any()) } returns response

        mockMvc.get("/api/v1/projects") {
            param("searchType", SEARCH_TYPE_POPULAR.name)
        }.andExpect {
            status { isOk() }
            content { ApiResponse.ok(response) }
        }.andDo {
            createDocument(
                "get-page-project-part-only-search-type-success",
                queryParameters(
                    parameterWithName("searchType").description("검색 타입"),
                    parameterWithName("category").description("카테고리").optional(),
                    parameterWithName("page").description("페이지 값").optional(),
                    parameterWithName("size").description("한 페이지에서 받을 데이터 개수").optional()
                ),
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("messages").description("응답 메시지"),
                    fieldWithPath("data.page").description("현재 응답 페이지 (0부터 시작)"),
                    fieldWithPath("data.totalPages").description("총 페이지 (1부터 시작)"),
                    fieldWithPath("data.size").description("응답 데이터 개수"),
                    fieldWithPath("data.projectList[0].projectPartInformation.id").description("프로젝트 ID"),
                    fieldWithPath("data.projectList[0].projectPartInformation.title").description("제목"),
                    fieldWithPath("data.projectList[0].projectPartInformation.likeCount").description("좋아요 개수"),
                    fieldWithPath("data.projectList[0].projectPartInformation.projectStartDate").description("프로젝트 시작 시간"),
                    fieldWithPath("data.projectList[0].projectPartInformation.projectEndDate").description("프로젝트 종료 시간"),
                    fieldWithPath("data.projectList[0].projectPartInformation.recruits").description("현재 모집된 인원"),
                    fieldWithPath("data.projectList[0].projectPartInformation.totalRecruits").description("총 모집 인원"),
                    fieldWithPath("data.projectList[0].projectPartInformation.facilityAddress.city").description("시설 주소 (시)"),
                    fieldWithPath("data.projectList[0].projectPartInformation.facilityAddress.county").description("시설 주소 (군)"),
                    fieldWithPath("data.projectList[0].projectPartInformation.facilityAddress.district").description("시설 주소 (구)"),
                    fieldWithPath("data.projectList[0].projectPartInformation.facilityAddress.jibun").description("시설 주소 (지번)"),
                    fieldWithPath("data.projectList[0].projectPartInformation.facilityAddress.detail").description("시설 주소 (상세 정보)"),
                    fieldWithPath("data.projectList[0].projectPartInformation.facilityAddress.longitude").description("시설 주소 (경도)"),
                    fieldWithPath("data.projectList[0].projectPartInformation.facilityAddress.latitude").description("시설 주소 (위도)"),
                    fieldWithPath("data.projectList[0].projectPartInformation.ownerName").description("소유주 이름"),
                    fieldWithPath("data.projectList[0].projectCategory").description("프로젝트 카테고리"),
                    fieldWithPath("data.projectList[0].thumbnailFileRes.projectFileUrl").description("썸네일 URL"),
                    fieldWithPath("data.projectList[0].thumbnailFileRes.projectFileType").description("파일 타입")
                )
            )
        }
    }
}
