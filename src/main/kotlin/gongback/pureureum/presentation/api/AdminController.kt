package gongback.pureureum.presentation.api

import gongback.pureureum.application.FacilityService
import gongback.pureureum.application.dto.FacilityRes
import gongback.pureureum.application.dto.FacilityWithDocIds
import gongback.pureureum.domain.facility.FacilityProgress
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

@RestController
@RequestMapping("/admin")
class AdminController(
    private val facilityService: FacilityService
) {
    @GetMapping("/facility/download/{id}/{docId}")
    fun downloadCertificationDoc(
        @PathVariable("id") id: Long,
        @PathVariable("docId") docId: Long
    ): ModelAndView {
        val downloadPath = facilityService.getCertificationDocDownloadPath(id, docId)
        return ModelAndView(RedirectView(downloadPath))
    }

    @GetMapping("/facility/all")
    fun getNotApprovedFacility(
        @RequestParam("category") category: String
    ): ResponseEntity<ApiResponse<List<FacilityRes>>> {
        val facilityRes = facilityService.getNotApprovedFacilitiesByCategory(category)
        return ResponseEntity.ok(ApiResponse.ok(facilityRes))
    }

    @GetMapping("/facility/{id}")
    fun getFacility(
        @PathVariable("id") id: Long
    ): ResponseEntity<ApiResponse<FacilityWithDocIds>> {
        val facilityRes = facilityService.getFacilityById(id)
        return ResponseEntity.ok(ApiResponse.ok(facilityRes))
    }

    @PostMapping("/facility/update/{id}")
    fun updateFacilityProgress(
        @PathVariable("id") id: Long,
        @RequestParam("progress") progress: FacilityProgress
    ): ResponseEntity<Unit> {
        facilityService.updateFacilityProgress(id, progress)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/facility/update")
    fun updateFacilitiesProgress(
        @RequestParam("ids") ids: List<Long>,
        @RequestParam("progress") progress: FacilityProgress
    ): ResponseEntity<Unit> {
        facilityService.updateFacilitiesProgress(ids, progress)
        return ResponseEntity.ok().build()
    }
}
