package gongback.pureureum.application.util

import java.util.UUID
import org.springframework.stereotype.Component

@Component
class FileNameGenerator : NameGenerator {
    override fun generate(): String {
        return UUID.randomUUID().toString()
    }
}
