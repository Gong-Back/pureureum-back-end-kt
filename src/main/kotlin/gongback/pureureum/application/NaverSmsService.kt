package gongback.pureureum.application

import gongback.pureureum.application.dto.MessageDto
import gongback.pureureum.application.dto.NaverSendMessageDto
import gongback.pureureum.application.dto.PhoneNumberReq
import gongback.pureureum.application.dto.SmsSendResponse
import gongback.pureureum.application.properties.NaverSmsProperties
import gongback.pureureum.domain.sms.SmsLog
import gongback.pureureum.domain.sms.SmsLogRepository
import gongback.pureureum.domain.sms.getLastSmsLog
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val ALGORITHM = "HmacSHA256"
private const val CHARSET_NAME = "UTF-8"
private const val SENDING_LIMIT = 50

@Service
class NaverSmsService(
    private val webClient: WebClient,
    private val smsLogRepository: SmsLogRepository,
    private val naverSmsProperties: NaverSmsProperties
) : SmsService {

    private val SEND_URI = "/sms/v2/services/${naverSmsProperties.serviceId}/messages"

    override fun sendSmsCertification(phoneNumberReq: PhoneNumberReq): SmsSendResponse {
        if (smsLogRepository.getTotalSize() > SENDING_LIMIT) {
            throw SmsOverRequestException()
        }

        val certificationNumber = getCertificationNumber()
        smsLogRepository.save(SmsLog(phoneNumberReq.phoneNumber))

        sendMessage(phoneNumberReq.receiver, certificationNumber)

        return SmsSendResponse(certificationNumber = certificationNumber)
    }

    @Transactional
    override fun completeCertification(phoneNumberReq: PhoneNumberReq) {
        smsLogRepository.getLastSmsLog(phoneNumberReq.phoneNumber).completeCertification()
    }

    private fun sendMessage(receiver: String, randomNumber: String) {
        val currentTimeMillis = System.currentTimeMillis()

        webClient
            .post()
            .uri(naverSmsProperties.domain + SEND_URI)
            .headers {
                it.contentType = MediaType.APPLICATION_JSON
                it.set("x-ncp-apigw-timestamp", currentTimeMillis.toString())
                it.set("x-ncp-iam-access-key", naverSmsProperties.accessKey)
                it.set("x-ncp-apigw-signature-v2", makeSignature(currentTimeMillis))
            }
            .bodyValue(
                NaverSendMessageDto(
                    from = naverSmsProperties.phoneNumber,
                    content = "[$randomNumber] ${naverSmsProperties.smsCertificationContent}",
                    messages = listOf(MessageDto(to = receiver))
                )
            )
            .retrieve()
            .onStatus({ httpStatusCode -> httpStatusCode.is4xxClientError }) { clientResponse ->
                clientResponse.bodyToMono(String::class.java)
                    .map { _ -> SmsSendException() }
            }
            .onStatus({ httpStatusCode -> httpStatusCode.is5xxServerError }) { clientResponse ->
                clientResponse.bodyToMono(String::class.java)
                    .map { _ -> SmsSendException() }
            }
            .bodyToMono(String::class.java)
            .block()
    }

    private fun getCertificationNumber(): String {
        val randomCertificationNumber = StringBuilder()
        var size = naverSmsProperties.size
        while (size-- > 0) {
            randomCertificationNumber.append((0..9).random())
        }
        return randomCertificationNumber.toString()
    }

    private fun makeSignature(
        currentTimeMillis: Long
    ): String? {
        val newLine = "\n"
        val message = StringBuilder()
            .append("POST")
            .append(" ")
            .append(SEND_URI)
            .append(newLine)
            .append(currentTimeMillis)
            .append(newLine)
            .append(naverSmsProperties.accessKey)
            .toString()

        val signingKey = SecretKeySpec(naverSmsProperties.secretKey.toByteArray(charset(CHARSET_NAME)), ALGORITHM)

        val mac = Mac.getInstance(ALGORITHM)
        mac.init(signingKey)

        val rawHmac = mac.doFinal(message.toByteArray(charset(CHARSET_NAME)))

        return Base64.getEncoder().encodeToString(rawHmac)
    }
}
