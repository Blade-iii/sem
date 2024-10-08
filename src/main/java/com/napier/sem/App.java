package com.napier.sem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class App
{
    public static void main(String[] args)
    {
        // Create new Application
        App a = new App();

        // Connect to database
        a.connect();

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

        Employee empToUpdate = new Employee();
        empToUpdate.first_name = "John";
        empToUpdate.last_name = "Doe";
        empToUpdate.gender = "M";
        empToUpdate.hire_date = Date.valueOf("2020-01-01");
        empToUpdate.birth_date = Date.valueOf("1980-05-15");

        boolean updated = a.updateEmployee(500000, empToUpdate);

        if (updated) {
            System.out.println("Employee was updated successfully.");
        } else {
            System.out.println("Failed to update employee.");
        }

        // Get all salaries
        //a.displaySalaries(a.getAllSalaries());



        // Disconnect from database
        a.disconnect();
    }


    /**
     * Connection to MySQL database.
     */
    private Connection con = null;

    /**
     * Connect to the MySQL database.
     */
    public void connect() {
        try {
            // Load Database driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }

        int retries = 10;
        for (int i = 0; i < retries; ++i) {
            System.out.println("Connecting to database...");
            try {
                // Wait a bit for db to start
                Thread.sleep(30000);
                // Connect to database
                con = DriverManager.getConnection("jdbc:mysql://db:3306/employees?useSSL=false", "root", "example");
                System.out.println("Successfully connected");
                return; // Exit the method after successful connection
            } catch (SQLException sqle) {
                System.out.println("Failed to connect to database attempt " + (i + 1));
                System.out.println(sqle.getMessage());
            } catch (InterruptedException ie) {
                System.out.println("Thread interrupted? Should not happen.");
            }
        }

        // If we reach here, it means all connection attempts failed
        System.err.println("Failed to connect to the database after " + retries + " attempts.");
        System.exit(-1); // Exit or handle this situation as necessary
    }


    /**
     * Disconnect from the MySQL database.
     */
    public void disconnect()
    {
        if (con != null)
        {
            try
            {
                // Close connection
                con.close();
            }
            catch (Exception e)
            {
                System.out.println("Error closing connection to database");
            }
        }
    }
    public Employee getEmployee(int ID) {
        Statement stmt = null;
        ResultSet rset = null;
        try {
            stmt = con.createStatement();
            String strSelect = "SELECT emp_no, first_name, last_name FROM employees WHERE emp_no = " + ID;
            rset = stmt.executeQuery(strSelect);

            if (rset.next()) {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("emp_no");
                emp.first_name = rset.getString("first_name");
                emp.last_name = rset.getString("last_name");
                return emp;
            } else {
                return null; // Employee not found
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get employee details");
            return null;
        } finally {
            // Close ResultSet and Statement to prevent resource leaks
            try {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                System.out.println("Failed to close resources: " + e.getMessage());
            }
        }
    }


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
        String strSelect = "SELECT emp_no, first_name, last_name, gender, hire_date, birth_date FROM employees WHERE emp_no = ?";

        // SQL update statement
        String strUpdate = "UPDATE employees SET first_name = ?, last_name = ?, gender = ?, hire_date = ?, birth_date = ? WHERE emp_no = ?";

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

                // Output the old details
                System.out.println("Old Employee Details:");
                displayEmployee(oldEmp);
            } else {
                System.out.println("No employee found with ID: " + ID);
                return false;
            }

            // Now, perform the update with the new employee details
            PreparedStatement updateStmt = con.prepareStatement(strUpdate);
            updateStmt.setString(1, newEmp.first_name);  // Update first name
            updateStmt.setString(2, newEmp.last_name);   // Update last name
            updateStmt.setString(3, newEmp.gender);      // Update gender
            updateStmt.setDate(4, (Date) newEmp.hire_date);     // Update hire date
            updateStmt.setDate(5, (Date) newEmp.birth_date);    // Update birth date
            updateStmt.setInt(6, ID);                    // The employee ID

            // Execute the update query
            int rowsAffected = updateStmt.executeUpdate();

            if (rowsAffected > 0) {
                // Output the new details after update
                System.out.println("New Employee Details:");
                displayEmployee(newEmp); // Show the updated employee details
                return true;
            } else {
                System.out.println("Failed to update employee.");
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

    public void displayEmployee(Employee emp)
    {
        if (emp != null)
        {
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