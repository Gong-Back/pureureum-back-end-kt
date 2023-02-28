package gongback.pureureum.api

import gongback.pureureum.api.properties.DocsProperties
import org.springframework.context.annotation.Profile
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Profile("local")
class LocalDocsController(
    private val resourceLoader: ResourceLoader,
    private val docsProperties: DocsProperties
) {
    @GetMapping("/api/docs", produces = [MediaType.TEXT_HTML_VALUE])
    fun localDocs(): Resource = resourceLoader.getResource(docsProperties.localFilePath)
}

@RestController
@Profile("prod")
class ProdDocsController(
    private val resourceLoader: ResourceLoader,
    private val docsProperties: DocsProperties
) {
    @GetMapping("/api/docs", produces = [MediaType.TEXT_HTML_VALUE])
    fun prodDocs(): Resource = resourceLoader.getResource(docsProperties.prodFilePath)
}
