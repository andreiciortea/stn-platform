package ro.andreiciortea.stn.platform.repository.jena.tdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpStatus;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.JenaException;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.base.file.Location;

import io.vertx.core.Vertx;
import ro.andreiciortea.stn.platform.artifact.DigitalArtifactModelValidator;
import ro.andreiciortea.stn.platform.eventbus.RepositoryResponse;
import ro.andreiciortea.stn.platform.repository.Repository;


public class JenaTDBRepository implements Repository {
    
    private final String DATASET_MEM_LOCATION = "org.swot.hub.repository.jenatdb";
    
    private Dataset dataset;
    
//    private Vertx vertx;
    
    
    public JenaTDBRepository(Vertx vertx, boolean inMemory) {
//        this.vertx = vertx;
        
        if (inMemory) {
            dataset = TDBFactory.createDataset(Location.mem(DATASET_MEM_LOCATION));
            TDB.getContext().set(TDB.symUnionDefaultGraph, true);
        } else {
            dataset = TDBFactory.assembleDataset("store/tdb-assembler.ttl");
        }
    }
    
    public RepositoryResponse containsArtifact(String uri) {
        dataset.begin(ReadWrite.READ);
        boolean res = dataset.containsNamedModel(uri);
        dataset.end();
        
        return (res == true) ? new RepositoryResponse(RepositoryResponse.SC_OK, uri) 
                                : new RepositoryResponse(RepositoryResponse.SC_NOT_FOUND, uri);
    }
    
    private Model retrieveArtifact(String uri) {
        dataset.begin(ReadWrite.READ);
        Model model = dataset.getNamedModel(uri);
        dataset.end();
        
        return model;
    }
    
/*    @Override
    public void getArtifact(String uri, Handler<AsyncResult<DigitalArtifactDeprecated>> result) {
        result.handle(Future.succeededFuture(new DigitalArtifactDeprecated(uri, retrieveArtifact(uri))));
    }*/
    
    
    public RepositoryResponse getArtifact(String uri, String format) {
        Model model = retrieveArtifact(uri);
        
        if (model == null || model.isEmpty()) {
            return new RepositoryResponse(RepositoryResponse.SC_NOT_FOUND, uri);
        } else {
            StringWriter sw = new StringWriter();
            model.write(sw, format);
            
            return new RepositoryResponse(RepositoryResponse.SC_OK, uri, sw.toString());
        }
    }
    
    @Override
    public RepositoryResponse putArtifact(String artifactUri, String rdfData, String format) {
        try {
            dataset.begin(ReadWrite.WRITE);

            RepositoryResponse response;
            
            if (dataset.containsNamedModel(artifactUri)) {
                dataset.replaceNamedModel(artifactUri, createModel(rdfData, format));
                response = new RepositoryResponse(HttpStatus.SC_OK, artifactUri, rdfData);
            } else {
                dataset.addNamedModel(artifactUri, createModel(rdfData, format));
                response = new RepositoryResponse(HttpStatus.SC_CREATED, artifactUri, rdfData);
            }
            
            dataset.commit();
            dataset.end();
            
            return response;
        } catch (JenaException e) {
            return new RepositoryResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, artifactUri);
        }
    }
    
    @Override
    public RepositoryResponse deleteArtifact(String uri) {
        RepositoryResponse response = containsArtifact(uri);
        
        if (response.getStatusCode() == RepositoryResponse.SC_NOT_FOUND) {
            return new RepositoryResponse(RepositoryResponse.SC_NOT_FOUND, uri);
        } else {
            dataset.begin(ReadWrite.WRITE);
            dataset.removeNamedModel(uri);
            dataset.commit();
            dataset.end();
            
            return new RepositoryResponse(RepositoryResponse.SC_OK, uri);
        }
    }
    
    @Override
    public RepositoryResponse runSelectQuery(String queryString) {
        dataset.begin(ReadWrite.READ);
        
//        System.out.println("Query string: " + queryString);
        
//        long startTime = System.currentTimeMillis();
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, dataset)) {
            ResultSet queryResultSet = qexec.execSelect();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(out, queryResultSet);
            
//            System.out.println("Results from TDB: " + out.toString("UTF-8"));
            
//            long endTime = System.currentTimeMillis();
//            MockupAgentService.logQueryTime(endTime - startTime);
//            logTime(endTime - startTime);
            
            dataset.end();
            
            return new RepositoryResponse(RepositoryResponse.SC_OK, null, out.toString("UTF-8"));
        } catch(Exception e) {
//            System.out.println("Query failed: " + e.getMessage());
            
            dataset.end();
            return new RepositoryResponse(RepositoryResponse.SC_INTERNAL_ERROR, null, e.getMessage());
        }
    }
    
    @Override
    public RepositoryResponse runConstructQuery(String queryString, String format) {
        dataset.begin(ReadWrite.READ);
        
//        long startTime = System.currentTimeMillis();
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, dataset)) {
            Model model = qexec.execConstruct();
            
            String results = DigitalArtifactModelValidator.modelToString(model, format);
            
//            long endTime = System.currentTimeMillis();
//            MockupAgentService.logQueryTime(endTime - startTime);
//            logTime(endTime - startTime);
            
            dataset.end();
            return new RepositoryResponse(RepositoryResponse.SC_OK, null, results);
        } catch(Exception e) {
            dataset.end();
            return new RepositoryResponse(RepositoryResponse.SC_INTERNAL_ERROR, null, e.getMessage());
        }
    }
    
    private Model createModel(String data, String format) throws JenaException {
        InputStream stream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        
        return ModelFactory.createDefaultModel().read(stream, null, format);
    }

    
    
/*    private void logTime(long duration) {
        LocalMap<String, Long> map = vertx.sharedData().getLocalMap("benchmark");
        Long total = map.get("qt");
        
        if (total != null) {
            total += (duration);
            map.put("qt", total);
        }
    }*/
}
