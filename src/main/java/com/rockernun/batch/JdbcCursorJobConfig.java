package com.rockernun.batch;

import com.rockernun.common.Product;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class JdbcCursorJobConfig {

    @Bean
    public Job jdbcCursorJob(JobRepository jobRepository, Step jdbcCursorStep) {
        return new JobBuilder("jdbcCursorJob", jobRepository)
                .start(jdbcCursorStep).build();
    }

    @Bean
    public Step jdbcCursorStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcCursorItemReader<Product> jdbcCursorItemReader
    ) {
        return new StepBuilder("jdbcCursorStep", jobRepository)
                .<Product, Product> chunk(3, transactionManager)
                .reader(jdbcCursorItemReader)
                .writer((chunk) -> {
                    System.out.println("-- chunk " + chunk.getItems().size() + "건 처리했습니다.");
                    chunk.getItems().forEach(item -> System.out.println(item + " 작업이 진행 중입니다."));
                }).build();
    }

    @Bean
    public JdbcCursorItemReader<Product> jdbcCursorItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Product>()
                .name("jdbcCursorItemReader")
                .dataSource(dataSource)
                .sql("SELECT ID, NAME, PRICE FROM PRODUCT ORDER BY ID")
                .rowMapper((rs, rowNum) -> {
                    Product product = new Product();
                    product.setId(rs.getLong("ID"));
                    product.setName(rs.getString("NAME"));
                    product.setPrice(rs.getInt("PRICE"));
                    return product;
                }).build();
    }

    /**
     * -- chunk 3건 처리했습니다.
     * Product{id=1, name='사과', price=1500} 작업이 진행 중입니다.
     * Product{id=2, name='바나나', price=800} 작업이 진행 중입니다.
     * Product{id=3, name='오렌지', price=2000} 작업이 진행 중입니다.
     * -- chunk 2건 처리했습니다.
     * Product{id=4, name='포도', price=3500} 작업이 진행 중입니다.
     * Product{id=5, name='딸기', price=4000} 작업이 진행 중입니다.
     */
}
