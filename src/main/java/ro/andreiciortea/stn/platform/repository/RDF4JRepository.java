package ro.andreiciortea.stn.platform.repository;

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

import io.vertx.core.json.JsonObject;


public class RDF4JRepository implements ArtifactRepository, SparqlRepository {

    private Repository repo;
    
    @Override
    public void init(JsonObject config) {
        repo = new SailRepository(new MemoryStore());
        repo.initialize();
    }
    
    @Override
    public boolean containsArtifact(String artifactIri) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getArtifact(String artifactIri, String format) throws ArtifactNotFoundException, RepositoryException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createArtifact(String artifactIri, String data, String format) throws RepositoryException {
        try (RepositoryConnection conn = repo.getConnection()) {
            InputStream stream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
            conn.add(stream, "", RDFFormat.TURTLE); // TODO: use format parameter
        } catch (Exception e) {
            throw new RepositoryException();
        }
    }

    @Override
    public void updateArtifact(String artifactIri, String data, String format)
            throws ArtifactNotFoundException, RepositoryException {
        
        try (RepositoryConnection conn = repo.getConnection()) {
            InputStream stream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
            conn.add(stream, "", RDFFormat.TURTLE); // TODO: use format parameter
        } catch (Exception e) {
            throw new RepositoryException();
        }
    }

    @Override
    public void deleteArtifact(String artifactIri) throws ArtifactNotFoundException, RepositoryException {
        // TODO Auto-generated method stub

    }
    
    @Override
    public String runSelectQuery(String queryString) throws RepositoryException {
        try (RepositoryConnection conn = repo.getConnection()) {
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            TupleQueryResultHandler writer = new SPARQLResultsJSONWriter(out);
            tupleQuery.evaluate(writer);
            
            return out.toString();
        } catch (Exception e) {
            throw new RepositoryException();
        }
    }

    @Override
    public boolean runAskQuery(String queryString) throws RepositoryException {
        // TODO
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String runConstructQuery(String queryString, String format) throws RepositoryException {
        try (RepositoryConnection conn = repo.getConnection()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, out);
            conn.prepareGraphQuery(queryString).evaluate(writer);
            
            return out.toString();
        } catch (Exception e) {
            throw new RepositoryException();
        }
    }
    
    @Override
    public String runDescribeQuery(String queryString, String format) throws RepositoryException {
        // TODO
        throw new UnsupportedOperationException();
    }

}
