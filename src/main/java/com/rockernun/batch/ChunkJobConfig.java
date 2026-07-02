package com.rockernun.batch;

import java.util.Arrays;
import java.util.List;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ChunkJobConfig {

    @Bean
    public Job chunkJob(JobRepository jobRepository, Step chunkStep) {
        return new JobBuilder("chunkJob", jobRepository)
                .start(chunkStep).build();
    }

    @Bean
    public Step chunkStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        List<String> items = Arrays.asList(
                "item1", "item2", "item3", "item4", "item5",
                "item6", "item7", "item8", "item9", "item10"
        );
        return new StepBuilder("chunkStep", jobRepository)
            .<String, String> chunk(3, transactionManager)
                .reader(new ListItemReader<>(items))
                .writer((chunk) -> {
                    System.out.println("-- chunk " + chunk.getItems().size() + "건 처리했습니다.");
                    chunk.getItems().forEach(item -> System.out.println(item + " 작업이 진행 중입니다."));
                }).build();
    }

    /**
     * -- chunk 3건 처리했습니다.
     * item1 작업이 진행 중입니다.
     * item2 작업이 진행 중입니다.
     * item3 작업이 진행 중입니다.
     * -- chunk 3건 처리했습니다.
     * item4 작업이 진행 중입니다.
     * item5 작업이 진행 중입니다.
     * item6 작업이 진행 중입니다.
     * -- chunk 3건 처리했습니다.
     * item7 작업이 진행 중입니다.
     * item8 작업이 진행 중입니다.
     * item9 작업이 진행 중입니다.
     * -- chunk 1건 처리했습니다.
     * item10 작업이 진행 중입니다.
     */
}
