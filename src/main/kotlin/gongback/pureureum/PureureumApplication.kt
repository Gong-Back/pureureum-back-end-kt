package gongback.pureureum

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [SecurityAutoConfiguration::class])
class PureureumApplication

fun main(args: Array<String>) {
	runApplication<PureureumApplication>(*args)
}
