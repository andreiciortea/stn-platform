package ro.andreiciortea.stn.platform.notification;

import org.apache.jena.query.QuerySolution;

import ro.andreiciortea.stn.vocabulary.STNCore;


public class RelationObserveHandler extends DefaultArtifactObserveHandler {

    @Override
    protected String getSelectObserversQuery(String artifactIRI) {
        return "SELECT ?observerUri ?callbackUri "
                + "WHERE { "
                + "<" + artifactIRI + "> <" + STNCore.target + "> ?observerUri ."
                + "OPTIONAL { ?observerUri <" + STNCore.callbackUri + "> ?callbackUri }"
                + "}";
    }

    @Override
    protected AgentCard parseObserveQuerySolution(QuerySolution solution) {
        String observerIRI = getIRIOrNUll(solution, "observerUri");        
        String callbackIRI = getIRIOrNUll(solution, "callbackUri");
        
        return new AgentCard(observerIRI, callbackIRI);
    }

}
