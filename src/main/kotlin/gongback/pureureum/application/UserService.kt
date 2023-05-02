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
import gongback.pureureum.support.constant.FileType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional(readOnly = true)
class UserService(
    private val uploadService: UploadService,
    private val userRepository: UserRepository,
    private val smsLogRepository: SmsLogRepository
) {
    fun getUserByEmail(email: String): User = userRepository.getUserByEmail(email)

    @Transactional
    fun updateUserInfo(email: String, userInfo: UserInfoReq) {
        val findUser = userRepository.getUserByEmail(email)
        userInfo.phoneNumber?.let {
            validatePhoneNumber(it)
            smsLogRepository.deleteByReceiver(findUser.phoneNumber)
            findUser.updatePhoneNumber(it)
        }
        userInfo.password?.let {
            findUser.updatePassword(it)
        }
        userInfo.nickname?.let {
            validateNickname(it)
            findUser.updateNickname(it)
        }
    }

    @Transactional
    fun updatedProfile(email: String, updateProfile: MultipartFile?) {
        updateProfile?.apply {
            val originalFileName = uploadService.validateFileName(updateProfile)
            val contentType = uploadService.getImageType(updateProfile)

            val findUser = userRepository.getUserByEmail(email)
            if (findUser.profile.originalFileName != "default_profile.png") {
                uploadService.deleteFile(findUser.profile.fileKey)
            }
            val fileKey = uploadService.uploadFile(updateProfile, FileType.PROFILE, originalFileName)

            findUser.profile.updateProfile(fileKey, contentType, originalFileName)
            userRepository.save(findUser)
        }
    }

    fun getUserInfoWithProfileUrl(email: String): UserInfoRes {
        val user = userRepository.getUserByEmail(email)
        val profileUrl = uploadService.getFileUrl(user.profile.fileKey)
        return UserInfoRes.toUserWithProfileUrl(user, profileUrl)
    }

    private fun validatePhoneNumber(phoneNumber: String) {
        require(!userRepository.existsByPhoneNumber(phoneNumber)) { "이미 가입된 전화번호입니다" }
        require(smsLogRepository.getLastSmsLog(phoneNumber).isSuccess) { "본인 인증되지 않은 정보입니다" }
    }

    private fun validateNickname(it: String) {
        require(!userRepository.existsNickname(it)) { "이미 존재하는 닉네임입니다" }
    }
}
