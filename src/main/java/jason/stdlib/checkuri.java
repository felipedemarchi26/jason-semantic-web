package jason.stdlib;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;
import jason.semanticWeb.SparqlObject;
import jason.semanticWeb.SparqlResult;
import jason.semanticWeb.SparqlSearch;

public class checkuri extends DefaultInternalAction {

    private static InternalAction singleton = null;

    public static InternalAction create() {
        if (singleton == null)
            singleton = new checkuri();
        return singleton;
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        Term t1 = args[0];
        Term t2 = args[1];
        if (t1.isString() && t2.isString()) {
            List<SparqlObject> sparqlObjects = new ArrayList<SparqlObject>();

            SparqlObject sparqlObject = new SparqlObject();
            StringTerm stUri = (StringTerm) args[0];
            sparqlObject.setUri(stUri.getString());
            StringTerm stPredicate = (StringTerm) args[1];
            sparqlObject.setPredicate(stPredicate.getString());
            sparqlObject.setObject(new String("?result"));

            sparqlObjects.add(sparqlObject);

            List<SparqlResult> results = SparqlSearch.create().
                    searchDbpedia(sparqlObjects);
            for (SparqlResult result : results) {
                if (result.getResourceResult() != null) {
                    Term t = new StringTermImpl("<" + result.
                            getResourceResult().getURI() + ">");
                    return un.unifies(args[2], t);
                }
            }
        }
        return false;
    }

}
