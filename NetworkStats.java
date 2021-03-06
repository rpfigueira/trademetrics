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
  
  Adaptado por Rui Figueira
  Há um bug que origina uma excepção por limite do número de processos
  Resolve-se alterando no sistema operativo 'ulimit -u 4096'
  Usar 'ulimit -a' para lista das opções actuais
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

public class NetworkStats {
    
    private DBConnection mdbc;
    private Connection conn;
    private Statement stmt;
    ArrayList<String> taxonID;
    ArrayList<String> taxon;
    int anoBan = 0;
    

    public NetworkStats() {
        try {
            mdbc = new DBConnection();
            mdbc.init();
            conn = mdbc.getDBConnection();
            stmt = conn.createStatement();
        } catch (SQLException sQLException) {
        }
        taxonID = new ArrayList<String>();
        taxon = new ArrayList<String>();
        this.anoBan = anoBan; 
    }
    
    public void clearTable(String tabela){
        String query = "DELETE FROM metrics"+tabela+"_wild";
        try {
            stmt.executeUpdate(query);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
        query = "DELETE FROM metricsInv"+tabela+"_wild";
        try {
            stmt.executeUpdate(query);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
        
    }
    
    public void getTaxonID(String queryPeriodo){
        String query = "SELECT taxonID, taxon FROM ind_year_quant_paleo1_wild WHERE importer <> 'XX' "+queryPeriodo+" GROUP BY taxonID order by taxonID asc;";
        
        ResultSet rs = null;
        
        try {
            rs = stmt.executeQuery(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Erro em NetworkStats.getList:\n" + ex);
        }

        try {
            
            while (rs.next()) {
                taxonID.add(rs.getString(1));
                taxon.add(rs.getString(2));
            }

        } catch (SQLException ex) {
            Logger.getLogger(NetworkStats.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String normalizeEccent(String value, String tabela) {
        int min = 0;
        min = getMin(value, tabela);
        int max = 0;
        max = getMax(value, tabela);
        String query = "UPDATE metrics"+tabela+"_wild SET EccentNorm = (Eccent-"+min+")/"+max+" WHERE taxonID = '"+value+"';";

        try {
            stmt.executeUpdate(query);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        } 
        return query;

    }
    
    public void run(boolean printGraphs, String query, String tabela){
        for (int i = 0; i < taxonID.size(); i++){
//         for (int i = 0; i < 1; i++){
            String value = taxonID.get(i);
            String vtaxon = taxon.get(i);
            System.out.println(value);
            Metrics metrics = new Metrics();
            metrics.script(value, vtaxon, printGraphs, query, tabela);
            MetricsInv metricsInvert = new MetricsInv();
            metricsInvert.script(value, vtaxon, printGraphs, query, tabela);
            normalizeEccent(value, tabela);

        }
                
    }

    

    private int getMin(String value, String tabela) {
        int min = 0;
        String query = "SELECT min(eccent) FROM metrics"+tabela+"_wild WHERE taxonid = "+value;
        
        ResultSet rs = null;
        
        try {
            rs = stmt.executeQuery(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Erro em NetworkStats.getMax:\n" + ex);
        }

        try {
            
            while (rs.next()) {
                min = rs.getInt(1);   
            }

        } catch (SQLException ex) {
            Logger.getLogger(NetworkStats.class.getName()).log(Level.SEVERE, null, ex);
        }
        return min;
    }
    
    private int getMax(String value, String tabela) {
        int max = 0;
        String query = "SELECT max(eccent) FROM metrics"+tabela+"_wild WHERE taxonid = "+value;
        
        ResultSet rs = null;
        
        try {
            rs = stmt.executeQuery(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Erro em NetworkStats.getMax:\n" + ex);
        }

        try {
            
            while (rs.next()) {
                max = rs.getInt(1);   
            }

        } catch (SQLException ex) {
            Logger.getLogger(NetworkStats.class.getName()).log(Level.SEVERE, null, ex);
        }
        return max;
    }

    public void closeConnection(){
        try {
            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(NetworkStats.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
