package gongback.pureureum.presentation.api

import gongback.pureureum.application.DashboardReadService
import gongback.pureureum.application.dto.DashboardMemberRes
import gongback.pureureum.security.LoginEmail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private const val BASE_URL = "/api/v1/dashboards"

@RestController
@RequestMapping(BASE_URL)
class DashboardController(
    private val dashboardReadService: DashboardReadService
) {
    @GetMapping("/{id}")
    fun getDashboardMembers(
        @PathVariable id: Long,
        @LoginEmail email: String
    ): ResponseEntity<ApiResponse<List<DashboardMemberRes>>> {
        val result = dashboardReadService.getDashboardMembers(id, email)
        return ResponseEntity.ok().body(ApiResponse.ok(result))
    }
}
