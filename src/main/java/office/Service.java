package office;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class Service {

    public static void createDB() {
        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            Statement stm = con.createStatement();
            stm.executeUpdate("DROP TABLE Department IF EXISTS");
            stm.executeUpdate("CREATE TABLE Department(ID INT PRIMARY KEY, NAME VARCHAR(255))");
            stm.executeUpdate("INSERT INTO Department VALUES(1,'Accounting')");
            stm.executeUpdate("INSERT INTO Department VALUES(2,'IT')");
            stm.executeUpdate("INSERT INTO Department VALUES(3,'HR')");

            stm.executeUpdate("DROP TABLE Employee IF EXISTS");
            stm.executeUpdate("CREATE TABLE Employee(ID INT PRIMARY KEY, NAME VARCHAR(255), DepartmentID INT)");
            stm.executeUpdate("INSERT INTO Employee VALUES(1,'Pete',1)");
            stm.executeUpdate("INSERT INTO Employee VALUES(2,'Ann',1)");

            stm.executeUpdate("INSERT INTO Employee VALUES(3,'Liz',2)");
            stm.executeUpdate("INSERT INTO Employee VALUES(4,'Tom',2)");

            stm.executeUpdate("INSERT INTO Employee VALUES(5,'Todd',3)");

        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public static void addDepartment(Department d) {
        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            PreparedStatement stm = con.prepareStatement("INSERT INTO Department VALUES(?,?)");
            stm.setInt(1, d.departmentID);
            stm.setString(2, d.getName());
            stm.executeUpdate();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void removeDepartment(Department d) {
        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            PreparedStatement removeEmployees = con.prepareStatement("DELETE FROM Employee WHERE DepartmentID = ?"); // Удалить сотрудников из отдела!!!
            removeEmployees.setInt(1, d.departmentID);
            removeEmployees.executeUpdate();

            PreparedStatement removeDepartment = con.prepareStatement("DELETE FROM Department WHERE ID = ?"); // Удаляем сам отдел
            removeDepartment.setInt(1, d.departmentID);
            removeDepartment.executeUpdate();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void addEmployee(Employee empl) {
        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            PreparedStatement stm = con.prepareStatement("INSERT INTO Employee VALUES(?,?,?)");
            stm.setInt(1, empl.getEmployeeId());
            stm.setString(2, empl.getName());
            stm.setInt(3, empl.getDepartmentId());
            stm.executeUpdate();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void removeEmployee(Employee empl) {
        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            PreparedStatement stm = con.prepareStatement("DELETE FROM Employee WHERE ID=?");
            stm.setInt(1, empl.getEmployeeId());
            stm.executeUpdate();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void performOperations() {
        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {

            // Ищем сотрудника Ann и обновляем его департамент
            PreparedStatement findAnn = con.prepareStatement("SELECT ID FROM Employee WHERE NAME = ?");
            findAnn.setString(1, "Ann");
            ResultSet rs = findAnn.executeQuery();
            if (rs.next()) {
                int annId = rs.getInt("ID");
                PreparedStatement updateAnnDept = con.prepareStatement("UPDATE Employee SET DepartmentID = ? WHERE ID = ?");
                updateAnnDept.setInt(1, 3); // HR
                updateAnnDept.setInt(2, annId);
                updateAnnDept.executeUpdate();
            }

            // Исправляем имена с маленькой буквы (переписываем с большой)
            PreparedStatement findIncorrectNames = con.prepareStatement("SELECT ID, NAME FROM Employee");
            ResultSet incorrectNames = findIncorrectNames.executeQuery();
            int correctedCount = 0;
            while (incorrectNames.next()) {
                int employeeId = incorrectNames.getInt("ID");
                String name = incorrectNames.getString("NAME");
                if (Character.isLowerCase(name.charAt(0))) {
                    String correctedName = name.substring(0, 1).toUpperCase() + name.substring(1);
                    PreparedStatement updateName = con.prepareStatement("UPDATE Employee SET NAME = ? WHERE ID = ?");
                    updateName.setString(1, correctedName);
                    updateName.setInt(2, employeeId);
                    updateName.executeUpdate();
                    correctedCount++;
                }
            }
            System.out.println("Количество исправленных имён: " + correctedCount);

            // Выводим количество сотрудников в IT отделе
            PreparedStatement countITEmployees = con.prepareStatement("SELECT COUNT(*) AS count FROM Employee WHERE DepartmentID = ?");
            countITEmployees.setInt(1, 2); // IT department ID
            ResultSet itCountResult = countITEmployees.executeQuery();
            if (itCountResult.next()) {
                int count = itCountResult.getInt("count");
                System.out.println("Количество сотрудников в IT: " + count);
            }

        } catch (SQLException e) {
            System.out.println("Ошибка при выполнении операций: " + e.getMessage());
        }
    }
}
