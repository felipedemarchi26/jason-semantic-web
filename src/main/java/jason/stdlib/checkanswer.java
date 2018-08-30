package jason.stdlib;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

import jason.JasonException;
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

public class checkanswer extends DefaultInternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null)
            singleton = new checkanswer();
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
        Term answer = args[0];
        Term uri = args[1];
        Term predicate = args[2];

        if (answer.isString() && uri.isString() && predicate.isString()) {
            
            List<SparqlObject> sparqlObjects = new ArrayList<SparqlObject>();
            SparqlObject sparqlObject = new SparqlObject();
            StringTerm stAnswer = (StringTerm) answer;
            String sAnswer = stAnswer.getString();
            StringTerm stUri = (StringTerm) uri;
            sparqlObject.setUri(stUri.getString());
            StringTerm stPredicate = (StringTerm) predicate;
            sparqlObject.setPredicate(stPredicate.getString());
            sparqlObject.setObject(new String("?result"));
            sparqlObjects.add(sparqlObject);
            
            boolean isLiteral = false;
            
            if (sAnswer.isEmpty()) {
                throw new JasonException("No answer was sent.");
            } else {
                List<SparqlResult> results = SparqlSearch.create().searchDbpedia(sparqlObjects);
                for (SparqlResult result : results) {
                    if (result.getResourceResult() != null) {
                        Resource resource = (Resource) result.getResourceResult();
                        List<SparqlObject> sparqlObjName = new ArrayList<SparqlObject>();
                        SparqlObject sparqlForName = new 
                                SparqlObject("<"+resource.getURI()+">", "foaf:name");
                        sparqlForName.setObject(new String("?result"));
                        sparqlObjName.add(sparqlForName);
                        List<SparqlResult> resourceName = SparqlSearch.
                                create().searchDbpedia(sparqlObjName);
                        
                        for (SparqlResult name : resourceName) {
                            if (name.getLiteralResult() != null) {
                                Literal lName = (Literal) name.getLiteralResult();
                                String sName = lName.getString();
                                String[] splitName = sName.split("@");
                                if (sAnswer.equalsIgnoreCase(splitName[0])) {
                                    return true;
                                }
                            }
                        }
                    } else if (result.getLiteralResult() != null) {
                        isLiteral = true;
                        Literal lResult = result.getLiteralResult();
                        if (lResult.getValue().getClass().
                                equals(Integer.class)) {
                            try {
                                int answerConverted = Integer.parseInt(sAnswer);
                                if (lResult.getInt() == answerConverted) {
                                    return true;
                                }
                            } catch (NumberFormatException nfe) {
                                return false;
                            }
                        } else if (lResult.getValue().getClass().
                                equals(String.class)) {
                            String[] split = lResult.getString().split("@");
                            if (sAnswer.equalsIgnoreCase(split[0])) {
                                return true;
                            }
                        } else if (lResult.getValue().getClass().
                                equals(XSDDateTime.class)) {
                            XSDDateTime dateResult = 
                                    (XSDDateTime) lResult.getValue();
                            String splitDate[] = sAnswer.split("-");
                            try {
                                if (dateResult.getYears() == 
                                    Integer.parseInt(splitDate[0].trim()) &&
                                    dateResult.getMonths() == 
                                    Integer.parseInt(splitDate[1].trim()) &&
                                    dateResult.getDays() == 
                                    Integer.parseInt(splitDate[2].trim())) {
                                    return true;
                                }
                            } catch (NumberFormatException nfe) {
                                return false;
                            }
                        }
                    }
                }
                
                
                List<SparqlObject> correctAnswerObjects = new ArrayList<SparqlObject>();

                SparqlObject line1 = new SparqlObject(stPredicate.getString(), "?valueCorrect");
                line1.setUri(stUri.getString());
                line1.setPredicate(stPredicate.getString());
                line1.setObject("?valueCorrect");
                correctAnswerObjects.add(line1);

                if (!isLiteral) {
                    SparqlObject line2 = new SparqlObject();
                    line2.setUri("?valueCorrect");
                    line2.setPredicate("foaf:name");
                    line2.setObject("?correct");
                    line2.setOptional(true);
                    correctAnswerObjects.add(line2);

                    SparqlObject line3 = new SparqlObject();
                    line3.setUri("?uri");
                    line3.setPredicate("rdfs:label");
                    line3.setObject(answer+"@EN");
                    correctAnswerObjects.add(line3);

                    SparqlObject line4 = new SparqlObject();
                    line4.setUri("?uri");
                    line4.setPredicate("dbo:abstract");
                    line4.setObject("?description");
                    line4.setOptional(true);
                    line4.setFilter("FILTER langMatches(lang(?description), \"en\")");
                    correctAnswerObjects.add(line4);
                }
                
                //Executa a consulta
                List<SparqlResult> resultBaseCorrectAnswer = SparqlSearch.create().searchDbpedia(correctAnswerObjects);
                
                //Variável responsável por montar o feedback ao aluno
                String feedback = "";
                
                //Verifica se obteve alguma resposta
                if (!resultBaseCorrectAnswer.isEmpty() && resultBaseCorrectAnswer.size() > 0) {
                    //Obtém as informações referentes a primeira resposta válida
                    SparqlResult valueCorrectVariable = resultBaseCorrectAnswer.get(0);
                    if (isLiteral) {
                        feedback += "The correct answer to this question is " + valueCorrectVariable.getLiteralResult().toString();
                    } else {
                        SparqlResult correctVariable = resultBaseCorrectAnswer.get(1);
                        SparqlResult uriVariable = resultBaseCorrectAnswer.get(2);
                        SparqlResult descriptionVariable = resultBaseCorrectAnswer.get(3);
                        
                      //Verifica se a resposta correta é um literal
                        if (valueCorrectVariable.getLiteralResult() != null) {
                            feedback += "The correct answer to this question is " + valueCorrectVariable.getLiteralResult().toString();
                        } else if (valueCorrectVariable.getResourceResult() != null && correctVariable.getLiteralResult() != null) {
                            feedback += "The correct answer to this question is " + correctVariable.getLiteralResult().toString();
                        }
                        
                        if (uriVariable.getResourceResult() != null && descriptionVariable.getLiteralResult() != null) {
                            feedback += "\nDid you know?\n" + descriptionVariable.getLiteralResult().toString();
                        }
                    }
                    
                    Term t = new StringTermImpl(feedback);
                    un.unifies(args[3], t);
                }
                return false;
            }  
        }
        return false;
    }
    
}
