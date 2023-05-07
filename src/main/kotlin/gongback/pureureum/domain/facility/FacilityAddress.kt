package gongback.pureureum.domain.facility

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class FacilityAddress(
    @Column(length = 20)
    val city: String,

    @Column(length = 20)
    val county: String,

    @Column(length = 20)
    val district: String,

    @Column(length = 100)
    val jibun: String,

    @Column(length = 100)
    val detail: String,

    @Column(length = 20)
    val longitude: String,

    @Column(length = 20)
    val latitude: String
)
