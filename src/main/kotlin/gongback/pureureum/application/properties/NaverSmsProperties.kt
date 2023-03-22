package gongback.pureureum.application.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "sms.naver-cloud")
class NaverSmsProperties(
    val size: Int = 0,
    val domain: String = "",
    val accessKey: String = "",
    val secretKey: String = "",
    val serviceId: String = "",
    val phoneNumber: String = "",
    val smsCertificationContent: String = "본인 확인 인증번호를 입력하세요! [Pureureum]"
)
