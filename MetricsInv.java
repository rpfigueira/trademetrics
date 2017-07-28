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
import java.sql.SQLException;
import java.sql.Statement;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.io.database.drivers.MySQLDriver;
import org.gephi.io.database.drivers.SQLUtils;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.plugin.database.EdgeListDatabaseImpl;
import org.gephi.io.importer.plugin.database.ImporterEdgeList;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.ClusteringCoefficient;
import org.gephi.statistics.plugin.ConnectedComponents;
import org.gephi.statistics.plugin.Degree;
import org.gephi.statistics.plugin.EigenvectorCentrality;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.statistics.plugin.Hits;
import org.gephi.statistics.plugin.Modularity;
import org.gephi.statistics.plugin.PageRank;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * This demo shows how to import data from a MySQL database. The database format
 * must be "Edge List", basically a table for nodes and a table for edges.
 * <p>
 * To be found by the importer, you need to have following columns:
 * <ul><li><b>Nodes:</b> ID and LABEL</li>
 * <li><b>Edges:</b> SOURCE, TARGET and WEIGHT</li></ul>
 * Any other column will be imported as attributes. Other recognized columns are
 * X, Y and SIZE for nodes and ID and LABEL for edges.
 * <p>
 * A possible toolkit use-case is a layout server. Therefore this demo layout
 * the network imported from the database, layout it and update X, Y columns to
 * the database.
 * 
 * @author Mathieu Bastian
 */
public class MetricsInv {

    public MetricsInv() {
    }

