package gongback.pureureum.domain.facility

import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import java.util.*

@Entity
class Facility(
    val name: String,

    @Embedded
    val address: FacilityAddress,

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val category: FacilityCategory,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    var progress: FacilityProgress,

    certificationDoc: MutableList<FacilityCertificationDoc> = Collections.emptyList()
) : BaseEntity() {
    @OneToMany(
        fetch = FetchType.LAZY,
        cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE],
        orphanRemoval = true
    )
    @JoinColumn(
        name = "facility_id",
        foreignKey = ForeignKey(name = "fk_facility_certification_document_id_ref_facility_id")
    )
    val certificationDoc: MutableList<FacilityCertificationDoc> = certificationDoc.toMutableList()

    fun addCertificationDoc(facilityCertificationDoc: FacilityCertificationDoc) {
        certificationDoc.add(facilityCertificationDoc)
    }

    fun updateProgress(progress: FacilityProgress) {
        this.progress = progress
    }
}
