package jason.stdlib;

import java.util.ArrayList;
import java.util.List;

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

/**
 * Classe responsável pela implementação da Ação Interna adicionada ao Jason que
 * realiza a consulta SPARQL na DBPedia.
 * 
 * @author felipe.demarchi
 *
 */
public class searchdbpedia extends DefaultInternalAction {

    private static InternalAction singleton = null;

    public static InternalAction create() {
        if (singleton == null)
            singleton = new searchdbpedia();
        return singleton;
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 2;
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

            SparqlObject label = new SparqlObject();
            label.setUri(stUri.getString());
            label.setPredicate("rdfs:label");
            label.setObject(new String("?label"));
            sparqlObjects.add(label);

            List<SparqlResult> results = SparqlSearch.create().searchDbpedia(sparqlObjects);
            if (!results.isEmpty()) {
                for (SparqlResult sr : results) {
                    if (sr.getVariable().equals("?label")) {
                        Term t = new StringTermImpl(sr.getLiteralResult().getString());
                        return un.unifies(args[2], t);
                    }
                }
            }
            
            return false;
        }
        return false;
    }

}
