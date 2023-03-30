package gongback.pureureum.presentation.api

import gongback.pureureum.application.FileService
import gongback.pureureum.application.dto.FileRes
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/file")
class FileRestController(
    val fileService: FileService
) {
    @GetMapping("/url/{fileId}")
    fun getProfileUrl(
        @PathVariable("fileId") fileId: Long
    ): ResponseEntity<ApiResponse<FileRes>> {
        val fileUrl = fileService.getFileUrl(fileId)
        return ResponseEntity.ok().body(ApiResponse.ok(FileRes(fileUrl)))
    }
}
