package db;

import office.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}