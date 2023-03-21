package gongback.pureureum.application.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "api-docs")
class DocsProperties(
    val localFilePath: String = "",
    val prodFilePath: String = ""
)
