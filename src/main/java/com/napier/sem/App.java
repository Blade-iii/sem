package com.napier.sem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class App {
    public static void main(String[] args) {
        // Create new Application
        App a = new App();
        // Connect to the database

        if (args.length < 1) {
            a.connect("localhost:33060", 10000);
        } else {
            a.connect(args[0], Integer.parseInt(args[1]));
        }
        SpringApplication.run(App.class, args);

//        ArrayList<Employee> employees = a.getSalariesByDepartment("Development");
//        a.printSalaries(employees);
//        ArrayList<Employee> emp = a.getSalariesByRole("Manager");
//        a.outputEmployees(emp,"ManagerSalaries.md");
//
//        // Disconnect from database
//        a.disconnect();

        /*
        // Get employee
        Employee emp = a.getEmployee(255530);
        // Display Results
        a.displayEmployee(emp);

        // Extract employee salary information
        ArrayList<Employee> employees = a.getAllSalaries();
        // Print the salaries
        a.printSalaries(employees);

         //Test the size of the returned data - should be 240124
        System.out.println(employees.size());

        a.displaySalariesByDepartment( a.getSalariesByDepartment("Sales"));
        a.displaySalariesByDepartment(a.getDepartment("Marketing"));
        a.displaySalariesByDepartment(a.getEmployeeManager("Masako","Angiulli"));
        */
    }

    /**
     * Connection to MySQL database.
     */
    private static Connection con = null;

    /**
     * Connect to the MySQL database.
     */
    public static void connect(String location, int delay) {
        try {
            // Load Database driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }

        int retries = 10;
        boolean shouldWait = false;
        for (int i = 0; i < retries; ++i) {
            System.out.println("Connecting to database...");
            try {
                if (shouldWait) {
                    // Wait a bit for db to start
                    Thread.sleep(delay);
                }

                // Connect to database
                con = DriverManager.getConnection("jdbc:mysql://" + location
                                + "/employees?allowPublicKeyRetrieval=true&useSSL=false",
                        "root", "example");
                System.out.println("Successfully connected");
                break;
            } catch (SQLException sqle) {
                System.out.println("Failed to connect to database attempt " + i);
                System.out.println(sqle.getMessage());

                // Let's wait before attempting to reconnect
                shouldWait = true;
            } catch (InterruptedException ie) {
                System.out.println("Thread interrupted? Should not happen.");
            }
        }
    }


    /**
     * Disconnect from the MySQL database.
     */
    public static void disconnect() {
        if (con != null) {
            try {
                // Close connection
                con.close();
            } catch (Exception e) {
                System.out.println("Error closing connection to database");
            }
        }
    }

    /**
     * Get a single employee record.
     * @param ID emp_no of the employee record to get.
     * @return The record of the employee with emp_no or null if no employee exists.
     */


    @RequestMapping("employee")
    public Employee getEmployee(@RequestParam(value = "id") String ID) {
        try {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect =
                    "SELECT emp_no, first_name, last_name "
                            + "FROM employees "
                            + "WHERE emp_no = " + ID;
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);
            // Return new employee if valid.
            // Check one is returned
            if (rset.next()) {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("emp_no");
                emp.first_name = rset.getString("first_name");
                emp.last_name = rset.getString("last_name");
                return emp;
            } else
                return null;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get employee details");
            return null;
        }
    }

    /**
     * @param firstName Firstname of the employee
     * @param lastName  Lastname of the employee
     * @return The employee query searching for the first and last name provided
     */
    public ArrayList<Employee> getEmployeeManager(String firstName, String lastName) {
        try {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect =
                    "SELECT employees.emp_no, employees.first_name, employees.last_name, salaries.salary, departments.dept_name " +
                            "FROM employees, salaries, dept_emp, departments " +
                            "WHERE employees.emp_no = salaries.emp_no " +
                            "AND employees.emp_no = dept_emp.emp_no " +
                            "AND dept_emp.dept_no = departments.dept_no " +
                            "AND salaries.to_date = '9999-01-01' " +
                            "AND first_name = '" + firstName + "' " +
                            "AND last_name = '" + lastName + "' " +
                            "ORDER BY employees.emp_no ASC";
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);
            // Extract department information
            ArrayList<Employee> salary = new ArrayList<Employee>();

            while (rset.next()) {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("employees.emp_no");
                emp.first_name = rset.getString("employees.first_name");
                emp.last_name = rset.getString("employees.last_name");
                emp.salary = rset.getInt("salaries.salary");
                emp.dept_name = rset.getString("dept_name");
                salary.add(emp);
            }
            return salary;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get EMPLOYEE details");
            return null;
        }
    }


    /**
     * Gets all the current employees and salaries.
     *
     * @return A list of all employees and salaries, or null if there is an error.
     */

    public ArrayList<Employee> getAllSalaries() {
        try {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect =
                    "SELECT employees.emp_no, employees.first_name, employees.last_name, salaries.salary "
                            + "FROM employees, salaries, dept_emp, departments "
                            + "WHERE employees.emp_no = salaries.emp_no AND salaries.to_date = '9999-01-01' "
                            + "ORDER BY employees.emp_no ASC "
                            + "LIMIT 10";

            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);
            // Extract employee information
            ArrayList<Employee> employees = new ArrayList<Employee>();
            while (rset.next()) {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("employees.emp_no");
                emp.first_name = rset.getString("employees.first_name");
                emp.last_name = rset.getString("employees.last_name");
                emp.salary = rset.getInt("salaries.salary");
                employees.add(emp);
            }
            return employees;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get salary details");
            return null;
        }
    }

    /**
     * @param dept The department name
     * @return The array list of department sql query
     */
    public ArrayList<Employee> getDepartment(String dept) {
        try {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect =
                    "SELECT *" +
                            "FROM employees, salaries, dept_emp, departments " +
                            "WHERE employees.emp_no = salaries.emp_no " +
                            "AND employees.emp_no = dept_emp.emp_no " +
                            "AND dept_emp.dept_no = departments.dept_no " +
                            "AND salaries.to_date = '9999-01-01' " +
                            "AND departments.dept_name = '" + dept + "' " +
                            "ORDER BY employees.emp_no ASC";
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);
            // Extract department information
            ArrayList<Employee> salary = new ArrayList<Employee>();

            while (rset.next()) {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("employees.emp_no");
                emp.first_name = rset.getString("employees.first_name");
                emp.last_name = rset.getString("employees.last_name");
                emp.salary = rset.getInt("salaries.salary");
                emp.dept_name = rset.getString("dept_name");
                salary.add(emp);
            }
            return salary;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get departments details");
            return null;
        }
    }


    /**
     * @param dept Department from the user
     * @return Salary by department
     */
    public ArrayList<Employee> getSalariesByDepartment(String dept) {
        try {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect =
                    "SELECT employees.emp_no, employees.first_name, employees.last_name, salaries.salary, departments.dept_name " +
                            "FROM employees, salaries, dept_emp, departments " +
                            "WHERE employees.emp_no = salaries.emp_no " +
                            "AND employees.emp_no = dept_emp.emp_no " +
                            "AND dept_emp.dept_no = departments.dept_no " +
                            "AND salaries.to_date = '9999-01-01' " +
                            "AND departments.dept_name = '" + dept + "' " +
                            "ORDER BY employees.emp_no ASC";
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);
            // Extract department information
            ArrayList<Employee> salary = new ArrayList<Employee>();

            while (rset.next()) {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("employees.emp_no");
                emp.first_name = rset.getString("employees.first_name");
                emp.last_name = rset.getString("employees.last_name");
                emp.salary = rset.getInt("salaries.salary");
                emp.dept_name = rset.getString("dept_name");
                salary.add(emp);
            }
            return salary;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get departments details");
            return null;
        }
    }

    /**
     * @param salary Arraylist of the sql statement
     */
    public void displaySalariesByDepartment(ArrayList<Employee> salary) {
        if (salary == null) {
            System.out.println("No salaries found");
            return;
        }
        for (Employee emp : salary) {
            System.out.println("Employee No: " + emp.emp_no + ", First Name: " + emp.first_name + ", Last Name: " + emp.last_name + ", Salary: " + emp.salary + ", Department Name: " + emp.dept_name);
        }
    }

    /*
    public Employee addEmployee(int ID, String firstName, String lastName, String gender, String hireDateStr, String birthDateStr) {
        String strInsert =
                "INSERT INTO employees (emp_no, first_name, last_name, gender,  hire_date, birth_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            // Create a PreparedStatement
            PreparedStatement pstmt = con.prepareStatement(strInsert);

            // Set parameters
            pstmt.setInt(1, ID); // emp_no
            pstmt.setString(2, firstName); // first_name
            pstmt.setString(3, lastName); // last_name
            pstmt.setString(4, gender); // gender


            // Convert String dates to java.sql.Date
            Date hireDate = Date.valueOf(hireDateStr); // Format: YYYY-MM-DD
            Date birthDate = Date.valueOf(birthDateStr); // Format: YYYY-MM-DD

            pstmt.setDate(5, hireDate); // hire_date
            pstmt.setDate(6, birthDate); // birth_date

            // Execute SQL statement
            pstmt.executeUpdate();
            System.out.println("Employee added successfully.");

            // Create and return the Employee object
            Employee emp = new Employee();
            emp.emp_no = ID;
            emp.first_name = firstName;
            emp.last_name = lastName;
            emp.gender = gender; // Don't forget to set the gender
            emp.hire_date = hireDate; // Set hire date in Employee object
            emp.birth_date = birthDate; // Set birth date in Employee object

            return emp; // Return the newly created employee
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to add employee");
            return null; // Return null in case of failure
        }
    }
    public boolean deleteEmployee(int ID) {
        String strDelete = "DELETE FROM employees WHERE emp_no = ?";
        try {
            // Use a PreparedStatement to prevent SQL injection
            PreparedStatement pstmt = con.prepareStatement(strDelete);
            pstmt.setInt(1, ID);  // Set the employee ID into the query

            // Execute the update query, which returns the number of affected rows
            int rowsAffected = pstmt.executeUpdate();

            // If one or more rows were affected, deletion was successful
            if (rowsAffected > 0) {
                System.out.println("Employee deleted successfully.");
                return true;
            } else {
                System.out.println("No employee found with ID: " + ID);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to delete employee.");
        }
        return false;  // Return false if the deletion failed
    }

    public boolean updateEmployee(int ID, Employee newEmp) {
        // SQL query to retrieve the old employee details
        String strSelect = "SELECT e.emp_no, e.first_name, e.last_name, e.gender, e.hire_date, e.birth_date, s.salary " +
                "FROM employees e " +
                "JOIN salaries s ON e.emp_no = s.emp_no " +
                "WHERE e.emp_no = ?";

        // SQL update statements
        String strUpdateEmployee = "UPDATE employees SET first_name = ?, last_name = ?, gender = ?, hire_date = ?, birth_date = ? WHERE emp_no = ?";
        String strUpdateSalary = "UPDATE salaries SET salary = ? WHERE emp_no = ?";

        Employee oldEmp = null; // To store old employee details

        try {
            // First, retrieve the old employee details
            PreparedStatement selectStmt = con.prepareStatement(strSelect);
            selectStmt.setInt(1, ID); // Set the employee ID
            ResultSet rset = selectStmt.executeQuery();

            if (rset.next()) {
                // Create an Employee object to hold old details
                oldEmp = new Employee();
                oldEmp.emp_no = rset.getInt("emp_no");
                oldEmp.first_name = rset.getString("first_name");
                oldEmp.last_name = rset.getString("last_name");
                oldEmp.gender = rset.getString("gender");
                oldEmp.hire_date = rset.getDate("hire_date");
                oldEmp.birth_date = rset.getDate("birth_date");
                oldEmp.salary = rset.getInt("salary"); // Get old salary as well

                // Output the old details
                System.out.println("Old Employee Details:");
                displayEmployee(oldEmp);
            } else {
                System.out.println("No employee found with ID: " + ID);
                return false;
            }

            // Now, perform the update with the new employee details
            PreparedStatement updateStmtEmp = con.prepareStatement(strUpdateEmployee);
            updateStmtEmp.setString(1, newEmp.first_name);  // Update first name
            updateStmtEmp.setString(2, newEmp.last_name);   // Update last name
            updateStmtEmp.setString(3, newEmp.gender);      // Update gender
            updateStmtEmp.setDate(4, (Date) newEmp.hire_date);     // Update hire date
            updateStmtEmp.setDate(5, (Date) newEmp.birth_date);    // Update birth date
            updateStmtEmp.setInt(6, ID);                    // Keep the employee ID unchanged

            // Execute the update for the employee
            int rowsAffectedEmp = updateStmtEmp.executeUpdate();

            // Now update the salary
            PreparedStatement updateStmtSalary = con.prepareStatement(strUpdateSalary);
            updateStmtSalary.setInt(1, newEmp.salary); // Set new salary
            updateStmtSalary.setInt(2, ID);            // Set the employee ID

            // Execute the update for the salary
            int rowsAffectedSalary = updateStmtSalary.executeUpdate();

            // Check if both updates were successful
            if (rowsAffectedEmp > 0 && rowsAffectedSalary > 0) {
                // Ensure the new employee retains the same emp_no (ID) and salary
                newEmp.emp_no = ID;   // Set the employee ID back to the newEmp object
                newEmp.salary = oldEmp.salary; // Ensure salary is updated correctly

                // Output the new details after update
                System.out.println("New Employee Details:");
                displayEmployee(newEmp); // Show the updated employee details
                return true;
            } else {
                System.out.println("Failed to update employee or salary.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to update employee.");
        }

        return false;  // Return false if the update failed
    }

    public List<Employee> getAllSalaries() {
        List<Employee> employees = new ArrayList<>();  // List to store employees
        try {
            Statement stmt = con.createStatement();
            int pageSize = 100; // Number of records to fetch at once
            int pageNumber = 0; // Start from the first page

            while (true) {
                String strSelect =
                        "SELECT employees.emp_no, employees.first_name, employees.last_name, salaries.salary " +
                                "FROM employees " +
                                "JOIN salaries ON employees.emp_no = salaries.emp_no " +
                                "ORDER BY salaries.salary DESC " +
                                "LIMIT " + pageSize + " OFFSET " + (pageNumber * pageSize);

                // Execute SQL statement
                ResultSet rset = stmt.executeQuery(strSelect);

                // Check if any data is returned
                if (!rset.isBeforeFirst()) {
                    break; // Exit the loop if no more records are found
                }

                // Loop through the ResultSet to retrieve all employees
                while (rset.next()) {
                    Employee emp = new Employee();
                    emp.emp_no = rset.getInt("emp_no");
                    emp.first_name = rset.getString("first_name");
                    emp.last_name = rset.getString("last_name");
                    emp.salary = rset.getInt("salary");
                    employees.add(emp); // Add employee to the list
                }

                pageNumber++; // Move to the next page
            }
            return employees; // Return the list of employees
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get employee details");
            return null;
        }
    }

    public void displaySalaries(List<Employee> salaries){
        if(salaries != null && !salaries.isEmpty()) {
            for(Employee emp : salaries) {
                System.out.println("ID: " + emp.emp_no + "First Name: " + emp.first_name + "Last Name: " + emp.last_name + "Salary: " + emp.salary);
            }
        }
        else {
            System.out.println("No salaries to display.");
        }

    }
}
  */
    public ArrayList<Employee> getSalariesByRole(String role) {
        ArrayList<Employee> employees = new ArrayList<>();
        String strSelect = "SELECT employees.emp_no, employees.first_name, employees.last_name,\n" +
                "titles.title, salaries.salary, departments.dept_name, dept_manager.emp_no\n" +
                "FROM employees, salaries, titles, departments, dept_emp, dept_manager\n" +
                "WHERE employees.emp_no = salaries.emp_no\n" +
                "  AND salaries.to_date = '9999-01-01'\n" +
                "  AND titles.emp_no = employees.emp_no\n" +
                "  AND titles.to_date = '9999-01-01'\n" +
                "  AND dept_emp.emp_no = employees.emp_no\n" +
                "  AND dept_emp.to_date = '9999-01-01'\n" +
                "  AND departments.dept_no = dept_emp.dept_no\n" +
                "  AND dept_manager.dept_no = dept_emp.dept_no\n" +
                "  AND dept_manager.to_date = '9999-01-01'\n" +
                "  AND titles.title = '" + role + "'";

        try (Statement stmt = con.createStatement();
             ResultSet rset = stmt.executeQuery(strSelect)) {

            // Retrieve all salaries
            while (rset.next()) {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("emp_no");
                emp.first_name = rset.getString("first_name");
                emp.last_name = rset.getString("last_name");
                emp.salary = rset.getInt("salary");
                emp.title = rset.getString("title");
                employees.add(emp);
            }
        } catch (SQLException e) {
            System.out.println("Failed to retrieve salaries by role: " + e.getMessage());
        }

        return employees;
    }
    public void displayEmployee(Employee emp) {
        if (emp != null) {
            System.out.println(
                    emp.emp_no + " "
                            + emp.first_name + " "
                            + emp.last_name + "\n"
                            + emp.title + "\n"
                            + "Salary:" + emp.salary + "\n"
                            + emp.dept_name + "\n"
                            + "Manager: " + emp.manager + "\n");
        }
    }

    /**
     * Prints a list of employees.
     *
     * @param employees The list of employees to print.
     */
    public void printSalaries(ArrayList<Employee> employees) {
        // Check employees is not null
        if (employees == null) {
            System.out.println("No employees");
            return;
        }
        // Print header
        System.out.println(String.format("%-10s %-15s %-20s %-8s", "Emp No", "First Name", "Last Name", "Salary"));
        // Loop over all employees in the list
        for (Employee emp : employees) {
            if (emp == null)
                continue;
            String emp_string =
                    String.format("%-10s %-15s %-20s %-8s",
                            emp.emp_no, emp.first_name, emp.last_name, emp.salary);
            System.out.println(emp_string);
        }
    }
    /**
     * Outputs to Markdown
     *
     * @param employees
     */
    public void outputEmployees(ArrayList<Employee> employees, String filename) {
        // Check employees is not null
        if (employees == null) {
            System.out.println("No employees");
            return;
        }

        StringBuilder sb = new StringBuilder();
        // Print header
        sb.append("| Emp No | First Name | Last Name | Title | Salary | Department |                    Manager |\r\n");
        sb.append("| --- | --- | --- | --- | --- | --- | --- |\r\n");
        // Loop over all employees in the list
        for (Employee emp : employees) {
            if (emp == null) continue;
            sb.append("| " + emp.emp_no + " | " +
                    emp.first_name + " | " + emp.last_name + " | " +
                    emp.title + " | " + emp.salary + " | "
                    + emp.dept_name + " | " + emp.manager + " |\r\n");
        }
        try {
            new File("./reports/").mkdir();
            BufferedWriter writer = new BufferedWriter(new FileWriter(new                                 File("./reports/" + filename)));
            writer.write(sb.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        /*
        a.addEmployee(500000,"Simon","Pegg","M","2024-12-19","1985-07-02");

        // Retrieve the employee with ID 10002 (change to a valid ID if necessary)
        Employee emp = a.getEmployee(500000);


        //a.deleteEmployee(500000);

        // Display the retrieved employee
        if (emp != null) {
            a.displayEmployee(emp); // This will display employee details
        } else {
            System.out.println("Employee not found.");
        }

        Employee updatedEmployee = new Employee();
        updatedEmployee.first_name = "John";
        updatedEmployee.last_name = "Doe";
        updatedEmployee.gender = "M";
        updatedEmployee.hire_date = Date.valueOf("2020-10-10");
        updatedEmployee.birth_date = Date.valueOf("1990-06-15");
        updatedEmployee.salary = 70000; // New salary

        boolean isUpdated = a.updateEmployee(10002, updatedEmployee);

        if (isUpdated) {
            System.out.println("Employee updated successfully.");
        } else {
            System.out.println("Failed to update employee.");
        }


        // Get all salaries
        //a.displaySalaries(a.getAllSalaries());

        // Get salary by role
        a.displaySalaries(a.getSalariesByRole());

*/
}
