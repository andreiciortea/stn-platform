package ro.andreiciortea.stn.platform.repository.rdf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResultHandler;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import ro.andreiciortea.stn.platform.repository.RepositoryService;


public class Rdf4jRepositoryService implements RepositoryService {
    
//    private Vertx vertx;
    private Repository repo;
    
    public Rdf4jRepositoryService(Vertx vertx) {
//        this.vertx = vertx;
        
        repo = new SailRepository(new MemoryStore());
        repo.initialize();
    }
    
    @Override
    public void containsArtifact(String uri, Handler<AsyncResult<Boolean>> result) {
        
    }

//    @Override
//    public void getArtifact(String uri, Handler<AsyncResult<DigitalArtifactDeprecated>> result) {
        // TODO Auto-generated method stub

//    }

    @Override
    public void getArtifactAsString(String uri, String format, Handler<AsyncResult<String>> result) {
/*        try (RepositoryConnection conn = repo.getConnection()) {
            Model model = conn.get;
            
            if (model == null || model.isEmpty()) {
                result.handle(Future.succeededFuture(""));
            } else {
                StringWriter sw = new StringWriter();
                model.write(sw, format);
                result.handle(Future.succeededFuture(sw.toString()));
            }
        }*/
    }

    @Override
    public void putArtifact(String artifactUri, String rdfData, String format, Handler<AsyncResult<Void>> result) {
//        long start = System.currentTimeMillis();
        try (RepositoryConnection conn = repo.getConnection()) {
            InputStream stream = new ByteArrayInputStream(rdfData.getBytes(StandardCharsets.UTF_8));
            conn.add(stream, "", RDFFormat.TURTLE);
//            long end = System.currentTimeMillis();
//            logTime("st", end - start);
            
            result.handle(Future.succeededFuture());
        } catch (Exception e) {
            e.printStackTrace();
            result.handle(Future.failedFuture(e.getMessage()));
        }
    }
    
    @Override
    public void deleteArtifact(String uri, Handler<AsyncResult<Boolean>> result) {
        // TODO Auto-generated method stub

    }

    @Override
    public void runSelectQuery(String queryString, Handler<AsyncResult<String>> result) {
//        long start = System.currentTimeMillis();
        try (RepositoryConnection conn = repo.getConnection()) {
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            TupleQueryResultHandler writer = new SPARQLResultsJSONWriter(out);
            tupleQuery.evaluate(writer);
            
//            long end = System.currentTimeMillis();
//            logTime("qt", end - start);
            
//            System.out.println("Result set [" + (end-start) + "ms] : " + out.toString());            
            
            result.handle(Future.succeededFuture(out.toString()));
        } catch (Exception e) {
            result.handle(Future.failedFuture(e.getMessage()));
        }
    }
    
/*    private void logTime(String type, long duration) {
        LocalMap<String, Long> map = vertx.sharedData().getLocalMap("benchmark");
        Long total = map.get(type);
        
        if (total != null) {
            total += (duration);
            map.put(type, total);
        }
    }*/

    @Override
    public void runConstructQuery(String queryString, String format, Handler<AsyncResult<String>> result) {
        try (RepositoryConnection conn = repo.getConnection()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, out);
            conn.prepareGraphQuery(queryString).evaluate(writer);
            
            result.handle(Future.succeededFuture(out.toString()));
        } catch (Exception e) {
            result.handle(Future.failedFuture(e.getMessage()));
        }
    }

}
