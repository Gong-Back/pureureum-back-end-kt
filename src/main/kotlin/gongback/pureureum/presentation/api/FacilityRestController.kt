package gongback.pureureum.presentation.api

import gongback.pureureum.application.FacilityReadService
import gongback.pureureum.application.FacilityWriteService
import gongback.pureureum.application.FileHandlingException
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
    private val facilityReadService: FacilityReadService,
    private val facilityWriteService: FacilityWriteService
) {
    @PostMapping("/register")
    fun registerFacility(
        @RequestPart @Valid facilityReq: FacilityReq,
        @RequestPart(required = false) certificationDocs: List<MultipartFile>?,
        @LoginEmail userEmail: String
    ): ResponseEntity<ApiResponse<Unit>> {
        val savedFacilityId = facilityWriteService.registerFacility(userEmail, facilityReq)
        return try {
            certificationDocs?.let {
                val facilityCertificationDocsDto = facilityWriteService.uploadCertificationDocs(it)
                facilityWriteService.saveFacilityFiles(savedFacilityId, facilityCertificationDocsDto)
            }
            ResponseEntity.status(HttpStatus.CREATED).build()
        } catch (e: FileHandlingException) {
            facilityWriteService.deleteFacility(savedFacilityId)
            ResponseEntity.status(e.errorCode.httpStatus)
                .body(ApiResponse.error(e.errorCode.code, e.message ?: e.errorCode.message))
        }
    }

    @GetMapping("/me")
    fun getApprovedFacilitiesByCategory(
        @RequestParam("category") category: Category,
        @LoginEmail userEmail: String
    ): ResponseEntity<ApiResponse<List<FacilityRes>>> {
        val facilityRes = facilityReadService.getApprovedFacilityByCategory(userEmail, category)
        return ResponseEntity.ok().body(ApiResponse.ok(facilityRes))
    }

    @GetMapping("/all")
    fun getAllFacilities(
        @LoginEmail userEmail: String
    ): ResponseEntity<ApiResponse<List<FacilityResWithProgress>>> {
        val facilityRes = facilityReadService.getAllFacilities(userEmail)
        return ResponseEntity.ok().body(ApiResponse.ok(facilityRes))
    }
}
