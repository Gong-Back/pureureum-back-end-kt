package gongback.pureureum.infra.sms

class SmsOverRequestException(message: String = "월 메시지 전송 한도를 초과했습니다") : RuntimeException(message)
