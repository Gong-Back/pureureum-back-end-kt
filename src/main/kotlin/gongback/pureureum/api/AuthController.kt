package gongback.pureureum.api

import gongback.pureureum.api.dto.ApiResponse
import gongback.pureureum.api.dto.LoginReq
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController {

    @PostMapping("/login")
    fun login(
        @RequestBody @Valid loginReq: LoginReq
    ): ResponseEntity<ApiResponse<String>> =
        ResponseEntity.ok(ApiResponse.success("Success!"))
}
