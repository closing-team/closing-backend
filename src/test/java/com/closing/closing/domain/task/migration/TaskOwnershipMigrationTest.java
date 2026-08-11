package com.closing.closing.domain.task.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;

class TaskOwnershipMigrationTest {

    private static final String DATABASE_URL =
            "jdbc:h2:mem:task-ownership-migration;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";

    @Test
    void preservesExistingTaskAndAiSessionWhenOwnerChangesToUser() throws SQLException {
        migrateThroughVersionThree();
        insertExistingData();

        migrateThroughLatestVersion();

        try (Connection connection = DriverManager.getConnection(DATABASE_URL, "sa", "");
                Statement statement = connection.createStatement()) {
            assertEquals(1, queryLong(statement, "SELECT COUNT(*) FROM tasks"));
            assertEquals(101, queryLong(statement, "SELECT user_id FROM tasks WHERE task_id = 301"));
            assertEquals(1, queryLong(statement, "SELECT COUNT(*) FROM ai_sessions"));
            assertEquals(
                    "[301]",
                    queryString(
                            statement,
                            "SELECT confirmed_task_ids FROM ai_sessions WHERE session_id = 'session-1'"));
            assertEquals(
                    0,
                    queryLong(
                            statement,
                            "SELECT COUNT(*) FROM information_schema.columns "
                                    + "WHERE table_name = 'tasks' AND column_name = 'registration_id'"));

            statement.executeUpdate(
                    "INSERT INTO users (user_id, kakao_id, nickname) "
                            + "VALUES (102, 'kakao-102', '일반 사용자')");
            statement.executeUpdate(
                    "INSERT INTO tasks (task_id, user_id, title) "
                            + "VALUES (302, 102, '사업자 인증 없는 사용자의 일정')");

            assertEquals(2, queryLong(statement, "SELECT COUNT(*) FROM tasks"));
        }
    }

    private void migrateThroughVersionThree() {
        Flyway.configure()
                .dataSource(DATABASE_URL, "sa", "")
                .locations("classpath:db/migration")
                .target(MigrationVersion.fromVersion("3"))
                .load()
                .migrate();
    }

    private void migrateThroughLatestVersion() {
        Flyway.configure()
                .dataSource(DATABASE_URL, "sa", "")
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    private void insertExistingData() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "INSERT INTO users (user_id, kakao_id, nickname) "
                            + "VALUES (101, 'kakao-101', '기존 사용자')");
            statement.executeUpdate(
                    "INSERT INTO business_registrations "
                            + "(registration_id, user_id, business_number) "
                            + "VALUES (201, 101, '1234567890')");
            statement.executeUpdate(
                    "INSERT INTO tasks (task_id, registration_id, title) "
                            + "VALUES (301, 201, '기존 일정')");
            statement.executeUpdate(
                    "INSERT INTO ai_sessions "
                            + "(session_id, user_id, status, messages, turn_count, confirmed_task_ids) "
                            + "VALUES ('session-1', 101, 'ALREADY_CONFIRMED', '[]', 1, '[301]')");
        }
    }

    private long queryLong(Statement statement, String sql) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private String queryString(Statement statement, String sql) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getString(1);
        }
    }
}
