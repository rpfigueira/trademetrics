/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tradesps;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

/**
 *
 * @author rfigueira
 */
public class DBConnection {

    private Connection specifyConnect;

    /**
     * Creates a new instance of DBConnection
     */
    public DBConnection() {
    }

    public void init() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            specifyConnect = DriverManager.getConnection(
                    "jdbc:mysql://<hostname>/<database>", "<username>", "<password>");   //add hostname, database name, username and password
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public Connection getDBConnection() {
        return specifyConnect;
    }

    public void close(ResultSet rs) {

        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
            }

        }
    }

    public void close(java.sql.Statement stmt) {

        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
            }
        }
    }

    public void destroy() {

        if (specifyConnect != null) {

            try {
                specifyConnect.close();
            } catch (Exception e) {
            }


        }
    }
}