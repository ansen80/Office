package db;

import office.Department;
import office.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestOffice {

    @BeforeEach
    void setup() {
        Service.createDB(); // Пересоздаём базу данных перед каждым тестом
    }

    @Test
    void testPerformOperations() throws SQLException {
        Service.performOperations();

        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {

            // Ann переведена в HR
            PreparedStatement checkAnn = con.prepareStatement("SELECT DepartmentID FROM Employee WHERE NAME = ?");
            checkAnn.setString(1, "Ann");
            ResultSet annResult = checkAnn.executeQuery();
            if (annResult.next()) {
                int departmentId = annResult.getInt("DepartmentID");
                assertEquals(3, departmentId, "Ann должна быть в департаменте HR");
            }

            // Имена исправлены
            PreparedStatement checkNames = con.prepareStatement("SELECT NAME FROM Employee");
            ResultSet namesResult = checkNames.executeQuery();
            while (namesResult.next()) {
                String name = namesResult.getString("NAME");
                assertEquals(Character.toUpperCase(name.charAt(0)), name.charAt(0), "Имена должны начинаться с заглавной буквы");
            }

            // Количество сотрудников в IT отделе
            PreparedStatement countIT = con.prepareStatement("SELECT COUNT(*) AS count FROM Employee WHERE DepartmentID = ?");
            countIT.setInt(1, 2); // IT department ID
            ResultSet itResult = countIT.executeQuery();
            if (itResult.next()) {
                int count = itResult.getInt("count");
                assertEquals(2, count, "Должно быть 2 сотрудника в IT");
            }


        }
    }

    @Test
    void testDeletionEmployees() throws SQLException {
        Service.removeDepartment(new Department(2, "IT")); // Удаляем отдел it

        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {  // Cотрудников больше нет
            PreparedStatement checkEmployees = con.prepareStatement("SELECT COUNT(*) AS count FROM Employee WHERE DepartmentID = ?");
            checkEmployees.setInt(1, 2);
            ResultSet resultSet = checkEmployees.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt("count");
                assertEquals(0, count, "Все сотрудники из удалённого отдела должны быть удалены.");
            }
        }
    }

    /* Дополнительные проверки */

    @Test
    void testAnnDepartmentUpdate() throws SQLException {
        Service.performOperations();
        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            PreparedStatement checkAnn = con.prepareStatement(
                    "SELECT DepartmentID FROM Employee WHERE NAME = ?"
            );
            checkAnn.setString(1, "Ann");
            ResultSet rs = checkAnn.executeQuery();
            assertTrue(rs.next(), "Сотрудник Ann должен существовать.");
            assertEquals(3, rs.getInt("DepartmentID"), "Ann должна быть переведена в департамент HR.");
        }
    }

    @Test
    void testCorrectNames() throws SQLException {
        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            PreparedStatement insertEmployee = con.prepareStatement(
                    "INSERT INTO Employee VALUES(6, 'john', 1)"
            );
            insertEmployee.executeUpdate();
        }
        Service.performOperations();
        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            PreparedStatement checkName = con.prepareStatement(
                    "SELECT NAME FROM Employee WHERE ID = ?"
            );
            checkName.setInt(1, 6);
            ResultSet rs = checkName.executeQuery();
            assertTrue(rs.next(), "Сотрудник с ID = 6 должен существовать.");
            assertEquals("John", rs.getString("NAME"), "Имя должно начинаться с заглавной буквы.");
        }
    }

    @Test
    void testCountEmployeesInIT() throws SQLException {
        Service.performOperations();
        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            PreparedStatement countIT = con.prepareStatement(
                    "SELECT COUNT(*) AS count FROM Employee WHERE DepartmentID = ?"
            );
            countIT.setInt(1, 2); // IT department ID
            ResultSet rs = countIT.executeQuery();
            assertTrue(rs.next(), "Запрос должен вернуть результат.");
            assertEquals(2, rs.getInt("count"), "В IT-отделе должно быть 2 сотрудника.");
        }
    }
}