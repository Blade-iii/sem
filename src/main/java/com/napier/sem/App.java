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

        // Get all salaries
        a.displaySalaries(a.getAllSalaries());



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
                Thread.sleep(1000);
                // Connect to database (update URL as necessary)
                con = DriverManager.getConnection("jdbc:mysql://localhost:3306/employees?useSSL=false", "root", "example");
                System.out.println("Successfully connected");
                break;
            } catch (SQLException sqle) {
                System.out.println("Failed to connect to database attempt " + Integer.toString(i));
                System.out.println(sqle.getMessage());
                sqle.printStackTrace(); // Print detailed error information
            } catch (InterruptedException ie) {
                System.out.println("Thread interrupted? Should not happen.");
            }
        }
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
//    public Employee getEmployee(int ID)
//    {
//        try
//        {
//            // Create an SQL statement
//            Statement stmt = con.createStatement();
//            // Create string for SQL statement
//            String strSelect =
//                    "SELECT emp_no, first_name, last_name "
//                            + "FROM employees "
//                            + "WHERE emp_no = " + ID;
//            // Execute SQL statement
//            ResultSet rset = stmt.executeQuery(strSelect);
//            // Return new employee if valid.
//            // Check one is returned
//            if (rset.next())
//            {
//                Employee emp = new Employee();
//                emp.emp_no = rset.getInt("emp_no");
//                emp.first_name = rset.getString("first_name");
//                emp.last_name = rset.getString("last_name");
//                return emp;
//            }
//            else
//                return null;
//        }
//        catch (Exception e)
//        {
//            System.out.println(e.getMessage());
//            System.out.println("Failed to get employee details");
//            return null;
//        }
//    }



    public Employee addEmployee(int ID, String firstName, String lastName, String title, int salary, String department, String manager) {
        String strInsert =
                "INSERT INTO employees (emp_no, first_name, last_name, title, salary, dept_name, manager) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            // Create a PreparedStatement
            PreparedStatement pstmt = con.prepareStatement(strInsert);

            // Set parameters
            pstmt.setInt(1, ID); // emp_no
            pstmt.setString(2, firstName); // first_name
            pstmt.setString(3, lastName); // last_name
            pstmt.setString(4, title); // title
            pstmt.setInt(5, salary); // salary
            pstmt.setString(6, department); // dept_name
            pstmt.setString(7, manager); // manager

            // Execute SQL statement
            pstmt.executeUpdate();
            System.out.println("Employee added successfully.");

            // Create and return the Employee object
            Employee emp = new Employee();
            emp.emp_no = ID;
            emp.first_name = firstName;
            emp.last_name = lastName;
            emp.title = title;
            emp.salary = salary;
            emp.dept_name = department;
            emp.manager = manager;

            return emp; // Return the newly created employee
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to add employee");
            return null; // Return null in case of failure
        }
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