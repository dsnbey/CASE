package com.example.acase.Model;

import java.util.List;

public class ResponseModelPinecone {

    private List<Object> results;

    private List<Match> matches;

    private String namespace;

    public ResponseModelPinecone() {
    }

    public List<Object> getResults() {
        return results;
    }

    public void setResults(List<Object> results) {
        this.results = results;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
