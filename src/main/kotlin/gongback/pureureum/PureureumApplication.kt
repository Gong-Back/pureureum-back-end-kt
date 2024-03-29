package gongback.pureureum

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class PureureumApplication

fun main(args: Array<String>) {
    runApplication<PureureumApplication>(*args)
}
