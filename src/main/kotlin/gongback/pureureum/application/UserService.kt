package gongback.pureureum.application

import gongback.pureureum.application.dto.FileDto
import gongback.pureureum.application.dto.ProfileDto
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

private const val DEFAULT_FILE_NAME = "default_profile.png"

@Service
@Transactional(readOnly = true)
class UserReadService(
    private val fileService: FileService,
    private val userRepository: UserRepository
) {
    fun getUserByEmail(email: String): User = userRepository.getUserByEmail(email)

    fun getUserInfoWithProfileUrl(email: String): UserInfoRes {
        val user = userRepository.getUserByEmail(email)
        val profileUrl = fileService.getFileUrl(user.profile.fileKey)
        return UserInfoRes.toUserWithProfileUrl(user, profileUrl)
    }
}

@Service
@Transactional(readOnly = true)
class UserWriteService(
    private val fileService: FileService,
    private val userRepository: UserRepository,
    private val smsLogRepository: SmsLogRepository
) {

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

    fun uploadProfileImage(email: String, newProfile: MultipartFile): ProfileDto =
        with(newProfile) {
            val contentType = fileService.validateImageType(newProfile.contentType)
            val originalFileName = fileService.validateFileName(newProfile.originalFilename)
            val findUser = userRepository.getUserByEmail(email)
            if (findUser.profile.originalFileName != DEFAULT_FILE_NAME) {
                fileService.deleteFile(findUser.profile.fileKey)
            }
            val fileDto = FileDto(newProfile.size, newProfile.inputStream, contentType, originalFileName)
            val fileKey = fileService.uploadFile(fileDto, FileType.PROFILE)
            ProfileDto(fileKey, contentType, originalFileName)
        }

    @Transactional
    fun updateProfile(email: String, profileDto: ProfileDto) {
        val findUser = userRepository.getUserByEmail(email)
        val newProfile = profileDto.toEntity()
        findUser.updateProfile(newProfile)
    }

    private fun validatePhoneNumber(phoneNumber: String) {
        require(!userRepository.existsByPhoneNumber(phoneNumber)) { "이미 가입된 전화번호입니다" }
        require(smsLogRepository.getLastSmsLog(phoneNumber).isSuccess) { "본인 인증되지 않은 정보입니다" }
    }

    private fun validateNickname(it: String) {
        require(!userRepository.existsNickname(it)) { "이미 존재하는 닉네임입니다" }
    }
}
