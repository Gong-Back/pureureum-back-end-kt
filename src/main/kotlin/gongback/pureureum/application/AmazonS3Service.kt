package gongback.pureureum.application

import com.amazonaws.HttpMethod.GET
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import gongback.pureureum.application.properties.S3Properties
import gongback.pureureum.domain.file.FileType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class AmazonS3Service(
    private val s3Client: AmazonS3,
    private val s3Properties: S3Properties
) {
    fun uploadFile(image: MultipartFile, type: FileType, serverFileName: String): String {
        val bucketName = s3Properties.bucketName
        val fileKey = getSaveFilePath(type) + serverFileName
        val objectMetadata = ObjectMetadata().apply {
            contentLength = image.size
            contentType = image.contentType
        }
        s3Client.putObject(bucketName, fileKey, image.inputStream, objectMetadata)
            ?: throw S3UploadException()
        return fileKey
    }

    fun getUrl(fileKey: String): String {
        val urlValidTime = s3Properties.urlValidTime
        val bucketName = s3Properties.bucketName
        val expiration = Date(System.currentTimeMillis() + (urlValidTime * 60 * 1000))
        val preSignedUrlRequest = GeneratePresignedUrlRequest(bucketName, fileKey)
            .withMethod(GET)
            .withExpiration(expiration)
        return s3Client.generatePresignedUrl(preSignedUrlRequest).toString()
    }

    fun deleteFile(fileKey: String) {
        val bucketName = s3Properties.bucketName
        s3Client.deleteObject(bucketName, fileKey)
    }

    private fun getSaveFilePath(type: FileType): String {
        return if (type == FileType.PROFILE) s3Properties.profileFolderName else ""
    }
}
