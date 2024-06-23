package com.ruthetum.batch.job

import com.ruthetum.batch.logger
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.job.flow.FlowExecutionStatus
import org.springframework.batch.core.job.flow.JobExecutionDecider
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.util.Random


@Configuration
class DeciderJobConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    private val log = logger()

    @Bean
    fun deciderJob(): Job {
        return JobBuilder("deciderJob", jobRepository)
            .start(deciderStartStep())
            .next(decider()) // 홀수 | 짝수 구분
            .from(decider()) // decider의 상태가
            .on("ODD") // ODD라면
            .to(oddStep()) // oddStep로 간다.
            .from(decider()) // decider의 상태가
            .on("EVEN") // ODD라면
            .to(evenStep()) // evenStep로 간다.
            .end() // builder 종료
            .build()
    }

    @Bean
    fun deciderStartStep(): Step {
        return StepBuilder("deciderStartStep", jobRepository)
            .tasklet(
                { _, _ ->
                    log.info(">>>>> Start Step")
                    RepeatStatus.FINISHED
                },
                transactionManager,
            )
            .build()
    }

    @Bean
    fun evenStep(): Step {
        return StepBuilder("evenStep", jobRepository)
            .tasklet(
                { _, _ ->
                    log.info(">>>>> 짝수입니다")
                    RepeatStatus.FINISHED
                },
                transactionManager,
            )
            .build()
    }

    @Bean
    fun oddStep(): Step {
        return StepBuilder("oddStep", jobRepository)
            .tasklet(
                { _, _ ->
                    log.info(">>>>> 홀수입니다")
                    RepeatStatus.FINISHED
                },
                transactionManager,
            )
            .build()
    }

    @Bean
    fun decider(): JobExecutionDecider {
        return OddDecider()
    }

    class OddDecider : JobExecutionDecider {
        private val log = logger()

        override fun decide(jobExecution: JobExecution, stepExecution: StepExecution?): FlowExecutionStatus {
            val pivot = Random().nextInt(50) + 1
            log.info("랜덤숫자: {}", pivot)

            return if (pivot % 2 == 0) {
                FlowExecutionStatus("EVEN")
            } else {
                FlowExecutionStatus("ODD")
            }
        }
    }
}