package support

import gongback.pureureum.application.dto.FileDto
import org.springframework.mock.web.MockMultipartFile

fun createFileDto(
    file: MockMultipartFile
): FileDto = FileDto(
    file.size,
    file.inputStream,
    file.contentType!!,
    file.originalFilename
)
