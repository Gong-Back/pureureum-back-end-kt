package gongback.pureureum.application

import gongback.pureureum.application.dto.UserInfoReq
import gongback.pureureum.application.dto.UserInfoRes
import gongback.pureureum.domain.file.FileType
import gongback.pureureum.domain.file.Profile
import gongback.pureureum.domain.user.User
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.existsNickname
import gongback.pureureum.domain.user.getUserByEmail
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val userAuthenticationService: UserAuthenticationService,
    private val profileService: ProfileService
) {
    fun getUserByEmail(email: String): User = userRepository.getUserByEmail(email)

    fun getUserInfo(user: User): UserInfoRes {
        return UserInfoRes(
            user.email,
            user.phoneNumber,
            user.name,
            user.nickname,
            user.gender,
            user.birthday,
            user.profile.id
        )
    }

    @Transactional
    fun updateUserInfo(user: User, userInfo: UserInfoReq) {
        userInfo.phoneNumber?.let {
            validatePhoneNumber(it)
            userAuthenticationService.deleteByPhoneNumber(user.phoneNumber)
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
    fun updateProfile(user: User, profile: MultipartFile?) {
        val updatedProfile = profile?.let {
            profileService.uploadFile(profile, FileType.PROFILE)
                .toProfile()
        } ?: Profile.defaultProfile()

        if (user.profile.id != Profile.defaultProfile().id) {
            profileService.deleteFile(user.profile.id, user.profile.fileKey)
        }
        profileService.save(updatedProfile)
        userRepository.updateProfile(updatedProfile, user.id)
    }

    private fun validatePhoneNumber(it: String) {
        userAuthenticationService.checkDuplicatedPhoneNumber(it)
        userAuthenticationService.validateCertifiedPhoneNumber(it)
    }

    private fun validateNickname(it: String) {
        require(!userRepository.existsNickname(it)) { "이미 존재하는 닉네임입니다" }
    }
}
