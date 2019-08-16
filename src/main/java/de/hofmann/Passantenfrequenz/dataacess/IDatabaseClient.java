package de.hofmann.Passantenfrequenz.dataacess;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface IDatabaseClient {
    ResultSet getBySql(String SQL) throws SQLException;
    void writeBySql(String SQL) throws SQLException;
}
