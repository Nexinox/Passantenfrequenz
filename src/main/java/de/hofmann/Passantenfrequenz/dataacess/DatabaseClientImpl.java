package de.hofmann.Passantenfrequenz.dataacess;

import java.io.*;
import java.sql.*;

public class DatabaseClientImpl implements IDatabaseClient {
    private Statement stmt;
    private String pw, user;
    public DatabaseClientImpl() {
        try {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(
                    "rsc/databaseConfig.txt"));
                String line = reader.readLine();
                int i = 0;
                while (line != null) {
                    if(i == 0){
                        user = line;
                    }else if(i == 1){
                        pw = line;
                    }
                    line = reader.readLine();
                    i++;
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Class.forName("org.postgresql.Driver");
            Connection c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/overlay",
                user, pw);
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
