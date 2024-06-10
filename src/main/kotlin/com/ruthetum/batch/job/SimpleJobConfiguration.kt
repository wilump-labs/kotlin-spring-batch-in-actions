package com.ruthetum.batch.job

import com.ruthetum.batch.logger
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager


@Configuration
class SimpleJobConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    private val log = logger()

    @Bean
    fun simpleJob(): Job {
        return JobBuilder("simpleJob", jobRepository)
            .start(simpleStep1())
            .build()
    }

    @Bean
    fun simpleStep1(): Step {
        return StepBuilder("simpleStep1", jobRepository)
            .tasklet(simpleTasklet(), transactionManager)
            .build()
    }

    fun simpleTasklet(): Tasklet {
        return Tasklet { _, _ ->
            log.info(">>>>> This is Step1");
            RepeatStatus.FINISHED
        }
    }
}