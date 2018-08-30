package jason.semanticWeb;

/**
 * 
 * Classe responsável por armazenar as informações referentes a uma tupla RDF.
 * 
 * @author felipe.demarchi
 *
 */
public class SparqlObject {

    /*
     * Os atributos podem receber um valor de acordo com o seu identificador uma uma variável, a qual deverá iniciar com uma ?
     */
    private String uri;
    private String predicate;
    private Object object;
    private boolean optional;
    private String filter;

    /**
     * Construtor Default.
     */
    public SparqlObject() {
        this.optional = false;
        this.filter = null;
    }

    /**
     * Construtor que inicializa o objeto com uma URI e um Predicate para descobrir o Object.
     * 
     * @param uri URI a ser utilizada na consulta.
     * @param predicate Predicado a ser utilizado na consulta.
     */
    public SparqlObject(String uri, String predicate) {
        this();
        this.uri = uri;
        this.predicate = predicate;
    }

    /**
     * Construtor que inicializa o objeto com um Predicate e um Object para descobrir a URI.
     * 
     * @param predicate Predicado a ser utilizado na consulta.
     * @param object Objeto a ser utilizado na consulta.
     */
    public SparqlObject(String predicate, Object object) {
        this();
        this.predicate = predicate;
        this.object = object;
    }

    /*Getters and Setters*/
    
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
    
    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean isOptional) {
        this.optional = isOptional;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * Método que verifica se a URI e o predicado foram setados.
     * 
     * @return Retorna verdadeiro caso ambos tenam sido setados.
     */
    public boolean checkUriAndPredicate() {
        return this.uri != null && !this.uri.isEmpty() && this.predicate != null && !this.predicate.isEmpty();
    }
    
    /**
     * Sobrescrita do método toString para auxiliar na construção de uma query sparql.
     */
    @Override
    public String toString() {
        if (optional && filter != null && !filter.trim().isEmpty()) {
            return "OPTIONAL { " + uri + " " + predicate + " " + object + " . " + filter + " } .";
        } else if (optional) {
            return "OPTIONAL { " + uri + " " + predicate + " " + object + " } .";
        } else if (filter != null && !filter.trim().isEmpty()) {
            return uri + " " + predicate + " " + object + " . " + filter + " ."; 
        }
        return uri + " " + predicate + " " + object + " . ";
    }
    
    /**
     * Verifica se a URI foi definida como uma variável.
     * 
     * @return verdadeiro caso a URI seja uma variável.
     */
    public boolean isUriVariable() {
        if (uri != null && uri.startsWith("?")) {
            return true;
        }
        return false;
    }
    
    /**
     * Verifica se o Predicado foi definido como uma variável.
     * 
     * @return verdadeiro caso o Predicado seja uma variável.
     */
    public boolean isPredicateVariable() {
        if (predicate != null && predicate.startsWith("?")) {
            return true;
        }
        return false;
    }
    
    /**
     * Verifica se a Objecto foi definido como uma variável.
     * 
     * @return verdadeiro caso o Objeto seja uma variável.
     */
    public boolean isObjectVariable() {
        if (object != null && object.getClass().equals(String.class)) {
            String s = (String) object;
            if (s.startsWith("?")) {
                return true;
            }
        }
        return false;
    }

}
