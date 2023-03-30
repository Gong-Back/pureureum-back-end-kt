package support

import gongback.pureureum.domain.file.Profile
import org.springframework.mock.web.MockMultipartFile

private const val PROFILE_KEY = "profile/default_profile.png"
private const val PROFILE_NAME = "profile"
private const val PROFILE_ORIGINAL_FILE_NAME = "default_profile.png"
private const val PROFILE_SERVER_FILE_NAME = "server_default_profile.png"
private const val PROFILE_CONTENT_TYPE = "image/png"
private const val PROFILE_CONTENT = "sample"
const val PROFILE_URL = "http://pureureum.com/profile/default_profile.png"

fun createProfile(
    fileKey: String = PROFILE_KEY,
    contentType: String = PROFILE_CONTENT_TYPE,
    originalFileName: String = PROFILE_ORIGINAL_FILE_NAME,
    serverFileName: String = PROFILE_SERVER_FILE_NAME
): Profile {
    return Profile(
        fileKey = fileKey,
        contentType = contentType,
        originalFileName = originalFileName,
        serverFileName = serverFileName
    )
}

fun createMockFile(
    name: String = PROFILE_NAME,
    originalFileName: String = PROFILE_ORIGINAL_FILE_NAME,
    contentType: String = PROFILE_CONTENT_TYPE,
    content: String = PROFILE_CONTENT
): MockMultipartFile {
    return MockMultipartFile(
        name,
        originalFileName,
        contentType,
        content.toByteArray()
    )
}
