package ro.andreiciortea.stn.platform.repository.jena.tdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import ro.andreiciortea.stn.platform.artifact.DigitalArtifactModelValidator;
import ro.andreiciortea.stn.platform.repository.RepositoryService;


public class JenaTDBRepositoryService implements RepositoryService {
    
    private final String DATASET_MEM_LOCATION = "org.swot.hub.repository.jenatdb";
    
    private Dataset dataset;
    
//    private Vertx vertx;
    
    
    public JenaTDBRepositoryService(Vertx vertx, boolean inMemory) {
//        this.vertx = vertx;
        
        if (inMemory) {
            dataset = TDBFactory.createDataset(Location.mem(DATASET_MEM_LOCATION));
            TDB.getContext().set(TDB.symUnionDefaultGraph, true);
        } else {
            dataset = TDBFactory.assembleDataset("store/tdb-assembler.ttl");
        }
    }
    
    private boolean containsArtifact(String uri) {
        dataset.begin(ReadWrite.READ);
        boolean res = dataset.containsNamedModel(uri);
        dataset.end();
        
        return res;
    }
    
    @Override
    public void containsArtifact(String uri, Handler<AsyncResult<Boolean>> result) {
        result.handle(Future.succeededFuture(containsArtifact(uri)));
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
    
    @Override
    public void getArtifactAsString(String uri, String format, Handler<AsyncResult<String>> result) {
        Model model = retrieveArtifact(uri);
        
        if (model == null || model.isEmpty()) {
            result.handle(Future.succeededFuture(""));
        } else {
            StringWriter sw = new StringWriter();
            model.write(sw, format);
            result.handle(Future.succeededFuture(sw.toString()));
        }
    }
    
    @Override
    public void putArtifact(String artifactUri, String rdfData, String format, Handler<AsyncResult<Void>> result) {
        try {
            dataset.begin(ReadWrite.WRITE);
            
            if (dataset.containsNamedModel(artifactUri)) {
                dataset.replaceNamedModel(artifactUri, createModel(rdfData, format));
            } else {
                dataset.addNamedModel(artifactUri, createModel(rdfData, format));
            }
    
            dataset.commit();
            dataset.end();
            
            result.handle(Future.succeededFuture());
        } catch (JenaException e) {
            result.handle(Future.failedFuture(e));
        }
    }
    
    @Override
    public void deleteArtifact(String uri, Handler<AsyncResult<Boolean>> result) {
        if (!containsArtifact(uri)) {
            result.handle(Future.succeededFuture(false));
        } else {
            dataset.begin(ReadWrite.WRITE);
            dataset.removeNamedModel(uri);
            dataset.commit();
            dataset.end();
            
            result.handle(Future.succeededFuture(true));
        }
    }
    
    @Override
    public void runSelectQuery(String queryString, Handler<AsyncResult<String>> result) {
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
            
            result.handle(Future.succeededFuture(out.toString("UTF-8")));
        } catch(Exception e) {
//            System.out.println("Query failed: " + e.getMessage());
            result.handle(Future.failedFuture(e.getMessage()));
        }
        
        dataset.end();
    }
    
    @Override
    public void runConstructQuery(String queryString, String format, Handler<AsyncResult<String>> result) {
        dataset.begin(ReadWrite.READ);
        
//        long startTime = System.currentTimeMillis();
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, dataset)) {
            Model model = qexec.execConstruct();
            
            String results = DigitalArtifactModelValidator.modelToString(model, format);
            
//            long endTime = System.currentTimeMillis();
//            MockupAgentService.logQueryTime(endTime - startTime);
//            logTime(endTime - startTime);
            
            result.handle(Future.succeededFuture(results));
        } catch(Exception e) {
            result.handle(Future.failedFuture(e.getMessage()));
        }
        
        dataset.end();
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
