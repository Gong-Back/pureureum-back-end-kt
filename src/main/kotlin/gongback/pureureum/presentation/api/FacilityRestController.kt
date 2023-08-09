package gongback.pureureum.presentation.api

import gongback.pureureum.application.FacilityService
import gongback.pureureum.application.dto.FacilityReq
import gongback.pureureum.application.dto.FacilityRes
import gongback.pureureum.application.dto.FacilityResWithProgress
import gongback.pureureum.security.LoginEmail
import gongback.pureureum.support.constant.Category
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/facilities")
class FacilityRestController(
    private val facilityService: FacilityService
) {
    @PostMapping("/register")
    fun registerFacility(
        @RequestPart @Valid facilityReq: FacilityReq,
        @RequestPart(required = false) certificationDoc: List<MultipartFile>?,
        @LoginEmail userEmail: String
    ): ResponseEntity<Unit> {
        facilityService.registerFacility(userEmail, facilityReq, certificationDoc)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @GetMapping("/me")
    fun getApprovedFacilitiesByCategory(
        @RequestParam("category") category: Category,
        @LoginEmail userEmail: String
    ): ResponseEntity<ApiResponse<List<FacilityRes>>> {
        val facilityRes = facilityService.getApprovedFacilityByCategory(userEmail, category)
        return ResponseEntity.ok().body(ApiResponse.ok(facilityRes))
    }

    @GetMapping("/all")
    fun getAllFacilities(
        @LoginEmail userEmail: String
    ): ResponseEntity<ApiResponse<List<FacilityResWithProgress>>> {
        val facilityRes = facilityService.getAllFacilities(userEmail)
        return ResponseEntity.ok().body(ApiResponse.ok(facilityRes))
    }
}
