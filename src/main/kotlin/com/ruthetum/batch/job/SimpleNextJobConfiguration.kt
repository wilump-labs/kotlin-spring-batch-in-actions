package com.ruthetum.batch.job

import com.ruthetum.batch.logger
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager


@Configuration
class SimpleNextJobConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    private val log = logger()

    @Bean
    fun stepNextJob(): Job {
        return JobBuilder("stepNextJob", jobRepository)
            .start(simpleNextStep1())
            .next(simpleNextStep2())
            .next(simpleNextStep3())
            .build()
    }

    @Bean
    fun simpleNextStep1(): Step {
        return StepBuilder("simpleNextStep1", jobRepository)
            .tasklet(
                { _, _ ->
                    log.info(">>>>> This is Step1")
                    RepeatStatus.FINISHED
                },
                transactionManager,
            )
            .build()
    }

    @Bean
    fun simpleNextStep2(): Step {
        return StepBuilder("simpleNextStep2", jobRepository)
            .tasklet(
                { _, _ ->
                    log.info(">>>>> This is Step2")
                    RepeatStatus.FINISHED
                },
                transactionManager,
            )
            .build()
    }

    @Bean
    fun simpleNextStep3(): Step {
        return StepBuilder("simpleNextStep3", jobRepository)
            .tasklet(
                { _, _ ->
                    log.info(">>>>> This is Step3")
                    RepeatStatus.FINISHED
                },
                transactionManager,
            )
            .build()
    }
}