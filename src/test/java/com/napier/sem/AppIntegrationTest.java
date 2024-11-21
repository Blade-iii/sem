package com.napier.sem;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Disabled;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class AppIntegrationTest {
    static App app;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        app = new App();
        app.connect("localhost:33060", 8080);
    }

    @Test
    void testGetEmployees() {
        String id = "";
        Employee emp = app.getEmployee(id);
        assertNotNull(emp);

    }

    @Test
    void testGetDepartments() {
        ArrayList<Employee> employees = app.getSalariesByDepartment("Development");
        assertEquals(employees.size(), 68392);
    }

    @Disabled
    @Test
    void testGetAllSalaries() {
        ArrayList<Employee> employees = app.getAllSalaries();
        assertEquals(employees.size(), 300024 );
        assertNotNull(employees);
    }

    @Test
    void testGetEmployeeManager() {
        ArrayList<Employee> manager = app.getEmployeeManager("Masako","Angiulli");
        assertEquals(manager.size(), 2);
        assertNotNull(manager);
        assertEquals(manager.get(0).first_name, "Masako");
        assertEquals(manager.get(0).last_name, "Angiulli");
        assertNotEquals(manager.isEmpty(), true);
    }
}
