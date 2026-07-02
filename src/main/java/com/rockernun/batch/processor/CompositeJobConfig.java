package com.rockernun.batch.processor;

import com.rockernun.common.Product;
import java.util.Arrays;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CompositeJobConfig {

    @Bean
    public Job compositeJob(JobRepository jobRepository, Step compositeStep) {
        return new JobBuilder("compositeJob", jobRepository)
                .start(compositeStep).build();
    }

    @Bean
    public Step compositeStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            DataSource dataSource) {
        return new StepBuilder("compositeStep", jobRepository)
                .<Product, Product>chunk(3, transactionManager)
                .reader(new JdbcCursorItemReaderBuilder<Product>()
                        .name("compositeReader")
                        .dataSource(dataSource)
                        .sql("SELECT ID, NAME, PRICE FROM PRODUCT ORDER BY ID")
                        .rowMapper((rs, rowNum) -> {
                            Product product = new Product();
                            product.setId(rs.getLong("ID"));
                            product.setName(rs.getString("NAME"));
                            product.setPrice(rs.getInt("PRICE"));
                            return product;
                        })
                        .build())
                .processor(compositeItemProcessor())
                .writer(chunk -> {
                    System.out.println("--- chunk " + chunk.getItems().size() + "건 처리되었습니다.");
                    chunk.getItems().forEach(product -> System.out.println("Writing: " + product));
                }).build();
    }

    @Bean
    public CompositeItemProcessor<Product, Product> compositeItemProcessor() {
        CompositeItemProcessor<Product, Product> compositeItemProcessor = new CompositeItemProcessor<>();
        compositeItemProcessor.setDelegates(Arrays.asList(
                new PriceValidator(),
                new TaxProcessor()
        ));
        return compositeItemProcessor;
    }

    /**
     * --- chunk 3건 처리되었습니다.
     * Writing: Product{id=1, name='사과', price=1650}
     * Writing: Product{id=2, name='바나나', price=880}
     * Writing: Product{id=3, name='오렌지', price=2200}
     * 필터링 되었습니다! 불량상품
     * --- chunk 2건 처리되었습니다.
     * Writing: Product{id=4, name='포도', price=3850}
     * Writing: Product{id=5, name='딸기', price=4400}
     */
}
