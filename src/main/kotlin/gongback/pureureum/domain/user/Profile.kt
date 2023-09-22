package gongback.pureureum.domain.user

import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity
class Profile(
    fileKey: String,

    contentType: String,

    originalFileName: String
) : BaseEntity() {
    @Column(nullable = false)
    var fileKey: String = fileKey
        protected set

    @Column(nullable = false)
    var contentType: String = contentType
        protected set

    @Column(nullable = false)
    var originalFileName: String = originalFileName
        protected set

    companion object {
        fun defaultProfile(): Profile {
            return Profile(
                "profile/default_profile.png",
                "image/png",
                "default_profile.png"
            )
        }
    }

    fun updateProfile(profile: Profile) {
        this.fileKey = profile.fileKey
        this.contentType = profile.contentType
        this.originalFileName = profile.originalFileName
    }
}
