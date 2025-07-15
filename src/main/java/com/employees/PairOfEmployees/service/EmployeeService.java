package com.employees.PairOfEmployees.service;

import com.employees.PairOfEmployees.ReportType;
import com.employees.PairOfEmployees.model.EmployeeProjectRecord;
import com.employees.PairOfEmployees.model.Team;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface EmployeeService {
    List<Team> findPartnership(MultipartFile csvFile, ReportType reportType);

    List<EmployeeProjectRecord> parseCSV(MultipartFile file);

    byte[] generateCSV(List<Team> partnerships);
}
