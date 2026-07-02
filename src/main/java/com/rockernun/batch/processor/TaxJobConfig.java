package com.rockernun.batch.processor;

import com.rockernun.common.Product;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TaxJobConfig {

    @Bean
    public Job taxJob(JobRepository jobRepository, Step taxStep) {
        return new JobBuilder("taxJob", jobRepository)
                .start(taxStep).build();
    }

    @Bean
    public Step taxStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            DataSource dataSource) {
        return new StepBuilder("taxStep", jobRepository)
                .<Product, Product>chunk(3, transactionManager)
                .reader(new JdbcCursorItemReaderBuilder<Product>()
                        .name("taxReader")
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
                .processor(new TaxProcessor())  // Processor 끼워넣기
                .writer(chunk -> {
                    System.out.println("--- chunk " + chunk.getItems().size() + "건 처리되었습니다.");
                    chunk.getItems().forEach(product -> System.out.println("Writing: " + product));
                }).build();
    }

    /**
     * --- chunk 3건 처리되었습니다.
     * Writing: Product{id=1, name='사과', price=1650}
     * Writing: Product{id=2, name='바나나', price=880}
     * Writing: Product{id=3, name='오렌지', price=2200}
     * --- chunk 3건 처리되었습니다.
     * Writing: Product{id=4, name='포도', price=3850}
     * Writing: Product{id=5, name='딸기', price=4400}
     * Writing: Product{id=6, name='불량상품', price=0}
     */
}
