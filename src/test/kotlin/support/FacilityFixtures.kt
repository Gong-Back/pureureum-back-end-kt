package support

import gongback.pureureum.application.dto.FacilityReq
import gongback.pureureum.application.dto.FacilityRes
import gongback.pureureum.application.dto.FacilityResWithProgress
import gongback.pureureum.application.dto.FacilityWithDocIds
import gongback.pureureum.domain.facility.Facility
import gongback.pureureum.domain.facility.FacilityAddress
import gongback.pureureum.domain.facility.FacilityCertificationDoc
import gongback.pureureum.domain.facility.FacilityProgress
import gongback.pureureum.domain.user.User
import gongback.pureureum.support.constant.Category
import org.springframework.mock.web.MockMultipartFile
import java.util.*

const val FACILITY_ID: Long = 1L
const val FACILITY_NAME: String = "test_name"
const val FACILITY_CITY: String = "test_city"
const val FACILITY_COUNTY: String = "test_county"
const val FACILITY_DISTRICT: String = "test_district"
const val FACILITY_DETAIL: String = "test_detail"

const val CERTIFICATION_DOC_NAME = "certificationDoc"
const val CERTIFICATION_DOC_ORIGINAL_FILE_NAME = "test_certification_file_name"
const val CERTIFICATION_DOC_TYPE = "image/png"
const val CERTIFICATION_DOC_CONTENT = "sample"
const val CERTIFICATION_DOC_FILE_KEY = "facility/certification/sample.png"

val FACILITY_CATEGORY: Category = Category.YOUTH_FARMING
val FACILITY_PROGRESS: FacilityProgress = FacilityProgress.NOT_APPROVED

fun createFacility(
    name: String = FACILITY_NAME,
    city: String = FACILITY_CITY,
    county: String = FACILITY_COUNTY,
    district: String = FACILITY_DISTRICT,
    detail: String = FACILITY_DETAIL,
    user: User = createUser(),
    progress: FacilityProgress = FACILITY_PROGRESS,
    certificationDoc: MutableList<FacilityCertificationDoc> = Collections.emptyList()
): Facility {
    return Facility(
        name,
        FacilityAddress(city, county, district, detail),
        FACILITY_CATEGORY,
        user.id,
        progress,
        certificationDoc
    )
}

fun createFacilityReq(
    category: Category = FACILITY_CATEGORY,
    name: String = FACILITY_NAME,
    city: String = FACILITY_CITY,
    county: String = FACILITY_COUNTY,
    district: String = FACILITY_DISTRICT,
    detail: String = FACILITY_DETAIL
): FacilityReq {
    return FacilityReq(
        category,
        name,
        city,
        county,
        district,
        detail
    )
}

fun createMockCertificationDoc(
    name: String = CERTIFICATION_DOC_NAME,
    originalFileName: String? = CERTIFICATION_DOC_ORIGINAL_FILE_NAME,
    contentType: String? = CERTIFICATION_DOC_TYPE,
    content: String = CERTIFICATION_DOC_CONTENT
): MockMultipartFile {
    return MockMultipartFile(
        name,
        originalFileName,
        contentType,
        content.toByteArray()
    )
}

fun createCertificationDoc(
    fileKey: String = CERTIFICATION_DOC_FILE_KEY,
    contentType: String = CERTIFICATION_DOC_TYPE,
    originalFileName: String = CERTIFICATION_DOC_ORIGINAL_FILE_NAME
): FacilityCertificationDoc {
    return FacilityCertificationDoc(
        fileKey,
        contentType,
        originalFileName
    )
}

fun createFacilityRes(
    id: Long = 0L,
    category: Category = FACILITY_CATEGORY,
    name: String = FACILITY_NAME,
    city: String = FACILITY_CITY,
    county: String = FACILITY_COUNTY,
    district: String = FACILITY_DISTRICT,
    detail: String = FACILITY_DETAIL
): FacilityRes {
    return FacilityRes(
        id,
        category,
        name,
        city,
        county,
        district,
        detail
    )
}

fun createFacilityResWithProgress(
    id: Long = FACILITY_ID,
    category: Category = FACILITY_CATEGORY,
    name: String = FACILITY_NAME,
    city: String = FACILITY_CITY,
    county: String = FACILITY_COUNTY,
    district: String = FACILITY_DISTRICT,
    detail: String = FACILITY_DETAIL,
    progress: FacilityProgress = FACILITY_PROGRESS
): FacilityResWithProgress {
    return FacilityResWithProgress(
        id,
        category,
        name,
        city,
        county,
        district,
        detail,
        progress
    )
}

fun createFacilityWithDocIds(
    id: Long = 0L,
    category: Category = FACILITY_CATEGORY,
    name: String = FACILITY_NAME,
    city: String = FACILITY_CITY,
    county: String = FACILITY_COUNTY,
    district: String = FACILITY_DISTRICT,
    detail: String = FACILITY_DETAIL,
    fileIds: List<Long> = Collections.emptyList()
): FacilityWithDocIds {
    return FacilityWithDocIds(
        id,
        category,
        name,
        city,
        county,
        district,
        detail,
        fileIds
    )
}
