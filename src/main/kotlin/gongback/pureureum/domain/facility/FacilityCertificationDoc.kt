package gongback.pureureum.domain.facility

import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity
class FacilityCertificationDoc(
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
}
