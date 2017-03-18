package ro.andreiciortea.stn.platform.notification;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import ro.andreiciortea.stn.platform.eventbus.RepositoryRequest;
import ro.andreiciortea.stn.platform.eventbus.SparqlRequest;
import ro.andreiciortea.stn.platform.repository.RepositoryServiceVerticle;


public abstract class DefaultArtifactObserveHandler implements ArtifactObserveHandler {
    
    protected abstract String getSelectObserversQuery(String artifactIRI);
    
    protected abstract AgentCard parseObserveQuerySolution(QuerySolution solution);
    
    public List<AgentCard> getObservers(String artifactIRI, String artifactStr) {
        List<AgentCard> observerCards = new ArrayList<AgentCard>();
        
        RepositoryRequest request = new SparqlRequest(getSelectObserversQuery(artifactIRI));
        EventBus eventBus = Vertx.currentContext().owner().eventBus();
        
        eventBus.send(RepositoryServiceVerticle.REPOSITORY_BUS_ADDRESS, request.toJson(), reply -> {
            if (reply.succeeded() && reply.result().body() != null) {
                String resultsStr = reply.result().body().toString();
                
                ResultSet results = 
                        ResultSetFactory.fromJSON(new ByteArrayInputStream(resultsStr.getBytes(StandardCharsets.UTF_8)));
                
                observerCards.addAll(buildObserverList(artifactIRI, artifactStr, results));
            }
        });
        
        return observerCards;
    }
    
    private List<AgentCard> buildObserverList(String artifactIRI, String artifactStr, ResultSet results) {
        List<AgentCard> list = new ArrayList<AgentCard>();
        
        while (results.hasNext()) {
            list.add(parseObserveQuerySolution(results.next()));
        }
        
        return list;
    }
    
    protected String getIRIOrNUll(QuerySolution solution, String label) {
        RDFNode observerNode = solution.get(label);
        
        if (observerNode != null && observerNode.isURIResource()) {
            return ((Resource) observerNode).getURI();
        }
        
        return null;
    }
    
    
}
