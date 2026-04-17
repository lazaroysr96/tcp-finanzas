package com.tcpfinanzas.model;

public class CnaeItem {
    public String section;
    public String structure;
    public String code;
    public String description;
    public String notes;
    public String correlations;

    public CnaeItem() {}

    public CnaeItem(String section, String structure, String code, String description) {
        this.section = section;
        this.structure = structure;
        this.code = code;
        this.description = description;
    }
}