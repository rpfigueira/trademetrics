/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Adaptado por Rui Figueira
*/
package tradesps;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

public class NetworkStatsEU {
    
    private DBConnection mdbc;
    private Connection conn;
    private Statement stmt;
    ArrayList<String> taxonID;
    ArrayList<String> taxon;

    public NetworkStatsEU() {
        try {
            mdbc = new DBConnection();
            mdbc.init();
            conn = mdbc.getDBConnection();
            stmt = conn.createStatement();
        } catch (SQLException sQLException) {
        }
        taxonID = new ArrayList<String>();
        taxon = new ArrayList<String>();
    }
    
    public void clearTable(){
         String query = "DELETE FROM metricsEU";
        try {
            stmt.executeUpdate(query);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
        
    public void getTaxonID(){
        String query = "SELECT taxonID FROM ind_year_quant_paleo1 i "
                + "WHERE i.importer IN ('BE','BG','CZ','DK','DE','EE','IE','GR','ES','FR','IT','CY','LV','LT','LU','HU','MT','NL','AT','PL','PT','RO','SI','SK','FI','SE','GB') "
                + "GROUP BY taxonid;";
        
        ResultSet rs = null;
        
        try {
            rs = stmt.executeQuery(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Erro em PairSpecies.getList:\n" + ex);
        }

        try {
            
            while (rs.next()) {
                taxonID.add(rs.getString(1));
                taxon.add(rs.getString(2));
            }

        } catch (SQLException ex) {
            Logger.getLogger(NetworkStatsEU.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String normalizeEccent(String value) {
        int min = getMin(value);
        int max = getMax(value);
        String query = "UPDATE metricsEU SET EccentNorm = (Eccent-"+min+")/"+max+" WHERE taxonID = '"+value+"';";

        try {
            stmt.executeUpdate(query);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        } 
        return query;

    }
    
    public void run(){
        for (int i = 0; i < taxonID.size(); i++){
            String value = taxonID.get(i);
            String vtaxon = taxon.get(i);
            System.out.println(value);
            MetricsEU metricsEU = new MetricsEU();
            metricsEU.script(value, vtaxon);
            normalizeEccent(value);
        }
                
    }
    
    private int getMin(String value) {
        int min = 0;
        String query = "SELECT min(eccent) FROM metricsEU WHERE taxonid = "+value;
        
        ResultSet rs = null;
        
        try {
            rs = stmt.executeQuery(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Erro em NetworkStatsEU.getMin:\n" + ex);
        }

        try {
            
            while (rs.next()) {
                min = rs.getInt(1);   
            }

        } catch (SQLException ex) {
            Logger.getLogger(NetworkStatsEU.class.getName()).log(Level.SEVERE, null, ex);
        }
        return min;
    }
    
    private int getMax(String value) {
        int max = 0;
        String query = "SELECT max(eccent) FROM metricsEU WHERE taxonid = "+value;
        
        ResultSet rs = null;
        
        try {
            rs = stmt.executeQuery(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Erro em NetworkStatsEU.getMax:\n" + ex);
        }

        try {
            
            while (rs.next()) {
                max = rs.getInt(1);   
            }

        } catch (SQLException ex) {
            Logger.getLogger(NetworkStatsEU.class.getName()).log(Level.SEVERE, null, ex);
        }
        return max;
    }    

    public void closeConnection(){
        try {
            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(NetworkStatsEU.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
