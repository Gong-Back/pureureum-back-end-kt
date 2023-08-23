package gongback.pureureum.support.event

import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionalEventListener

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Transactional
@TransactionalEventListener
annotation class EventListenerWithTransaction()
