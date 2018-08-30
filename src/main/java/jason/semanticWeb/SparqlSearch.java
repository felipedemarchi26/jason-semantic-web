package jason.semanticWeb;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.DatasetImpl;




public class SparqlSearch {
    
    private static SparqlSearch singleton = null;
    
    public static SparqlSearch create() {
        if (singleton == null)
            singleton = new SparqlSearch();
        return singleton;
    }

    private final String OWL = "PREFIX owl: <http://www.w3.org/2002/07/owl#>";
    private final String XSD = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>";
    private final String RDFS = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>";
    private final String RDF = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
    private final String FOAF = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>";
    private final String DC = "PREFIX dc: <http://purl.org/dc/elements/1.1/>";
    private final String DB = "PREFIX : <http://dbpedia.org/resource/>";
    private final String DBPEDIA2 = "PREFIX dbpedia2: <http://dbpedia.org/property/>";
    private final String DBPEDIA = "PREFIX dbpedia: <http://dbpedia.org/>";
    private final String SKOS = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>";
    private final String DBO = "PREFIX dbo: <http://dbpedia.org/ontology/>";
    
    private final String PREFIXES = OWL + XSD + RDFS + RDF + FOAF + DC + DB + DBPEDIA2 + DBPEDIA + SKOS + DBO;
    
    private final String SERVICE = "SERVICE <http://DBpedia.org/sparql>";
    
    //Método responsável por realizar a consulta SPARQL e retornar o resultado
    public List<SparqlResult> searchDbpedia(List<SparqlObject> sparqlObjects) {

        String querySearch = "";
        String results = "";

        for (SparqlObject so : sparqlObjects) {
            querySearch += so.toString();
            if (so.isUriVariable() && !results.contains(so.getUri())) {
                results += " " + so.getUri();
            }
            if (so.isPredicateVariable() && !results.contains(so.getPredicate())) {
                results += " " + so.getPredicate();
            }
            if (so.isObjectVariable() && !results.contains((String) so.getObject())) {
                String s = (String) so.getObject();
                results += " " + s;
            }
        }

        String query = PREFIXES + 
                "SELECT " + results + " WHERE {"
                + SERVICE + " {"
                + querySearch
                + "}"
                + "}";
        
        //Cria a query sparql.
        Query queryExec = QueryFactory.create(query);
        //Prepara a execução da query sparql.
        QueryExecution qe = QueryExecutionFactory.create(queryExec, new DatasetImpl(ModelFactory.createDefaultModel()));
        //Executa a query e obtém o ResultSet como resultado
        ResultSet rs = qe.execSelect();
        
        //Cria a lista de objetos do tipo SparqlResult para receber os resultados.
        List<SparqlResult> itemList = new ArrayList<SparqlResult>();
        //Separa as variáveis utilizadas como resultado para obter seus valores.
        String[] splitResult = results.trim().split(" ");
        
        //Itera sobre o resultSet para obter todos os resultados.
        while(rs.hasNext()) {
            
            //Obtém o QuerySolution com base no resultSet.
            QuerySolution sol = rs.nextSolution();
            
            //Para todas as variáveis de retorno
            for (String variable : splitResult) {
                //Cria o objeto responsável por armazenar o resultado da query
                SparqlResult sr = new SparqlResult();
                
                //Verifica se o retorno foi um Resource
                try {
                    sr.setResourceResult(sol.getResource(variable));
                } catch (ClassCastException e) { }
                
                //Verifica se o retorno foi um Literal
                try {
                    sr.setLiteralResult(sol.getLiteral(variable));
                } catch (ClassCastException e) { }
                
                //Define a variável responsável por este retorno
                sr.setVariable(variable);
                
                //Adiciona este SparqlResult a lista
                itemList.add(sr);
            }
            
        }
        
        //Retorna a lista de resultados
        return itemList;
        
        /*String querySearch = "";
        if (sparqlObject.getUri() != null 
                && !sparqlObject.getUri().isEmpty()) {
            querySearch = sparqlObject.getUri() + " " + 
                          sparqlObject.getPredicate() + 
                          " ?result .";
        } else if (sparqlObject.getObject() != null) {
            querySearch = "?result " + 
                          sparqlObject.getPredicate() + " ";
            if (sparqlObject.getClass().equals(String.class)) {
                querySearch += (String) sparqlObject.getObject();
            } else if (sparqlObject.getClass().equals(Integer.class)) {
                querySearch += (int) sparqlObject.getObject();
            }
            querySearch += " .";
        }
        
        String labelSearch = "";
        labelSearch += sparqlObject.getUri();
        labelSearch += " rdfs:label ?label .";
        
        String filter = "FILTER(langMatches(lang(?label), \"EN\")) ."; 
        
        String query = PREFIXES + 
                "SELECT ?result ?label WHERE {"
                + "SERVICE <http://DBpedia.org/sparql> {"
                + querySearch
                + labelSearch
                + filter
                + "}"
                + "}";
        
        Query queryExec = QueryFactory.create(query);
        QueryExecution qe = QueryExecutionFactory.create(queryExec, new DatasetImpl(ModelFactory.createDefaultModel()));
        ResultSet results = qe.execSelect();
        
        List<SparqlResult> itemList = new ArrayList<SparqlResult>();
        while(results.hasNext()) {
            
            SparqlResult sr = new SparqlResult();
            
            QuerySolution sol = results.nextSolution();
            try {
                sr.setResourceResult(sol.getResource("result"));
            } catch (ClassCastException e) { }
            
            try {
                sr.setLiteralResult(sol.getLiteral("result"));
            } catch (ClassCastException e) { }
            
            sr.setLabelResult(sol.getLiteral("label"));
            itemList.add(sr);
        }
        
        return itemList;*/
    }
    
