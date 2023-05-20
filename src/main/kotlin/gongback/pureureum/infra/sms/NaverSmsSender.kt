package gongback.pureureum.infra.sms

import gongback.pureureum.application.SmsSendException
import gongback.pureureum.application.SmsSender
import gongback.pureureum.application.dto.MessageDto
import gongback.pureureum.application.dto.NaverSendMessageDto
import gongback.pureureum.application.dto.SmsRequestDto
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val ALGORITHM = "HmacSHA256"
private const val CHARSET_NAME = "UTF-8"
private const val SENDING_LIMIT = 50

@Component
class NaverSmsSender(
    private val webClient: WebClient,
    private val naverSmsProperties: NaverSmsProperties
) : SmsSender {

    private val sendUrl = "/sms/v2/services/${naverSmsProperties.serviceId}/messages"

    override fun send(smsRequestDto: SmsRequestDto) {
        if (smsRequestDto.monthlySmsCount > SENDING_LIMIT) {
            throw SmsOverRequestException()
        }

        sendMessage(smsRequestDto.receiver, smsRequestDto.certificationNumber)
    }

    private fun sendMessage(receiver: String, randomNumber: String) {
        val currentTimeMillis = System.currentTimeMillis()

        webClient
            .post()
            .uri(naverSmsProperties.domain + sendUrl)
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

    private fun makeSignature(
        currentTimeMillis: Long
    ): String? {
        val newLine = "\n"
        val message = StringBuilder()
            .append("POST")
            .append(" ")
            .append(sendUrl)
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
