package gongback.pureureum.presentation.api

import gongback.pureureum.application.ProjectService
import gongback.pureureum.application.dto.ProjectPartPageRes
import gongback.pureureum.application.dto.ProjectRegisterReq
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

@RestController
@RequestMapping("/api/v1/projects")
class ProjectRestController(
    private val projectService: ProjectService
) {

    @PostMapping
    fun registerProject(
        @RequestPart @Valid projectRegisterReq: ProjectRegisterReq,
        @RequestPart(required = false) projectFiles: List<MultipartFile>?,
        @LoginEmail email: String
    ): ResponseEntity<ApiResponse<Unit>> {
        projectService.registerProject(email, projectRegisterReq, projectFiles)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{id}")
    fun getProjectDetail(
        @PathVariable("id") id: Long
    ): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok().body(ApiResponse.ok(projectService.getProject(id)))

    @DeleteMapping("/{id}")
    fun deleteProject(
        @PathVariable("id") id: Long,
        @LoginEmail email: String
    ): ResponseEntity<Unit> {
        projectService.deleteProject(id, email)
        return ResponseEntity.ok().build()
    }

    /**
     * 메인 페이지에서 인기 순으로 프로젝트 페이지 조회
     */
    @GetMapping("/parts")
    fun getPopularProjectPage(
        @RequestParam(value = "searchType") searchType: SearchType,
        @RequestParam(value = "category", required = false) category: Category?,
        @PageableDefault(page = 0, size = 10) pageable: Pageable
    ): ResponseEntity<ApiResponse<ProjectPartPageRes>> =
        ResponseEntity.ok(
            ApiResponse.ok(
                projectService.getRunningProjectPartsByTypeAndCategory(
                    searchType,
                    category,
                    pageable
                )
            )
        )
}
