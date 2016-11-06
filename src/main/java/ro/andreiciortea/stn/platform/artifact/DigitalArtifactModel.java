package ro.andreiciortea.stn.platform.artifact;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.shareddata.LocalMap;
import ro.andreiciortea.stn.platform.repository.RepositoryService;


public abstract class DigitalArtifactModel {
    
    private boolean isObservable = false;
    private boolean isPersistent = true;
    
    
    /** Data model validation **/
    
    public abstract String getContainerPath();
    
    public List<Statement> getRequiredStatements(String artifactUri) {
        return new ArrayList<Statement>(); 
    }
    
    public List<Statement> getOptionalStatements(String artifactUri) {
        return new ArrayList<Statement>();
    }
    
    public List<Statement> getPlatformStatements(String artifactUri) {
        return new ArrayList<Statement>();
    }
    
    
    /** Query filtering **/
    
    public String getConstructCollectionQuery(MultiMap params) {
        return null; 
    }
    
    
    /** Storage and caching **/
    
    public DigitalArtifactModel withoutStorage() {
        isPersistent = false;
        return this;
    }
    
    public boolean isPersistent() {
        return isPersistent;
    }
    
    
    /** Notification routing **/
    
    public DigitalArtifactModel withObserve() {
        isObservable = true;
        return this;
    }
    
    public boolean isObservable() {
        return isObservable;
    }
    
    public void getObservers(String artifactUri, Model artifactModel, RepositoryService repository, 
            LocalMap<String, String> cache, Handler<AsyncResult<List<String>>> hObservers) {
        // TODO
    }
    
    public String getSelectObserversQuery(String artifactUri) {
        return null;
    }
    
    public List<String> parseObserversQuerySolution(QuerySolution solution) {
        return null;
    }
}
