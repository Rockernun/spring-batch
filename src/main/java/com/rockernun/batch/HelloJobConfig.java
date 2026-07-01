package com.rockernun.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class HelloJobConfig {

    @Bean
    // 하나의 Job은 JobRepository와 Step을 파라미터로 받음
    public Job helloJob(JobRepository jobRepository, Step helloStep) {
        return new JobBuilder("helloJob", jobRepository).start(helloStep).build();
    }

//    @Bean
//    public Step helloStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
//        return new StepBuilder("helloStep", jobRepository).tasklet((contribution, chunkContext) -> {
//            System.out.println("hello spring batch!");
//            return RepeatStatus.FINISHED;
//        }, transactionManager).build();
//    }

    @Bean
    public Step helloStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("helloStep", jobRepository).tasklet((contribution, chunkContext) -> {

            // 현재 Step의 Execution Context를 가져옴 -> 저장
            ExecutionContext stepContext = chunkContext.getStepContext()
                            .getStepExecution().getExecutionContext();
            stepContext.put("processedAt", "2026-07-01");
            stepContext.put("count", 100);

            System.out.println("Hello Spring Batch Execution!");
            return RepeatStatus.FINISHED;
        }, transactionManager).build();
    }
}
