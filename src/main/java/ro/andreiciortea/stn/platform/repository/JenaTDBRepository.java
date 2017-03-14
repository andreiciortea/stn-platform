package ro.andreiciortea.stn.platform.repository;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

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


public class JenaTDBRepository implements ArtifactRepository, SparqlRepository {
    
    private final String DATASET_MEM_LOCATION = "ro.andreiciortea.stn.platform.repository.jenatdb";
    
    private Dataset dataset;
    
    
    private void initInMemory() {
        dataset = TDBFactory.createDataset(Location.mem(DATASET_MEM_LOCATION));
        TDB.getContext().set(TDB.symUnionDefaultGraph, true);
    }
    
    public void init(JsonObject config) {
        if (config == null) {
            initInMemory();
        } else {
            boolean inMemory = config.getBoolean("in-memory", false);
            
            if (inMemory) {
                initInMemory();
            } else {
                String assemblerFile = config.getString("assemblerFilePath", "store/tdb-assembler.ttl");
                dataset = TDBFactory.assembleDataset(assemblerFile);
            }
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
    public void createArtifact(String artifactUri, String rdfData, String format) throws RepositoryException {
        try {
            dataset.begin(ReadWrite.WRITE);
            dataset.addNamedModel(artifactUri, RdfUtils.stringToRdfModel(rdfData, format));
            dataset.commit();
            dataset.end();
        } catch (JenaException e) {
            throw new RepositoryException();
        }
    }
    
    @Override
    public void updateArtifact(String artifactUri, String rdfData, String format) throws ArtifactNotFoundException, RepositoryException {
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
    public void deleteArtifact(String artifactUri)  throws ArtifactNotFoundException, RepositoryException {
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
    public ResultSet runSelectQuery(String queryString) throws RepositoryException {
        dataset.begin(ReadWrite.READ);
        
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, dataset)) {
            ResultSet queryResultSet = qexec.execSelect();
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(out, queryResultSet);
            
            dataset.end();
            
            return queryResultSet; //new RepositoryResponse(HttpStatus.SC_OK, null, out.toString("UTF-8"));
        } catch(Exception e) {
            dataset.end();
            throw new RepositoryException();
        }
    }
    
    @Override
    public Model runConstructQuery(String queryString, String format) throws RepositoryException {
        dataset.begin(ReadWrite.READ);
        
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, dataset)) {
            Model model = qexec.execConstruct();
            
            dataset.end();
            
            return model;
        } catch(Exception e) {
            dataset.end();
            throw new RepositoryException();
        }
    }
    
    @Override
    public boolean runAskQuery(String queryString) throws RepositoryException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Model runDescribeQuery(String queryString, String format) throws RepositoryException {
        // TODO
        throw new UnsupportedOperationException();
    }
    
}
