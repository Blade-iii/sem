package com.napier.sem;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
        Employee employee = app.getEmployee(255530);
        assertEquals(employee.emp_no, 255530);
        assertEquals(employee.first_name, "Ronghao");
        assertEquals(employee.last_name, "Garigliano");
    }
}
