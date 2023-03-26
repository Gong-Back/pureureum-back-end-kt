package gongback.pureureum.domain.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(name = "temp_social_auth", indexes = [Index(name = "idx_email", columnList = "email")])
class TempSocialAuth(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(unique = true, nullable = false)
    val email: String,

    val name: String? = null,

    val birthday: String? = null,

    val phoneNumber: String? = null,

    val gender: Gender? = null,

    @Enumerated(EnumType.STRING)
    val socialType: SocialType? = null
)
