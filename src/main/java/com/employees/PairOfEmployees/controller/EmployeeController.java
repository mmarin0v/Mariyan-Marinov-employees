package com.employees.PairOfEmployees.controller;

import com.employees.PairOfEmployees.ReportType;
import com.employees.PairOfEmployees.exception.EmployeeServiceException;
import com.employees.PairOfEmployees.model.Team;
import com.employees.PairOfEmployees.service.EmployeeService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/file")
public class EmployeeController {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/uploadCSV")
    public String showUploadForm() {
        return "index";
    }

    @PostMapping("/uploadCSV/{reportType}")
        public String uploadCsv(@RequestParam("file") MultipartFile file,
                                @PathVariable(value = "reportType") String reportType,
                                Model model,
                                HttpSession session) {
            return processUpload(file, model, ReportType.valueOf(reportType.toUpperCase()), session);
    }

    @GetMapping("/downloadCSV")
    public ResponseEntity<byte[]> downloadCSV(HttpSession session) {
        List<Team> partnerships = (List<Team>) session.getAttribute("resultsData");
        if (partnerships == null || partnerships.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        try {
            byte[] results = employeeService.generateCSV(partnerships);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
            return ResponseEntity.ok().headers(headers).body(results);
        } catch (EmployeeServiceException e) {
            logger.error("Error generating CSV: {}",   e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String processUpload(MultipartFile file, Model model, ReportType reportType, HttpSession session) {
        try {
            if (file.isEmpty()) {
                logger.info("Uploaded file is empty");
                model.addAttribute("error", "Uploaded file is empty");
                return "index";
            }

            if (!reportType.equals(ReportType.MAX_DAYS) && !reportType.equals(ReportType.ALL)) {
                logger.info("Invalid report type (Supported report types are ALL and MAX_DAYS)");
                model.addAttribute("error", "Invalid report type: " + reportType);
                return "index";
            }

            List<Team> partnerships = employeeService.findPartnership(file, reportType);
            if (partnerships != null && ! partnerships.isEmpty()) {
                model.addAttribute("partnerships", partnerships);
                session.setAttribute("resultsData", partnerships);
            } else {
                model.addAttribute("error", "No common projects found!");
            }
        } catch (EmployeeServiceException e) {
            logger.error("Error processing file: {}", e.getMessage(), e);
            model.addAttribute("error", "Error processing file: " + e.getMessage());
        }
        return "index";
    }
}