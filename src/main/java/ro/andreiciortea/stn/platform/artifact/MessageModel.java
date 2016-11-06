package ro.andreiciortea.stn.platform.artifact;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.shareddata.LocalMap;
import ro.andreiciortea.stn.platform.repository.RepositoryService;
import ro.andreiciortea.stn.vocabulary.STNCore;


public class MessageModel extends DigitalArtifactModel {

    private static final String MESSAGE_CONTAINER_PATH = "/messages/";
    
    
    @Override
    public String getContainerPath() {
        return MESSAGE_CONTAINER_PATH;
    }
    
    @Override
    public List<Statement> getRequiredStatements(String artifactUri) {
        List<Statement> list = super.getRequiredStatements(artifactUri);
        
        list.add(ResourceFactory.createStatement(
                    ResourceFactory.createResource(artifactUri), 
                    RDF.type, 
                    STNCore.Message
                )
            );
        
        return list;
    }
    
    /** design a hash function for caching, smthg like f(accountUri, relationType) = hash value **/

    
    public List<String> extractReceivers(Model message) {
        List<String> receivers = new ArrayList<String>();
        NodeIterator iterator = message.listObjectsOfProperty(STNCore.hasReceiver);
        
        while (iterator.hasNext()) {
            RDFNode node = iterator.next();
            if (node.isURIResource()) {
                receivers.add(((Resource) node).getURI());
            }
        }
        
        return receivers;
    }
    
    @Override
    public void getObservers(String artifactUri, Model artifactModel, 
            RepositoryService repository, LocalMap<String, String> cache, Handler<AsyncResult<List<String>>> hObservers) {
        
        List<String> observers = new ArrayList<String>();
        List<String> receivers = extractReceivers(artifactModel);
        
        // TOOD: refactor this mess
        if (receivers != null && !receivers.isEmpty()) {
//            for (String r : receivers) {
                // TODO: check for callback URI
//                observers.add(new Observer(r, ""));
//                return receivers;
//            System.out.println("Receivers: " + receivers);
//            hObservers.handle(Future.succeededFuture(receivers));
//            }
            String query = "SELECT ?observerUri ?callbackUri "
                    + "WHERE { "
                    + "<" + artifactUri + "> <" + RDF.type + "> <" + STNCore.Message + "> ."
                    + "<" + artifactUri + "> <" + STNCore.hasReceiver + "> ?observerUri ."
                    + "OPTIONAL { ?observerUri <" + STNCore.callbackUri + "> ?callbackUri }"
                    + "}";
            
//            System.out.println("Query: " + query);

            repository.runSelectQuery(query, r -> {
                if (r.succeeded()) {
                    ResultSet results = 
                            ResultSetFactory.fromJSON(new ByteArrayInputStream(r.result().getBytes(StandardCharsets.UTF_8)));
                    
                    while (results.hasNext()) {
                        observers.addAll(parseObserversQuerySolution(results.next()));
                    }
                    
//                    System.out.println("Callbacks: " + observers);
                    
                    hObservers.handle(Future.succeededFuture(observers));
                } else {
                    hObservers.handle(Future.failedFuture(r.cause().getMessage()));
                }
            });
        } else {
            Resource sender = (Resource) artifactModel.getRequiredProperty(null, STNCore.hasSender).getObject();
            
//            System.out.println("Message has sender: " + sender.getURI());
            
            if (sender.isURIResource()) {
                String connections = cache.get(sender.getURI());
                
                if (connections == null) {
                    String query = "SELECT ?observerUri ?callbackUri "
                                        + "WHERE { "
                                        + "?relationUri <" + RDF.type + "> <" + STNCore.Relation + "> ."
                                        + "?relationUri <" + STNCore.target + "> <" + sender.getURI() + "> ."
                                        + "?relationUri <" + STNCore.source + "> ?observerUri ."
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
                            
                            // TODO: proper cache implementation
//                            cache.put(sender.getURI(), r.result());
                        } else {
                            hObservers.handle(Future.failedFuture(r.cause().getMessage()));
                        }
                    });
                } else {
                    ResultSet results = 
                            ResultSetFactory.fromJSON(new ByteArrayInputStream(connections.getBytes(StandardCharsets.UTF_8)));
                    
                    while (results.hasNext()) {
                        observers.addAll(parseObserversQuerySolution(results.next()));
                    }
                    
                    hObservers.handle(Future.succeededFuture(observers));
                }
            }
        }
    }
    
    @Override
    public String getSelectObserversQuery(String artifactUri) {
        
        // TODO: private messages with explicit receivers
        
        return "SELECT ?observerUri ?callbackUri "
                + "WHERE { "
                + "<" + artifactUri + "> <" + STNCore.hasSender + "> ?senderAccountUri ."
//                + "?relationUri <" + RDF.type + "> <" + STNCore.Relation + "> ."
                + "?relationUri <" + STNCore.target + "> ?senderAccountUri ."
                + "?relationUri <" + STNCore.source + "> ?observerUri ."
//                + "OPTIONAL { ?observerUri <" + STNCore.callbackUri + "> ?callbackUri }"
                + "}";
        
/*        return "SELECT ?observerUri ?callbackUri "
                + "WHERE { "
                + "<" + artifactUri + "> <" + STNCore.hasSender + "> ?senderUri ."
                + "?relationUri <" + RDF.type + "> <" + STNCore.Relation + "> ."
                + "?relationUri <" + STNCore.target + "> ?senderUri ."
                + "?relationUri <" + STNCore.source + "> ?followerUri ."
                + "OPTIONAL { <" + artifactUri + "> <" + STNCore.hasReceiver + "> ?receiverUri }"
                + "OPTIONAL { ?receiverUri <" + STNCore.callbackUri + "> ?receiverCallbackUri }"
                + "OPTIONAL { ?followerUri <" + STNCore.callbackUri + "> ?followerCallbackUri }"
                + "BIND ( IF(bound(?receiverUri), ?receiverUri, ?followerUri) AS ?observerUri )"
                + "BIND ( IF(bound(?receiverUri), ?receiverCallbackUri, ?followerCallbackUri) AS ?callbackUri )"
                + "}";*/
    }
    
    @Override
    public List<String> parseObserversQuerySolution(QuerySolution solution) {
        List<String> observers = new ArrayList<String>();
        
        RDFNode callbackNode = solution.get("callbackUri");
        
        if (callbackNode == null) {
            RDFNode observerNode = solution.get("observerUri");
            
            if (observerNode != null && observerNode.isURIResource()) {
                String observerUri = ((Resource) observerNode).getURI();
//                System.out.println("Agent URI: " + observerUri);
                observers.add(observerUri);
            }
        } else {
            String callbackUri = ((Resource) callbackNode).getURI();
//            System.out.println("Callback URI: " + callbackUri);
            observers.add(callbackUri);
        }
        
        return observers;
    }
}
