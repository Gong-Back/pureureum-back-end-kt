package gongback.pureureum.presentation.api

import com.ninjasquad.springmockk.MockkBean
import gongback.pureureum.application.FileService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import support.PROFILE_URL
import support.test.ControllerTestHelper

@WebMvcTest(FileRestController::class)
class FileRestControllerTest : ControllerTestHelper() {
    @MockkBean
    private lateinit var fileService: FileService

    @Test
    fun `이미지 URL 조회 성공`() {
        every { fileService.getFileUrl(any()) } returns PROFILE_URL

        this.mockMvc
            .perform(RestDocumentationRequestBuilders.get("/api/v1/file/url/{fileId}", 1L))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(
                createPathDocument(
                    "get-file-url",
                    pathParameters(
                        RequestDocumentation.parameterWithName("fileId").description("사용자 정보 조회 시 받은 profileId")
                    ),
                    responseFields(
                        fieldWithPath("code").description("응답 코드"),
                        fieldWithPath("messages").description("응답 메시지"),
                        fieldWithPath("data.fileUrl").description("S3 이미지 URL (5분 지속)")
                    )
                )
            )
    }

    @Test
    fun `이미지 URL 조회 실패 - 잘못된 파일 번호`() {
        every { fileService.getFileUrl(any()) } throws IllegalArgumentException("프로필 이미지 정보가 존재하지 않습니다")

        this.mockMvc
            .perform(RestDocumentationRequestBuilders.get("/api/v1/file/url/{fileId}", 1L))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andDo(
                createPathDocument(
                    "get-file-url-fail",
                    pathParameters(
                        RequestDocumentation.parameterWithName("fileId").description("유효하지 않은 profileId")
                    ),
                    responseFields(
                        fieldWithPath("code").description("오류 코드"),
                        fieldWithPath("messages").description("오류 메시지"),
                        fieldWithPath("data").description("오류 데이터")
                    )
                )
            )
    }
}
