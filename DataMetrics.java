/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tradesps;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author rfigueira
 */
public class DataMetrics {
    private DBConnection mdbc;
    private Connection conn;
    private Statement stmt;

    public DataMetrics() {
        try {
            mdbc = new DBConnection();
            mdbc.init();
            conn = mdbc.getDBConnection();
            stmt = conn.createStatement();
        } catch (SQLException ex) {
            Logger.getLogger(DataMetrics.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String getData(int taxonID, String countryID){
         String data = "";

        String query = "SELECT CONCAT_WS(',',m.Eccent,mI.Eccent,m.CloseCentral,mI.CloseCentral,m.BetCentral,m.inDegree,m.outDegree,m.Degree,m.Modular,m.Pagerank,"
                + "m.Authority,m.Hub,m.EigenCentral,m.ClustCoef) "
                + "FROM metrics_wild m, metricsInv_wild mI WHERE m.taxonid = mI.taxonID AND m.countryID = mI.countryID AND "
                + "m.taxonid = " + taxonID 
                + " AND m.countryID LIKE '" + countryID+"'";
        
//        System.out.println(query);

        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(query);
            while (rs.next()){
                data = rs.getString(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataMetrics.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (data.length()==0)
            data = "0,0,0,0,0,0,0,0,0,0,0,0,0,0";
        
        return data;
    }
    
    public String getDataEU(int taxonID, String countryID){
        
        String data = "";

        String query = "SELECT CONCAT_WS(',',Eccent,EccentNorm,CloseCentral,BetCentral,inDegree,outDegree,Degree,Modular,Pagerank,"
                + "Authority,Hub,EigenCentral,ClustCoef) "
                + "FROM metricsEU "
                + "WHERE taxonid = " + taxonID 
                + " AND countryID LIKE '" + countryID+"'";
        
//        System.out.println(query);

        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(query);
            while (rs.next()){
                data = rs.getString(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataMetrics.class.getName()).log(Level.SEVERE, null, ex);
        }

        return data;
    }
    
    public void closeConnection(){
        try {
            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(DataMetrics.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
}
