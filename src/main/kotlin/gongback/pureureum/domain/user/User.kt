package gongback.pureureum.domain.user

import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDate

@Entity
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Embedded
    var information: UserInformation,

    @Column(nullable = false)
    var password: String
) {
    val email: String
        get() = information.email

    val phoneNumber: String
        get() = information.phoneNumber

    val name: String
        get() = information.name

    val gender: Gender
        get() = information.gender

    val birthday: LocalDate
        get() = information.birthday

    constructor(
        email: String,
        phoneNumber: String,
        name: String,
        gender: Gender,
        birthday: LocalDate,
        password: String,
        id: Long = 0L
    ) : this(
        id,
        UserInformation(name, email, phoneNumber, gender, birthday),
        password
    )
}
