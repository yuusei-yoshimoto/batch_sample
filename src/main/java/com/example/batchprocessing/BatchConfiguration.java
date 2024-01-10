package com.example.batchprocessing;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	public JobBuilderFactory jobBuilderFactory;

	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	public BatchConfiguration(JobBuilderFactory jobBuilderFactory
			, StepBuilderFactory stepBuilderFactory) {
		this.jobBuilderFactory = jobBuilderFactory;
		this.stepBuilderFactory = stepBuilderFactory;
	}

	@Bean
	public EmployeeItemProcessor processor() {
		return new EmployeeItemProcessor();
	}

	@Bean
	public FlatFileItemReader<Employee> reader() {
		return new FlatFileItemReaderBuilder<Employee>()
				.name("employeeItemReader")
				.resource(new ClassPathResource("employee.csv"))
				.delimited()
				.names(new String[] {"name", "department"})
				.fieldSetMapper(new BeanWrapperFieldSetMapper<Employee>() {{
					setTargetType(Employee.class);
				}})
				.build();
	}

	@Bean
	public JdbcBatchItemWriter<Employee> writer(DataSource dataSource) {
		return new JdbcBatchItemWriterBuilder<Employee>()
				.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
				.sql("INSERT INTO EMPLOYEES (ID, NAME, DEPARTMENT) VALUES (EMPLOYEE_ID_SEQ.nextval, :name, :department)")
				.dataSource(dataSource)
				.build();
	}

	@Bean
	public Step step1(JdbcBatchItemWriter<Employee> writer) {
		return stepBuilderFactory.get("step1")
				.<Employee, Employee> chunk(10)
				.reader(reader())
				.processor(processor())
				.writer(writer)
				.build();
	}
	@Bean
	public Job importEmployeeJob(Step step1) {
		return jobBuilderFactory.get("importEmployeeJob")
				.incrementer(new RunIdIncrementer())
				.flow(step1)
				.end()
				.build();
	}
}
