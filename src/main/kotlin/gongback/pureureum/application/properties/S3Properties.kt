package gongback.pureureum.application.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "s3")
class S3Properties(
    val awsAccessKey: String = "",
    val awsSecretKey: String = "",
    val urlValidTime: Int = 0,
    val bucketName: String = "",
    val profileFolderName: String = "",
    val facilityCertificationFolderName: String = "",
    val projectFileFolderName: String = ""
)
