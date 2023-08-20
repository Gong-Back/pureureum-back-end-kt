package gongback.pureureum.infra.file

import com.amazonaws.HttpMethod.GET
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import gongback.pureureum.application.S3Exception
import gongback.pureureum.application.StorageService
import gongback.pureureum.application.dto.FileInfo
import gongback.pureureum.support.constant.FileType
import java.util.*
import org.springframework.stereotype.Service

@Service
class AmazonS3Service(
    private val s3Client: AmazonS3,
    private val s3Properties: S3Properties
) : StorageService {
    override fun uploadFile(image: FileInfo, fileType: FileType, serverFileName: String): String {
        val bucketName = s3Properties.bucketName
        val fileKey = getSaveFilePath(fileType) + serverFileName
        val objectMetadata = ObjectMetadata().apply {
            contentLength = image.size
            contentType = image.contentType
        }

        return execute {
            s3Client.putObject(bucketName, fileKey, image.inputStream, objectMetadata)
            fileKey
        }
    }

    override fun getUrl(fileKey: String): String {
        val urlValidTime = s3Properties.urlValidTime
        val bucketName = s3Properties.bucketName
        val expiration = Date(System.currentTimeMillis() + urlValidTime)
        val preSignedUrlRequest = GeneratePresignedUrlRequest(bucketName, fileKey)
            .withMethod(GET)
            .withExpiration(expiration)

        return execute {
            s3Client.generatePresignedUrl(preSignedUrlRequest).toString()
        }
    }

    override fun deleteFile(fileKey: String) {
        val bucketName = s3Properties.bucketName
        return execute {
            s3Client.deleteObject(bucketName, fileKey)
        }
    }

    private fun getSaveFilePath(fileType: FileType): String {
        return when (fileType) {
            FileType.PROFILE -> s3Properties.profileFolderName
            FileType.FACILITY_CERTIFICATION -> s3Properties.facilityCertificationFolderName
            FileType.PROJECT -> s3Properties.projectFileFolderName
        }
    }

    private fun <T> execute(operation: () -> T): T {
        return runCatching { operation() }
            .getOrElse { throw S3Exception(it) }
    }
}
