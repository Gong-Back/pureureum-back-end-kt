package gongback.pureureum.domain.user

import gongback.pureureum.domain.social.SocialType
import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.AttributeOverride
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import java.time.LocalDate

@Entity
class User(
    @Embedded
    val information: UserInformation,

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "password", nullable = false))
    var password: Password,

    @Enumerated(EnumType.STRING)
    val socialType: SocialType,

    profile: Profile = Profile.defaultProfile()
) : BaseEntity() {
    @OneToOne(
        fetch = FetchType.LAZY,
        cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE],
        orphanRemoval = true
    )
    @JoinColumn(name = "profile_id", unique = true)
    var profile: Profile = profile
        protected set

    val email: String
        get() = information.email

    val phoneNumber: String
        get() = information.phoneNumber

    val name: String
        get() = information.name

    val userGender: UserGender
        get() = information.gender

    val birthday: LocalDate
        get() = information.birthday

    val userRole: UserRole
        get() = information.userRole

    val nickname: String
        get() = information.nickname

    constructor(
        email: String,
        phoneNumber: String,
        name: String,
        nickname: String,
        gender: UserGender,
        birthday: LocalDate,
        password: Password,
        userRole: UserRole,
        socialType: SocialType
    ) : this(
        UserInformation(name, email, phoneNumber, nickname, gender, birthday, userRole = userRole),
        password,
        socialType,
        Profile.defaultProfile()
    )

    fun authenticate(password: Password) {
        identify(this.password == password) { "비밀번호가 일치하지 않습니다" }
    }

    fun updatePhoneNumber(phoneNumber: String) {
        if (phoneNumber.isNotBlank()) {
            information.phoneNumber = phoneNumber
        }
    }

    fun updatePassword(password: Password) {
        if (password.value.isNotBlank()) {
            this.password = password
        }
    }

    fun updateNickname(nickname: String) {
        if (nickname.isNotBlank()) {
            information.nickname = nickname
        }
    }

    private fun identify(value: Boolean, message: () -> Any = {}) {
        if (!value) {
            throw IllegalArgumentException(message().toString())
        }
    }
}
