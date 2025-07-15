package com.employees.PairOfEmployees.service;

import com.employees.PairOfEmployees.ReportType;
import com.employees.PairOfEmployees.exception.EmployeeServiceException;
import com.employees.PairOfEmployees.model.EmployeeProjectRecord;
import com.employees.PairOfEmployees.model.Team;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
            .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .appendOptional(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            .toFormatter();
    private static final String[] HEADERS = { "EmpID", "ProjectID", "DateFrom", "DateTo" };
    private static final String[] DOWNLOAD_HEADERS = { "Employee ID #1", "Employee ID #2", "Project ID", "Days worked" };

    public List<Team> findPartnership(MultipartFile csvFile, ReportType reportType) {
        List<EmployeeProjectRecord> csvRecords = parseCSV(csvFile);
        return getCommonProjectPairs(csvRecords, reportType);
    }

    public List<EmployeeProjectRecord> parseCSV(MultipartFile file) {
        logger.info("File parsing in progress...");
        List<EmployeeProjectRecord> records = new ArrayList<>();

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .get();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVParser csvParser = CSVParser.parse(reader, csvFormat);

            for (CSVRecord csvRecord : csvParser) {
                EmployeeProjectRecord record = new EmployeeProjectRecord();
                record.setEmployeeId(Long.parseLong(csvRecord.get("EmpID")));
                record.setProjectID(Long.parseLong(csvRecord.get("ProjectID")));
                record.setDateFrom(parseDate(csvRecord.get("DateFrom")));
                String dateTo = csvRecord.get("DateTo");
                record.setDateTo(dateTo.equalsIgnoreCase("NULL") ? LocalDate.now() : parseDate(dateTo));

                records.add(record);
            }
            logger.info("File is parsed and added to List");
            return records;
        } catch (IOException e) {
            logger.error("Failed to parse CSV: {}", e.getMessage(), e);
            throw new EmployeeServiceException("Failed to parse file", e);
        }
    }

    @Override
    public byte[] generateCSV(List<Team> partnerships) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(
                     new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8),
                     CSVFormat.DEFAULT.builder()
                             .setHeader(DOWNLOAD_HEADERS).get())) {
            for (Team team : partnerships) {
                csvPrinter.printRecord(
                        team.getEmployeeId1(),
                        team.getEmployeeId2(),
                        team.getProjectID(),
                        team.getDaysWorked()
                );
            }
            csvPrinter.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            logger.error("Failed to generate CSV for teams: {}", e.getMessage(), e);
            throw new EmployeeServiceException("Failed to generate CSV", e);
        }
    }

    private LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            logger.error("Failed to parse date: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Not valid date format: " + dateString);
        }
    }

    private List<Team> getCommonProjectPairs(List<EmployeeProjectRecord> records, ReportType reportType) {
        Team maxDayInPartnership = new Team(0L, 0L, 0L, 0L);
        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            for (int j = i + 1; j < records.size(); j++) {
                EmployeeProjectRecord employee1 = records.get(i);
                EmployeeProjectRecord employee2 = records.get(j);

                if (employee1.getProjectID().equals(employee2.getProjectID())) {

                    LocalDate startDate = employee1.getDateFrom().isAfter(employee2.getDateFrom()) ?
                            employee1.getDateFrom() : employee2.getDateFrom();
                    logger.info("Start Date {}", startDate);

                    LocalDate endDate = employee1.getDateTo().isBefore(employee2.getDateTo()) ?
                            employee1.getDateTo() : employee2.getDateTo();
                    logger.info("End Date {}", startDate);

                    if (startDate.isBefore(endDate)) {
                        long days = ChronoUnit.DAYS.between(startDate, endDate);
                        Team team = new Team(employee1.getEmployeeId(), employee2.getEmployeeId(), employee1.getProjectID(), days);

                        logger.info("Team: Employee 1 - " + employee1.getEmployeeId() +
                                ", Employee 2 - " + employee2.getEmployeeId() +
                                ", ProjectID - " + team.getProjectID() + ", Days - " + days);
                        if (days > maxDayInPartnership.getDaysWorked()) {
                            maxDayInPartnership = team;
                        }
                        teams.add(team);
                    }
                }
            }
        }

        if (reportType.equals(ReportType.ALL)) {
            return teams;
        } else if (reportType.equals(ReportType.MAX_DAYS)) {
            return Collections.singletonList(maxDayInPartnership);
        }
        return Collections.emptyList();
    }
}
