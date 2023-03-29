package gongback.pureureum.application.util

import org.springframework.stereotype.Component
import java.util.UUID

@Component
class FileNameGenerator : NameGenerator {
    override fun generate(): String {
        return UUID.randomUUID().toString()
    }
}
