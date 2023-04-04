package gongback.pureureum.application

import gongback.pureureum.application.dto.UserInfoReq
import gongback.pureureum.application.dto.UserInfoRes
import gongback.pureureum.domain.sms.SmsLogRepository
import gongback.pureureum.domain.sms.getLastSmsLog
import gongback.pureureum.domain.user.User
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.existsByPhoneNumber
import gongback.pureureum.domain.user.existsNickname
import gongback.pureureum.domain.user.getUserByEmail
import gongback.pureureum.support.enum.FileType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional(readOnly = true)
class UserService(
    private val uploadService: UploadService,
    private val userRepository: UserRepository,
    private val smsLogRepository: SmsLogRepository,
) {
    fun getUserByEmail(email: String): User = userRepository.getUserByEmail(email)

    @Transactional
    fun updateUserInfo(user: User, userInfo: UserInfoReq) {
        userInfo.phoneNumber?.let {
            validatePhoneNumber(it)
            smsLogRepository.deleteByReceiver(user.phoneNumber)
            userRepository.getReferenceById(user.id).updatePhoneNumber(it)
        }
        userInfo.password?.let {
            userRepository.getReferenceById(user.id).updatePassword(it)
        }
        userInfo.nickname?.let {
            validateNickname(it)
            userRepository.getReferenceById(user.id).updateNickname(it)
        }
    }

    @Transactional
    fun updateProfile(user: User, updateProfile: MultipartFile?) {
        updateProfile?.apply {
            val originalFileName = validateFileName(updateProfile)
            val contentType = validateContentType(updateProfile)
            val fileKey = uploadService.uploadFile(updateProfile, FileType.PROFILE, originalFileName)
            user.profile.updateProfile(fileKey, contentType, originalFileName)
            userRepository.save(user)
        }
    }

    fun getUserInfoWithProfileUrl(user: User): UserInfoRes {
        val profileUrl = uploadService.getFileUrl(user.profile.fileKey)
        return UserInfoRes.toUserWithProfileUrl(user, profileUrl)
    }

    private fun validateFileName(file: MultipartFile): String {
        val originalFileName = (file.originalFilename ?: throw IllegalArgumentException("원본 파일 이름이 존재하지 않습니다"))
        require(originalFileName.isNotBlank()) { throw IllegalArgumentException("원본 파일 이름이 비어있습니다") }
        return originalFileName
    }

    private fun validateContentType(file: MultipartFile): String {
        val contentType = file.contentType
            ?: throw IllegalArgumentException("파일 형식이 유효하지 않습니다")
        require(contentType.startsWith("image")) { "이미지 형식의 파일만 가능합니다" }
        return contentType
    }

    private fun validatePhoneNumber(phoneNumber: String) {
        require(!userRepository.existsByPhoneNumber(phoneNumber)) { "이미 가입된 전화번호입니다" }
        require(smsLogRepository.getLastSmsLog(phoneNumber).isSuccess) { "본인 인증되지 않은 정보입니다" }
    }

    private fun validateNickname(it: String) {
        require(!userRepository.existsNickname(it)) { "이미 존재하는 닉네임입니다" }
    }
}
