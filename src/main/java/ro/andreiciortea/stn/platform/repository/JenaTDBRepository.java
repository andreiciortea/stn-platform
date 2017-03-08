package ro.andreiciortea.stn.platform.repository;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

import org.apache.http.HttpStatus;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.JenaException;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.base.file.Location;

import io.vertx.core.json.JsonObject;
import ro.andreiciortea.stn.platform.RdfUtils;
import ro.andreiciortea.stn.platform.artifact.DigitalArtifactModelValidator;
import ro.andreiciortea.stn.platform.eventbus.RepositoryResponse;


public class JenaTDBRepository implements ArtifactRepository, SparqlRepository {
    
    private final String DATASET_MEM_LOCATION = "ro.andreiciortea.stn.platform.repository.jenatdb";
    
    private Dataset dataset;
    
    
    public void init(JsonObject config) {
        boolean inMemory = config.getBoolean("in-memory", false);
        
        if (inMemory) {
            dataset = TDBFactory.createDataset(Location.mem(DATASET_MEM_LOCATION));
            TDB.getContext().set(TDB.symUnionDefaultGraph, true);
        } else {
            String assemblerFile = config.getString("assemblerFilePath", "store/tdb-assembler.ttl");
            dataset = TDBFactory.assembleDataset(assemblerFile);
        }
    }
    
    public boolean containsArtifact(String artifactUri) {
        dataset.begin(ReadWrite.READ);
        boolean res = dataset.containsNamedModel(artifactUri);
        dataset.end();
        
        return (res == true);
    }
    
    private Model retrieveArtifact(String uri) {
        dataset.begin(ReadWrite.READ);
        Model model = dataset.getNamedModel(uri);
        dataset.end();
        
        return model;
    }
    
    public String getArtifact(String uri, String format) throws ArtifactNotFoundException {
        Model model = retrieveArtifact(uri);
        
        if (model == null || model.isEmpty()) {
            throw new ArtifactNotFoundException();
        } else {
            StringWriter sw = new StringWriter();
            model.write(sw, format);
            
            return sw.toString();
        }
    }
    
    @Override
    public void createArtifact(String artifactUri, String rdfData, String format) throws ArtifactRepositoryException {
        try {
            dataset.begin(ReadWrite.WRITE);
            dataset.addNamedModel(artifactUri, RdfUtils.stringToRdfModel(rdfData, format));
            dataset.commit();
            dataset.end();
        } catch (JenaException e) {
            throw new ArtifactRepositoryException();
        }
    }
    
    @Override
    public void updateArtifact(String artifactUri, String rdfData, String format) throws ArtifactNotFoundException, ArtifactRepositoryException {
        if (!containsArtifact(artifactUri)) {
            throw new ArtifactNotFoundException();
        } else {
            dataset.begin(ReadWrite.WRITE);
            dataset.replaceNamedModel(artifactUri, RdfUtils.stringToRdfModel(rdfData, format));
            dataset.commit();
            dataset.end();
        }
    }
    
    @Override
    public void deleteArtifact(String artifactUri)  throws ArtifactNotFoundException, ArtifactRepositoryException {
        if (!containsArtifact(artifactUri)) {
            throw new ArtifactNotFoundException();
        } else {
            dataset.begin(ReadWrite.WRITE);
            dataset.removeNamedModel(artifactUri);
            dataset.commit();
            dataset.end();
        }
    }
    
    @Override
    public RepositoryResponse runSelectQuery(String queryString) {
        dataset.begin(ReadWrite.READ);
        
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, dataset)) {
            ResultSet queryResultSet = qexec.execSelect();
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(out, queryResultSet);
            
            dataset.end();
            
            return new RepositoryResponse(HttpStatus.SC_OK, null, out.toString("UTF-8"));
        } catch(Exception e) {
            dataset.end();
            return new RepositoryResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, null, e.getMessage());
        }
    }
    
    @Override
    public RepositoryResponse runConstructQuery(String queryString, String format) {
        dataset.begin(ReadWrite.READ);
        
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, dataset)) {
            Model model = qexec.execConstruct();
            
            String results = DigitalArtifactModelValidator.modelToString(model, format);
            
            dataset.end();
            return new RepositoryResponse(HttpStatus.SC_OK, null, results);
        } catch(Exception e) {
            dataset.end();
            return new RepositoryResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, null, e.getMessage());
        }
    }
    
    @Override
    public RepositoryResponse runAskQuery(String queryString) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public RepositoryResponse runDescribeQuery(String queryString, String format) {
        // TODO
        throw new UnsupportedOperationException();
    }
    
}
