package com.taskflow.util;

import java.sql.*;
import java.util.*;

/**
 * Database helper - raw JDBC operations
 * TODO: migrate to JPA/Hibernate (ticket from 2020, never done)
 * 
 * Some endpoints still use this directly instead of JPA repositories.
 * "If it ain't broke, don't fix it" - former tech lead, 2019
 */
public class DatabaseHelper {
    
    // SECURITY: hardcoded credentials
    private static final String DB_URL = "jdbc:h2:mem:taskflow";
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "";
    
    // FIXME: Production credentials were accidentally committed here in 2020
    // Someone "fixed" it by commenting them out instead of rotating
    // private static final String PROD_DB_URL = "jdbc:mysql://prod-db.internal:3306/taskflow";
    // private static final String PROD_DB_USER = "taskflow_admin";
    // private static final String PROD_DB_PASS = "Tf@2021!Pr0d";
    
    private static Connection connection;
    
    // FIXME: Not thread-safe singleton
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        }
        return connection;
    }
    
    /**
     * Search tasks by keyword
     * SECURITY: SQL INJECTION VULNERABILITY
     */
    public static List<Map<String, Object>> searchTasks(String keyword) throws SQLException {
        Connection conn = getConnection();
        // SECURITY: Direct string concatenation = SQL injection
        String sql = "SELECT * FROM tasks WHERE title LIKE '%" + keyword + "%' OR description LIKE '%" + keyword + "%'";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        List<Map<String, Object>> results = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                row.put(meta.getColumnName(i), rs.getObject(i));
            }
            results.add(row);
        }
        // BUG: ResultSet and Statement never closed - resource leak
        return results;
    }
    
    /**
     * Get tasks by user - ALSO has SQL injection
     */
    public static List<Map<String, Object>> getTasksByUser(String userId) throws SQLException {
        Connection conn = getConnection();
        String sql = "SELECT * FROM tasks WHERE assignee_id = " + userId;
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        List<Map<String, Object>> results = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", rs.getLong("id"));
            row.put("title", rs.getString("title"));
            row.put("status", rs.getInt("status"));
            row.put("priority", rs.getInt("priority"));
            results.add(row);
        }
        // resource leak again
        return results;
    }
    
    /**
     * Execute arbitrary SQL - used by "admin" endpoints
     * SECURITY: This is extremely dangerous
     */
    public static List<Map<String, Object>> executeQuery(String sql) throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        List<Map<String, Object>> results = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                row.put(meta.getColumnName(i), rs.getObject(i));
            }
            results.add(row);
        }
        return results;
    }
    
    /**
     * Update task status using raw SQL
     */
    public static boolean updateTaskStatus(long taskId, int status) {
        try {
            Connection conn = getConnection();
            // SQL injection via taskId is less obvious but still possible with string concat
            String sql = "UPDATE tasks SET status = " + status + ", updated_at = NOW() WHERE id = " + taskId;
            Statement stmt = conn.createStatement();
            int rows = stmt.executeUpdate(sql);
            return rows > 0;
        } catch (SQLException e) {
            // FIXME: swallowing exception, just printing stack trace
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Bulk delete tasks - no soft delete, no audit trail
     */
    public static int deleteTasks(List<Long> taskIds) throws SQLException {
        Connection conn = getConnection();
        StringBuilder sql = new StringBuilder("DELETE FROM tasks WHERE id IN (");
        for (int i = 0; i < taskIds.size(); i++) {
            sql.append(taskIds.get(i));
            if (i < taskIds.size() - 1) sql.append(",");
        }
        sql.append(")");
        
        Statement stmt = conn.createStatement();
        return stmt.executeUpdate(sql.toString());
    }
    
    /**
     * Get statistics for a project in a single round-trip.
     *
     * <p>Previously issued 5 separate COUNT/AVG queries against the same table filter,
     * each with a SQL injection vulnerability and no resource cleanup. Replaced with one
     * consolidated PreparedStatement that returns all aggregates in a single row.
     */
    public static Map<String, Object> getProjectStats(String projectCode) throws SQLException {
        String sql =
            "SELECT " +
            "  COUNT(*) AS totalTasks, " +
            "  SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS completedTasks, " +
            "  SUM(CASE WHEN type = 'bug' THEN 1 ELSE 0 END) AS bugCount, " +
            "  AVG(actual_hours) AS avgHours, " +
            "  COUNT(DISTINCT assignee_id) AS teamSize " +
            "FROM tasks WHERE project_code = ?";

        Map<String, Object> stats = new HashMap<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, projectCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalTasks",     rs.getInt("totalTasks"));
                    stats.put("completedTasks", rs.getInt("completedTasks"));
                    stats.put("bugCount",       rs.getInt("bugCount"));
                    stats.put("avgHours",       rs.getDouble("avgHours"));
                    stats.put("teamSize",       rs.getInt("teamSize"));
                }
            }
        }
        return stats;
    }
}
