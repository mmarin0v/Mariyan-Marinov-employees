package com.employees.PairOfEmployees.model;

import java.util.Objects;

public class Team {
    private Long employeeId1;
    private Long employeeId2;
    private Long projectID;
    private Long daysWorked;

    public Team(Long employeeId1, Long employeeId2, Long projectID, Long daysWorked) {
        this.employeeId1 = Math.min(employeeId1, employeeId2);
        this.employeeId2 = Math.max(employeeId1, employeeId2);
        this.projectID = projectID;
        this.daysWorked = daysWorked;
    }

    public Long getEmployeeId1() {
        return employeeId1;
    }

    public Long getEmployeeId2() {
        return employeeId2;
    }

    public Long getProjectID() {
        return projectID;
    }

    public Long getDaysWorked() {
        return daysWorked;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(employeeId1, team.employeeId1) && Objects.equals(employeeId2, team.employeeId2) &&
                Objects.equals(projectID, team.projectID) && Objects.equals(daysWorked, team.daysWorked);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId1, employeeId2, projectID, daysWorked);
    }
}
