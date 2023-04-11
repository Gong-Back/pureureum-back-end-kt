package gongback.pureureum.presentation.api

import gongback.pureureum.application.FacilityService
import gongback.pureureum.application.dto.FacilityReq
import gongback.pureureum.application.dto.FacilityRes
import gongback.pureureum.application.dto.FacilityResWithProgress
import gongback.pureureum.security.LoginEmail
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

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
        return ResponseEntity.ok().build()
    }

    @GetMapping("/me")
    fun getFacilitiesByCategory(
        @RequestParam("category") category: String,
        @LoginEmail userEmail: String
    ): ResponseEntity<ApiResponse<List<FacilityRes>>> {
        val facilityInfo = facilityService.getFacilityByCategory(userEmail, category)
        return ResponseEntity.ok().body(ApiResponse.ok(facilityInfo))
    }

    @GetMapping("/all")
    fun getFacilities(
        @LoginEmail userEmail: String
    ): ResponseEntity<ApiResponse<List<FacilityResWithProgress>>> {
        val facilityInfo = facilityService.getFacilities(userEmail)
        return ResponseEntity.ok().body(ApiResponse.ok(facilityInfo))
    }

    // TODO 별도의 Admin 인증 권한 필요
    @GetMapping("/download/{id}")
    fun downloadCertificationDoc(
        @PathVariable("id") docId: Long
    ): ModelAndView {
        val downloadPath = facilityService.getCertificationDocDownloadPath(docId)
        return ModelAndView(RedirectView(downloadPath))
    }
}
