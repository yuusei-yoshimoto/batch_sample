package com.example.batchprocessing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class EmployeeItemProcessor implements ItemProcessor<Employee, Employee>{
	private static final Logger log = LoggerFactory.getLogger(EmployeeItemProcessor.class);

	@Override
	public Employee process(final Employee employee) throws Exception {
		final String name = employee.getName().toUpperCase();
		final Employee transformedEmployee = new Employee(name, employee.getDepartment());
		log.info("変換結果 (" + employee + ") => (" + transformedEmployee + ")");
		return transformedEmployee;
	}
}
