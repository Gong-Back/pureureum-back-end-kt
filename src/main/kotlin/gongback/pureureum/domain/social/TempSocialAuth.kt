package gongback.pureureum.domain.social

import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(name = "temp_social_auth", indexes = [Index(name = "idx_email", columnList = "email")])
class TempSocialAuth(
    @Column(unique = true, nullable = false)
    val email: String,

    val name: String? = null,

    val birthday: String? = null,

    val phoneNumber: String? = null,

    val gender: SocialTempGender? = null,

    @Enumerated(EnumType.STRING)
    val socialType: SocialType? = null
) : BaseEntity()
