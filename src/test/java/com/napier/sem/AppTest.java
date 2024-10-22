package com.napier.sem;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest
{
    static App app;

    @BeforeAll
    static void init()
    {
        app = new App();
    }

    @Test
    void printSalariesTestNull()
    {
        app.printSalaries(null);
    }

    @Test
    void printSalriesTestEmpty(){
        ArrayList<Employee> employees = new ArrayList<Employee>();
        app.printSalaries(employees);
    }

    @Test
    void printSalriesTestContainsNull(){
        ArrayList<Employee> employees = new ArrayList<Employee>();
        employees.add(null);
        app.printSalaries(employees);
    }

    @Test
    void printSalaries(){
        ArrayList<Employee> employees = new ArrayList<Employee>();
        Employee emp = new Employee();
        emp.emp_no= 1;
        emp.first_name= "John";
        emp.last_name= "Smith";
        emp.title="Engineering";
        emp.salary=55000;
        employees.add(emp);
        app.printSalaries(employees);
    }

    @Test
    void displayEmployeeTestNull(){
        app.displayEmployee(null);
    }

    @Test
    void displayEmployeeTestEmpty(){
        Employee emp = new Employee();
        app.displayEmployee(emp);
    }

    @Test
    void displayEmployeeContainsNull(){
        app.displayEmployee(null);
    }

    @Test
    void displayEmployee(){
        Employee emp = new Employee();
        emp.emp_no= 1;
        emp.first_name= "Bob";
        emp.last_name= "Ryan";
        emp.title="Postman";
        emp.salary=55000;

        app.displayEmployee(emp);
    }



}