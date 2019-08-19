package de.hofmann.Passantenfrequenz.dataacess;

import java.sql.*;

public class DatabaseClientImpl implements IDatabaseClient {
    private Connection c;
    private Statement stmt;
    public DatabaseClientImpl() {
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/overlay",
                "postgres", "4444");
            stmt = c.createStatement();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    @Override
    public ResultSet getBySql(String SQL) throws SQLException {
        return stmt.executeQuery(SQL);
    }

    @Override
    public void writeBySql(String SQL) throws SQLException {
        stmt.executeUpdate(SQL);
    }
}
