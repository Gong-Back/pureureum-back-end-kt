package gongback.pureureum.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
@EnableAsync
class AsyncConfig {

    @Bean
    fun taskExecutor(): TaskExecutor {
        val threadPoolTaskExecutor = ThreadPoolTaskExecutor()
        threadPoolTaskExecutor.corePoolSize = 5
        threadPoolTaskExecutor.maxPoolSize = 10
        threadPoolTaskExecutor.queueCapacity = 10
        return threadPoolTaskExecutor
    }
}