    public void script(String taxonID, String taxon, boolean printGraphs, String queryPeriodo, String tabela) {
        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get controllers and models
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();

        //Import database
        EdgeListDatabaseImpl db = new EdgeListDatabaseImpl();
        db.setDBName("birdcites");
        db.setHost("10.17.128.160");
        db.setUsername("rootSpec");
        db.setPasswd("tIrrAq739");
        db.setSQLDriver(new MySQLDriver());
        //db.setSQLDriver(new PostgreSQLDriver());
        //db.setSQLDriver(new SQLServerDriver());
        db.setPort(3306);
        db.setNodeQuery("SELECT concat(v.Nodes, '-',taxonid) as Nodes, concat(v.Nodes, '-',taxonid) AS Id, "
                + "v.Nodes AS Label, latitude, longitude, taxonid  "
                + "FROM vertices1Species_wild v WHERE taxonid = " + taxonID + " AND v.Nodes <> 'XX' "+queryPeriodo+" GROUP BY v.Nodes, taxonid");
        db.setEdgeQuery("SELECT concat(exporter,'-',taxonid) AS Target, concat(importer,'-',taxonid) AS Source, "
                + "'Directed' AS type, CONCAT(exporter,'-',importer) AS Label, quant AS weight, reexport, "
                + "log(quant+1)*10 AS logquant, quant, n_taxon, taxonid "
                + "FROM edgesSpecies_wild WHERE taxonid = " + taxonID + " AND exporter <> 'XX' "+queryPeriodo+" ORDER BY taxonid;");
        ImporterEdgeList edgeListImporter = new ImporterEdgeList();
        Container container = importController.importDatabase(db, edgeListImporter);
        container.setAllowAutoNode(false);      //Don't create missing nodes
        container.getLoader().setEdgeDefault(EdgeDefault.DIRECTED);   //Force DIRECTED

        //Append imported data to GraphAPI
        importController.process(container, new DefaultProcessor(), workspace);

        //See if graph is well imported
        DirectedGraph graph = graphModel.getDirectedGraph();
        System.out.println("Nodes: " + graph.getNodeCount());
        System.out.println("Edges: " + graph.getEdgeCount());

        if(printGraphs){
            PrintPreview prv = new PrintPreview();
            prv.script(container, taxonID+"_inv_"+taxon);
        }


        //Get Centrality, Network diameter in Gephi, eccentricity
        GraphDistance distance = new GraphDistance();
        distance.setDirected(true);
        distance.setNormalized(false);
        distance.execute(graphModel, attributeModel);

        //Get Degree, Average Degree
        Degree degree = new Degree();
        degree.execute(graphModel, attributeModel);
        
        

        //Get Modularity
        Modularity modularity = new Modularity();
        modularity.setRandom(true);
        modularity.execute(graphModel, attributeModel);

        //Get PageRank
        PageRank pagerank = new PageRank();
        pagerank.setDirected(true);
        pagerank.setProbability(0.85);
        pagerank.setEpsilon(0.001);
        pagerank.setUseEdgeWeight(true);
        pagerank.execute(graphModel, attributeModel);

        //Get Hits
        Hits hits = new Hits();
        hits.setEpsilon(0.0001);
        hits.execute(graphModel, attributeModel);

        //Get EigenvectorCentrality
        EigenvectorCentrality eigenvectorCentrality = new EigenvectorCentrality();
        eigenvectorCentrality.setDirected(true);
        eigenvectorCentrality.setNumRuns(1000);
        eigenvectorCentrality.execute(graphModel, attributeModel);

        //Get ClusteringCoefficient
        ClusteringCoefficient clusteringCoefficient = new ClusteringCoefficient();
        clusteringCoefficient.setDirected(true);
        clusteringCoefficient.execute(graphModel, attributeModel);

        //Get ConnectedComponents
        ConnectedComponents connectedComponents = new ConnectedComponents();
        connectedComponents.setDirected(true);
        connectedComponents.execute(graphModel, attributeModel);

        //Get columns created
        AttributeColumn[] col = attributeModel.getNodeTable().getColumns();

        //Iterate over values
        for (Node n : graph.getNodes()) {

            String id = n.getNodeData().getId();
//            Double centrality = (Double) n.getNodeData().getAttributes().getValue(col8[14].getIndex());
//            String data = (String) n.getNodeData().getAttributes().getValue(col8[10].getIndex());
            for (int i = 0; i < col.length; i++) {
                System.out.print(n.getNodeData().getAttributes().getValue(col[i].getId()) + ", ");
//                System.out.print(n.getNodeData().getAttributes().getValue(col[i].getIndex())+", ");                
            }
            System.out.print("\n");
        }


        //Export X, Y position to the DB
        //Connect database
        String url = SQLUtils.getUrl(db.getSQLDriver(), db.getHost(), db.getPort(), db.getDBName());
        Connection connection = null;
        try {
            //System.err.println("Try to connect at " + url);
            connection = db.getSQLDriver().getConnection(url, db.getUsername(), db.getPasswd());
//            System.err.println("Database connection established");
        } catch (SQLException ex) {
            if (connection != null) {
                try {
                    connection.close();
                    System.err.println("Database connection terminated");
                } catch (Exception e) { /* ignore close errors */ }
            }
            System.err.println("Failed to connect at " + url);
            ex.printStackTrace(System.err);
        }
        if (connection == null) {
            System.err.println("Failed to connect at " + url);
        }

        //Update
        int count = 0;
        for (Node node : graph.getNodes().toArray()) {
            String id = node.getNodeData().getId();
            String countryID = (String) node.getNodeData().getAttributes().getValue(col[1].getId());
            Double eccent = (Double) node.getNodeData().getAttributes().getValue(col[6].getId());
            Double closeCentral = (Double) node.getNodeData().getAttributes().getValue(col[7].getId());
            Double betCentral = (Double) node.getNodeData().getAttributes().getValue(col[8].getId());
            Integer inDegree = (Integer) node.getNodeData().getAttributes().getValue(col[9].getId());
            Integer outDegree = (Integer) node.getNodeData().getAttributes().getValue(col[10].getId());
            Integer degreeVal = (Integer) node.getNodeData().getAttributes().getValue(col[11].getId());
            Integer modular = (Integer) node.getNodeData().getAttributes().getValue(col[12].getId());
            Double pagerankVal = (Double) node.getNodeData().getAttributes().getValue(col[13].getId());
            Float authority = (Float) node.getNodeData().getAttributes().getValue(col[14].getId());
            Float hub = (Float) node.getNodeData().getAttributes().getValue(col[15].getId());
            Double eigenCentral = (Double) node.getNodeData().getAttributes().getValue(col[16].getId());
            Object clustCoef = node.getNodeData().getAttributes().getValue(col[17].getId());
            Integer componentID = (Integer) node.getNodeData().getAttributes().getValue(col[18].getId());
            Integer strongConnectID = (Integer) node.getNodeData().getAttributes().getValue(col[19].getId());



            String query = "INSERT INTO " + db.getDBName() + ".metricsInv"+tabela+"_wild "
                    + "(nodeID, countryID, taxonID, Eccent, CloseCentral, BetCentral, inDegree, outDegree, Degree, Modular, Pagerank, Authority, Hub, "
                    + "EigenCentral, ClustCoef, ComponentID, StrongConnectID) VALUES ('"
                    + id + "', '" + countryID + "', " + taxonID + ", " + eccent + ", " + closeCentral + ", " + betCentral + ", " + inDegree + ", " + outDegree + ", " + degreeVal + ", "
                    + modular + ", " + pagerankVal + ", " + authority + ", " + hub + ", " + eigenCentral + ", " + clustCoef + ", " + componentID + ", " + strongConnectID
                    + ");";
//            System.out.println(query);
            try {
                Statement s = connection.createStatement();
                count += s.executeUpdate(query);
                s.close();

            } catch (SQLException e) {
                System.err.println("Failed to update line node id = " + id + " " + e);
            }
        }
        System.err.println(count + " rows were updated");

        //Close connection
        if (connection != null) {
            try {
                connection.close();
                //System.err.println("Database connection terminated");
            } catch (Exception e) { /* ignore close errors */ }
        }

    }
}
