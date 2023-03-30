package gongback.pureureum.config

import com.amazonaws.ClientConfiguration
import com.amazonaws.Protocol
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import gongback.pureureum.application.properties.S3Properties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AmazonS3Config(
    private val s3Properties: S3Properties
) {
    @Bean
    fun s3Client(awsCredentials: AWSCredentials, clientConfiguration: ClientConfiguration): AmazonS3 =
        AmazonS3Client.builder()
            .withRegion(Regions.AP_NORTHEAST_2)
            .withForceGlobalBucketAccessEnabled(true)
            .withClientConfiguration(clientConfiguration)
            .withCredentials(AWSStaticCredentialsProvider(awsCredentials))
            .build()

    @Bean
    fun awsCredentials() = BasicAWSCredentials(s3Properties.awsAccessKey, s3Properties.awsSecretKey)

    @Bean
    fun clientConfiguration(): ClientConfiguration = ClientConfiguration()
        .withTcpKeepAlive(true)
        .withMaxConnections(100)
        .withProtocol(Protocol.HTTP)
        .withMaxErrorRetry(15)
}
