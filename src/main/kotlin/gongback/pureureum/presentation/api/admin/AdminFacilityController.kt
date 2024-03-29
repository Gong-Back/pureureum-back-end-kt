package gongback.pureureum.presentation.api.admin

import gongback.pureureum.application.FacilityReadService
import gongback.pureureum.application.FacilityWriteService
import gongback.pureureum.application.dto.FacilityRes
import gongback.pureureum.application.dto.FacilityWithDocIds
import gongback.pureureum.domain.facility.FacilityProgress
import gongback.pureureum.presentation.api.ApiResponse
import gongback.pureureum.support.constant.Category
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
@RequestMapping("/admin/facility")
class AdminFacilityController(
    private val facilityReadService: FacilityReadService,
    private val facilityWriteService: FacilityWriteService
) {

    @GetMapping("/download/{id}/{docId}")
    fun downloadCertificationDoc(
        @PathVariable("id") id: Long,
        @PathVariable("docId") docId: Long
    ): ModelAndView {
        val downloadPath = facilityReadService.getCertificationDocDownloadPath(id, docId)
        return ModelAndView(RedirectView(downloadPath))
    }

    @GetMapping("/all")
    fun getNotApprovedFacility(
        @RequestParam("category") category: Category
    ): ResponseEntity<ApiResponse<List<FacilityRes>>> {
        val facilityRes = facilityReadService.getNotApprovedFacilitiesByCategory(category)
        return ResponseEntity.ok(ApiResponse.ok(facilityRes))
    }

    @GetMapping("/{id}")
    fun getFacility(
        @PathVariable("id") id: Long
    ): ResponseEntity<ApiResponse<FacilityWithDocIds>> {
        val facilityRes = facilityReadService.getFacilityById(id)
        return ResponseEntity.ok(ApiResponse.ok(facilityRes))
    }

    @PostMapping("/update/{id}")
    fun updateFacilityProgress(
        @PathVariable("id") id: Long,
        @RequestParam("progress") progress: FacilityProgress
    ): ResponseEntity<Unit> {
        facilityWriteService.updateFacilityProgress(id, progress)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/update")
    fun updateFacilitiesProgress(
        @RequestParam("ids") ids: List<Long>,
        @RequestParam("progress") progress: FacilityProgress
    ): ResponseEntity<Unit> {
        facilityWriteService.updateFacilitiesProgress(ids, progress)
        return ResponseEntity.noContent().build()
    }
}
