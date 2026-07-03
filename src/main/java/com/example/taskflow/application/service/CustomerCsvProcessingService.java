package com.example.taskflow.application.service;

import com.example.taskflow.application.dto.CustomerCsvRecord;
import com.example.taskflow.application.dto.TaskProcessingResultDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CustomerCsvProcessingService {
    public TaskProcessingResultDto process(String filePath) {
        int totalRecords = 0;
        int successfulRecords = 0;
        int failedRecords = 0;
        List<String> errors = new ArrayList<>();

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                BOMInputStream.builder()
                                        .setPath(Path.of(filePath))
                                        .get(),
                                StandardCharsets.UTF_8
                        ));
                CSVParser csvParser = new CSVParser(
                        reader,
                        CSVFormat.DEFAULT
                                .builder()
                                .setHeader()
                                .setSkipHeaderRecord(true)
                                .build()
                )
        ) {
            log.info("Reading csv file");
            for (CSVRecord csvRecord : csvParser) {
                totalRecords++;

                try {
                    CustomerCsvRecord customer = mapRecord(csvRecord);
                    validateRecord(customer);
                    successfulRecords++;
                    log.info("Successfully parsed {} records", csvRecord.size());
                } catch (Exception ex) {
                    failedRecords++;
                    errors.add("Row " + totalRecords + ": " + ex.getMessage());
                    log.warn("Total failed records: {}", totalRecords);
                }
            }

        } catch (IOException ex) {
            log.error("Failed processing csv file = {}", filePath);
            throw new RuntimeException("Failed processing csv file: " + filePath, ex);
        }
        return new TaskProcessingResultDto(
                totalRecords,
                successfulRecords,
                failedRecords,
                errors
        );
    }

    private CustomerCsvRecord mapRecord(CSVRecord csvRecord) {
        return new CustomerCsvRecord(
                csvRecord.get("customerName"),
                csvRecord.get("email"),
                csvRecord.get("phone"),
                csvRecord.get("country")
        );
    }

    private void validateRecord(CustomerCsvRecord customer) {
        if (customer.customerName() == null || customer.customerName().isBlank()) {
            log.error("Customer name is required");
            throw new IllegalArgumentException("Customer name is required");
        }

        if (customer.email() == null
                || customer.email().isBlank()
                || !customer.email().contains("@")
        ) {
            log.debug("Invalid email address");
            throw new IllegalArgumentException("Invalid email address");
        }
    }
}
