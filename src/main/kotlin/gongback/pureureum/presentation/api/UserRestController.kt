package gongback.pureureum.presentation.api

import gongback.pureureum.application.UserService
import gongback.pureureum.application.dto.EmailReq
import gongback.pureureum.application.dto.LoginReq
import gongback.pureureum.application.dto.RegisterReq
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserRestController(
    private val userService: UserService
) {
    @PostMapping("/login")
    fun login(
        @RequestBody @Valid loginReq: LoginReq
    ): ResponseEntity<ApiResponse<String>> =
        ResponseEntity.ok(ApiResponse.success("Success!"))

    @PostMapping("/register")
    fun register(
        @RequestBody @Valid registerReq: RegisterReq
    ): ResponseEntity<ApiResponse<String>> {
        userService.register(registerReq)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @PostMapping("/check-email")
    fun checkDuplicatedEmail(
        @RequestBody @Valid emailReq: EmailReq
    ): ResponseEntity<Unit> {
        userService.checkDuplicatedEmail(emailReq.email)
        return ResponseEntity.ok().build()
    }
}
