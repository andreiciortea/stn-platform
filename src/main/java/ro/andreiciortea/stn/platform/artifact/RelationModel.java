package ro.andreiciortea.stn.platform.artifact;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.shareddata.LocalMap;
import ro.andreiciortea.stn.platform.repository.RepositoryService;
import ro.andreiciortea.stn.vocabulary.STNCore;

public class RelationModel extends DigitalArtifactModel {

    public static final String SOCIAL_RELATION_CONTAINER_PATH = "/connections/";
    
    @Override
    public String getContainerPath() {
        return SOCIAL_RELATION_CONTAINER_PATH;
    }

    @Override
    public List<Statement> getRequiredStatements(String artifactUri) {
        List<Statement> list = super.getRequiredStatements(artifactUri);
        
        list.add(ResourceFactory.createStatement(
                    ResourceFactory.createResource(artifactUri), 
                    RDF.type, 
                    STNCore.Relation
                )
            );
        
        return list;
    }
    
    @Override
    public String getConstructCollectionQuery(MultiMap params) {
        String sourceUri = params.get("source");
        String targetUri = params.get("target");
        
        if (sourceUri == null && targetUri == null) return null;
        
        if (sourceUri == null) {
            return "CONSTRUCT {"
                    + "?relationUri <" + RDF.type + "> <" + STNCore.Relation + "> ."
                    + "?relationUri <" + STNCore.source + "> ?sourceUri ."
                    + "?relationUri <" + STNCore.target + "> <" + targetUri + "> ."
                    + "}"
                    + "WHERE {"
                    + "?relationUri <" + STNCore.source + "> ?sourceUri ."
                    + "?relationUri <" + STNCore.target + "> <" + targetUri + "> ."
                    + "}";
        }
        
        if (targetUri == null) {
            return "CONSTRUCT {"
                    + "?relationUri <" + RDF.type + "> <" + STNCore.Relation + "> ."
                    + "?relationUri <" + STNCore.source + "> <" + sourceUri + "> ."
                    + "?relationUri <" + STNCore.target + "> ?targetUri ."
                    + "}"
                    + "WHERE {"
                    + "?relationUri <" + STNCore.source + "> <" + sourceUri + "> ."
                    + "?relationUri <" + STNCore.target + "> ?targetUri ."
                    + "}";
        }
        
        return "CONSTRUCT {"
                + "?relationUri <" + RDF.type + "> <" + STNCore.Relation + "> ."
                + "?relationUri <" + STNCore.source + "> <" + sourceUri + "> ."
                + "?relationUri <" + STNCore.target + "> <" + targetUri + "> ."
                + "}"
                + "WHERE {"
                + "?relationUri <" + STNCore.source + "> <" + sourceUri + "> ."
                + "?relationUri <" + STNCore.target + "> <" + targetUri + "> ."
                + "}";
    }
    
    @Override
    public void getObservers(String artifactUri, Model artifactModel, 
            RepositoryService repository, LocalMap<String, String> cache, Handler<AsyncResult<List<String>>> hObservers) {
        
        List<String> observers = new ArrayList<String>();
        
        String query = "SELECT ?observerUri ?callbackUri "
                + "WHERE { "
                + "<" + artifactUri + "> <" + STNCore.target + "> ?observerUri ."
                + "OPTIONAL { ?observerUri <" + STNCore.callbackUri + "> ?callbackUri }"
                + "}";
        
        repository.runSelectQuery(query, r -> {
            if (r.succeeded()) {
                ResultSet results = 
                        ResultSetFactory.fromJSON(new ByteArrayInputStream(r.result().getBytes(StandardCharsets.UTF_8)));
                
                while (results.hasNext()) {
                    observers.addAll(parseObserversQuerySolution(results.next()));
                }
                
                hObservers.handle(Future.succeededFuture(observers));
            } else {
                hObservers.handle(Future.failedFuture(r.cause().getMessage()));
            }
        });
    }
    
    @Override
    public String getSelectObserversQuery(String artifactUri) {
        return "SELECT ?observerUri ?callbackUri "
                + "WHERE { "
                + "<" + artifactUri + "> <" + STNCore.target + "> ?observerUri ."
                + "OPTIONAL { ?observerUri <" + STNCore.callbackUri + "> ?callbackUri }"
                + "}";
    }
    
    @Override
    public List<String> parseObserversQuerySolution(QuerySolution solution) {
        List<String> observers = new ArrayList<String>();
        
        RDFNode callbackNode = solution.get("callbackUri");
        
        if (callbackNode == null) {
/*            RDFNode observerNode = solution.get("observerUri");
            
            if (observerNode != null && observerNode.isURIResource()) {
                String observerUri = ((Resource) observerNode).getURI();
                System.out.println("Agent URI: " + observerUri);
                observers.add(observerUri);
            }*/
        } else {
            String callbackUri = ((Resource) callbackNode).getURI();
//            System.out.println("Callback URI: " + callbackUri);
            observers.add(callbackUri);
        }
        
        return observers;
    }

}
