package com.ruthetum.batch.job

import com.ruthetum.batch.logger
import org.springframework.batch.core.*
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager


@Configuration
class StepNextConditionalJobConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    private val log = logger()

    @Bean
    fun stepNextConditionalJob(): Job {
        BatchStatus.COMPLETED

        ExitStatus.FAILED
        return JobBuilder("stepNextConditionalJob", jobRepository)
            .start(nextConditionalStep1())
                .on("FAILED") // FAILED 일 경우
                .to(nextConditionalStep3()) // nextConditionalStep3으로 이동한다.
                .on("*") // nextConditionalStep3의 결과 관계 없이
                .end() // nextConditionalStep3으로 이동하면 Flow가 종료한다.
            .from(nextConditionalStep1()) // nextConditionalStep1로부터
                .on("*") // FAILED 외에 모든 경우
                .to(nextConditionalStep2()) // nextConditionalStep2로 이동한다.
                .next(nextConditionalStep3()) // nextConditionalStep2가 정상 종료되면 nextConditionalStep3으로 이동한다.
                .on("*") // nextConditionalStep3의 결과 관계 없이
                .end() // nextConditionalStep3으로 이동하면 Flow가 종료한다.
            .end()// Job 종료
            .build()
    }

    @Bean
    fun nextConditionalStep1(): Step {
        return StepBuilder("nextConditionalStep1", jobRepository)
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
    fun nextConditionalStep2(): Step {
        return StepBuilder("nextConditionalStep2", jobRepository)
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
    fun nextConditionalStep3(): Step {
        return StepBuilder("nextConditionalStep3", jobRepository)
            .tasklet(
                { _, _ ->
                    log.info(">>>>> This is Step3")
                    RepeatStatus.FINISHED
                },
                transactionManager,
            )
            .build()
    }

    // Custom Listener
    class SkipCheckingListener : StepExecutionListener {
        override fun afterStep(stepExecution: StepExecution): ExitStatus? {
            val exitCode = stepExecution.exitStatus.exitCode
            return if (exitCode != ExitStatus.FAILED.exitCode &&
                stepExecution.skipCount > 0
            ) {
                ExitStatus("COMPLETED WITH SKIPS") // 커스텀 exitCode 반환
            } else {
                null
            }
        }
    }
}