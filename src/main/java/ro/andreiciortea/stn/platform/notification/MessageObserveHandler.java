package ro.andreiciortea.stn.platform.notification;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.vocabulary.RDF;

import ro.andreiciortea.stn.vocabulary.STNCore;


public class MessageObserveHandler extends DefaultArtifactObserveHandler {
    
    @Override
    protected String getSelectObserversQuery(String artifactIRI) {
        return "SELECT ?observerUri ?callbackUri "
                + "WHERE { "
                + "<" + artifactIRI + "> <" + STNCore.hasSender + "> ?senderAccountUri ."
                + "?relationUri <" + RDF.type + "> <" + STNCore.Relation + "> ."
                + "?relationUri <" + STNCore.target + "> ?senderAccountUri ."
                + "?relationUri <" + STNCore.source + "> ?observerUri ."
                + "OPTIONAL { ?observerUri <" + STNCore.callbackUri + "> ?callbackUri }"
                + "}";
    }
    
    @Override
    public AgentCard parseObserveQuerySolution(QuerySolution solution) {
        String observerIRI = getIRIOrNUll(solution, "observerUri");        
        String callbackIRI = getIRIOrNUll(solution, "callbackUri");
        
        return new AgentCard(observerIRI, callbackIRI);
    }

}
