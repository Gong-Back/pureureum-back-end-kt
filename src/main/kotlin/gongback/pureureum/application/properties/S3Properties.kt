package gongback.pureureum.application.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "s3")
class S3Properties(
    val awsAccessKey: String,
    val awsSecretKey: String,
    val urlValidTime: Int,
    val bucketName: String,
    val profileFolderName: String
)
