package com.rockernun.batch;

import com.rockernun.common.Product;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class JdbcPagingJobConfig {

    @Bean
    public Job jdbcPagingJob(JobRepository jobRepository, Step jdbcPagingStep) {
        return new JobBuilder("jdbcPagingJob", jobRepository)
                .start(jdbcPagingStep).build();
    }

    @Bean
    public Step jdbcPagingStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcPagingItemReader<Product> jdbcPagingItemReader
    ) {
        return new StepBuilder("jdbcPagingStep", jobRepository)
                .<Product, Product> chunk(3, transactionManager)
                .reader(jdbcPagingItemReader)
                .writer((chunk) -> {
                    System.out.println("-- chunk " + chunk.getItems().size() + "건 처리했습니다.");
                    chunk.getItems().forEach(item -> System.out.println(item + " 작업이 진행 중입니다."));
                }).build();
    }

    @Bean
    public JdbcPagingItemReader<Product> jdbcPagingItemReader(DataSource dataSource) throws Exception {
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("ID, NAME, PRICE");
        queryProvider.setFromClause("FROM PRODUCT");
        queryProvider.setSortKeys(Map.of("ID", Order.ASCENDING));

        return new JdbcPagingItemReaderBuilder<Product>()
                .name("jdbcPagingItemReader")
                .dataSource(dataSource)
                .queryProvider(queryProvider.getObject())
                .pageSize(3)
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
