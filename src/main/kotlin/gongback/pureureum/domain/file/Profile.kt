package gongback.pureureum.domain.file

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Profile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false)
    val fileKey: String,

    @Column(nullable = false)
    val contentType: String,

    @Column(nullable = false)
    val originalFileName: String,

    @Column(unique = true)
    val serverFileName: String
) {
    constructor(
        fileKey: String,
        contentType: String,
        originalFileName: String,
        serverFileName: String,
        id: Long = 0L
    ) : this(id, fileKey, contentType, originalFileName, serverFileName)

    companion object {
        fun defaultProfile(): Profile {
            return Profile(
                1L,
                "profile/default_profile.png",
                "image/png",
                "default_profile.png",
                "default_profile.png"
            )
        }
    }
}
