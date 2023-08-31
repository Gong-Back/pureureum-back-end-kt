package gongback.pureureum.presentation.api

import gongback.pureureum.application.FileHandlingException
import gongback.pureureum.application.ProjectReadService
import gongback.pureureum.application.ProjectWriteService
import gongback.pureureum.application.dto.ProjectPartPageRes
import gongback.pureureum.application.dto.ProjectRegisterReq
import gongback.pureureum.application.dto.ProjectRes
import gongback.pureureum.security.LoginEmail
import gongback.pureureum.support.constant.Category
import gongback.pureureum.support.constant.SearchType
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.net.URI

private const val BASE_URL = "/api/v1/projects"

@RestController
@RequestMapping(BASE_URL)
class ProjectRestController(
    private val projectReadService: ProjectReadService,
    private val projectWriteService: ProjectWriteService
) {

    @PostMapping
    fun registerProject(
        @RequestPart @Valid projectRegisterReq: ProjectRegisterReq,
        @RequestPart(required = false) projectFiles: List<MultipartFile>?,
        @LoginEmail email: String
    ): ResponseEntity<ApiResponse<Unit>> {
        val savedProjectId = projectWriteService.registerProject(email, projectRegisterReq)
        return try {
            projectFiles?.let {
                val projectFileDtos = projectWriteService.uploadProjectFiles(it)
                projectWriteService.saveProjectFiles(savedProjectId, projectFileDtos)
            }
            return ResponseEntity.created(URI.create("$BASE_URL/$savedProjectId")).build()
        } catch (e: FileHandlingException) {
            projectWriteService.deleteProject(savedProjectId)
            ResponseEntity.status(e.errorCode.httpStatus)
                .body(ApiResponse.error(e.errorCode.code, e.message ?: e.errorCode.message))
        }
    }

    @GetMapping("/{id}")
    fun getProjectDetail(
        @PathVariable("id") id: Long
    ): ResponseEntity<ApiResponse<ProjectRes>> = ResponseEntity.ok().body(ApiResponse.ok(projectReadService.getProject(id)))

    @DeleteMapping("/{id}")
    fun deleteProject(
        @PathVariable("id") id: Long,
        @LoginEmail email: String
    ): ResponseEntity<Unit> {
        val targetFileKeys = projectWriteService.deleteProject(id, email)
        projectWriteService.deleteProjectFiles(targetFileKeys)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun getPopularProjectPage(
        @RequestParam(value = "searchType") searchType: SearchType,
        @RequestParam(value = "category", required = false) category: Category?,
        @PageableDefault(page = 0, size = 10) pageable: Pageable
    ): ResponseEntity<ApiResponse<ProjectPartPageRes>> =
        ResponseEntity.ok(
            ApiResponse.ok(
                projectReadService.getRunningProjectPartsByTypeAndCategory(
                    searchType,
                    category,
                    pageable
                )
            )
        )

    @PostMapping("/apply")
    fun projectApply() {
        // TODO: 1:다로 프로젝트 신청 정보 관리해야 할 듯...?
    }
}
