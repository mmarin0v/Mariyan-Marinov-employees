package com.employees.PairOfEmployees;

import com.employees.PairOfEmployees.model.Team;
import com.employees.PairOfEmployees.service.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmployeeServiceTest {
    @Autowired
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeServiceImpl();
    }

    @Test
    void findPartnership_returnMaxDaysInPartnership_whenInputIsValid() {
        String csvContent = """
            EmpID,ProjectID,DateFrom,DateTo
            1,100,2025-01-01,2025-01-25
            2,100,2025-01-01,2025-01-15
            3,100,2025-01-01,2025-01-30
            """;

        MultipartFile file = null;
        try {
            file = new MockMultipartFile(
                    "file",
                    "test.csv",
                    "text/csv",
                    new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Team> records = employeeService.findPartnership(file, ReportType.MAX_DAYS);

        assertEquals(records, getExpectedMaxDaysInPartnershipResults());
    }

    @Test
    void findPartnership_returnAllPartnership_whenInputIsValid() {
        String csvContent = """
            EmpID,ProjectID,DateFrom,DateTo
            1,100,2025-01-01,2025-01-25
            2,100,2025-01-01,2025-01-15
            3,100,2025-01-01,2025-01-30
            """;

        MultipartFile file = null;
        try {
            file = new MockMultipartFile(
                    "file",
                    "test.csv",
                    "text/csv",
                    new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Team> records = employeeService.findPartnership(file, ReportType.ALL);

        assertEquals(records, getExpectedAllPartnershipsResults());
    }

    private List<Team> getExpectedMaxDaysInPartnershipResults() {
        return List.of(new Team(1L, 3L, 100L, 24L));
    }

    private List<Team> getExpectedAllPartnershipsResults() {
        return List.of(
                new Team(1L, 2L, 100L, 14L),
                new Team(1L, 3L, 100L, 24L),
                new Team(2L, 3L, 100L, 14L));
    }
}