    /*public static void main(String[] args) {
        //Se a resposta estiver incorreta
        List<SparqlObject> correctAnswerObjects = new ArrayList<SparqlObject>();
        //<URI> ?predicate ?valueCorrect
        SparqlObject line1 = new SparqlObject();
        line1.setUri("<http://dbpedia.org/resource/Florianópolis>");
        line1.setPredicate("dbo:populationTotal");
        line1.setObject("?valueCorrect");
        correctAnswerObjects.add(line1);
        //?valueCorrect foaf:name ?correct
        SparqlObject line2 = new SparqlObject();
        line2.setUri("?valueCorrect");
        line2.setPredicate("foaf:name");
        line2.setObject("?correct");
        line2.setOptional(true);
        correctAnswerObjects.add(line2);
        //?uri rdfs:label answer
        SparqlObject line3 = new SparqlObject();
        line3.setUri("?uri");
        line3.setPredicate("rdfs:label");
        line3.setObject(200000);
        line3.setOptional(true);
        //line3.setObject("200000\"@EN\"");
        correctAnswerObjects.add(line3);
        //?uri dbo:abstract ?description . FILTER langMatches(lang(?description), "en")
        SparqlObject line4 = new SparqlObject();
        line4.setUri("?uri");
        line4.setPredicate("dbo:abstract");
        line4.setObject("?description");
        line4.setOptional(true);
        line4.setFilter("FILTER langMatches(lang(?description), \"en\")");
        correctAnswerObjects.add(line4);
        
        //Executa a consulta
        List<SparqlResult> resultBaseCorrectAnswer = SparqlSearch.create().searchDbpedia(correctAnswerObjects);
        
        System.out.println(resultBaseCorrectAnswer.size());
        
        for (SparqlResult result : resultBaseCorrectAnswer) {
            System.out.print(result.getVariable() + "\t\t - ");
            
            if (result.getLiteralResult() != null) {
                System.out.print(result.getLiteralResult().toString());
            } else if (result.getResourceResult() != null) {
                System.out.print(result.getResourceResult().toString());
            }
            
            System.out.println("");
            
        }
    }*/
    
   /*public static void main(String[] args) {
       //SparqlObject so = new SparqlObject("<http://dbpedia.org/resource/Florianópolis>", "dbo:country");
       SparqlObject so = new SparqlObject("<http://dbpedia.org/resource/Florianópolis>", "dbo:country");
       System.out.println(so.checkUriAndPredicate());
       List<SparqlResult> respostaDBPedia = SparqlSearch.create().searchDbpedia(so);
        if (respostaDBPedia.isEmpty()) {
            System.out.println("Nothing found!");
        } else {
            for (SparqlResult item : respostaDBPedia) {
                if (item.getLiteralResult() != null && item.getLiteralResult().getClass().equals(LiteralImpl.class)) {
                    Literal l = (Literal) item.getLiteralResult();
                    if (l.getValue().getClass().equals(String.class)) {
                        System.out.println(l.getString());
                    }
                }
                if (item.getResourceResult() != null && item.getResourceResult().getClass().equals(ResourceImpl.class)) {
                    Resource r = (Resource) item.getResourceResult();
                    System.out.println(r.getURI());
                }
                
                Literal label = (Literal) item.getLabelResult();
                System.out.println(label.getString());
            }
        }
    }*/
    
}
