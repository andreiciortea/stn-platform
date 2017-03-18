package ro.andreiciortea.stn.platform.notification;

import org.apache.jena.query.QuerySolution;

public interface QuerySolutionParser {

    AgentCard parseQuerySolution(QuerySolution solution);
}
