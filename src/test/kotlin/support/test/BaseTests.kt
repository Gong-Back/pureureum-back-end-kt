package support.test

import gongback.pureureum.config.EntityManagerConfig
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor

class BaseTests {
    @ActiveProfiles("test")
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
    annotation class TestEnvironment

    @DataJpaTest
    @TestEnvironment
    @Target(AnnotationTarget.CLASS)
    @Import(EntityManagerConfig::class)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class RepositoryTest
}
