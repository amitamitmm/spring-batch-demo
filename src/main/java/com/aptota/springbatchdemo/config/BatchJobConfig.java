package com.aptota.springbatchdemo.config;

import com.aptota.springbatchdemo.model.Customer;
import com.aptota.springbatchdemo.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchJobConfig {

    private final CustomerRepository customerRepository;

    @Bean
    public FlatFileItemReader<Customer> itemReader(){
        FlatFileItemReaderBuilder<Customer> readerBuilder = new FlatFileItemReaderBuilder<>();
        return readerBuilder.name("customer-reader")
                .resource(new ClassPathResource("customer.csv"))
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names(new String[]{"id", "firstName", "lastName","gender", "email", "phone", "address", "city", "state", "country", "zipCode"})
                .strict(false)
                .targetType(Customer.class)
                .build();
    }

    @Bean
    public CustomerProcessor processor(){
        return new CustomerProcessor();
    }

    @Bean
    public RepositoryItemWriter<Customer> itemWriter(){
        RepositoryItemWriterBuilder<Customer> writerBuilder = new RepositoryItemWriterBuilder<>();
        return writerBuilder.repository(customerRepository)
              .methodName("save")
              .build();
    }

    @Bean
    public Step sampleStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("read-csv-file-step", jobRepository)
                .<Customer, Customer>chunk(10, transactionManager)
                .reader(itemReader())
                .writer(itemWriter())
                .taskExecutor(taskExecutor())
                .processor(processor())
                .build();
    }

    @Bean
    public Job sampleJob(JobRepository jobRepository, Step sampleStep) {
        return new JobBuilder("spring-batch-job", jobRepository)
                .start(sampleStep)
                .build();
    }


    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(15);
        taskExecutor.setMaxPoolSize(20);
        taskExecutor.setQueueCapacity(30);
        return taskExecutor;
    }






}
