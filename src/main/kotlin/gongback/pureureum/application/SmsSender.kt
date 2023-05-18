package gongback.pureureum.application

interface SmsSender {
    fun send(receiver: String, certificationNumber: String, monthCounts: Long)
}
