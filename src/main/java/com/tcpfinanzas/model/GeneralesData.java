package com.tcpfinanzas.model;

public class GeneralesData {
    public String nombre;
    public String nit;
    public int anio;
    public String actividad;
    public String codigo;
    public String fiscalCalle;
    public String fiscalMunicipio;
    public String fiscalProvincia;
    public String legalCalle;
    public String legalMunicipio;
    public String legalProvincia;

    public GeneralesData() {
        this.nombre = "";
        this.nit = "";
        this.anio = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        this.actividad = "";
        this.codigo = "";
        this.fiscalCalle = "";
        this.fiscalMunicipio = "";
        this.fiscalProvincia = "";
        this.legalCalle = "";
        this.legalMunicipio = "";
        this.legalProvincia = "";
    }

    public GeneralesData(String nombre, String nit, int anio, String actividad, String codigo,
                        String fiscalCalle, String fiscalMunicipio, String fiscalProvincia,
                        String legalCalle, String legalMunicipio, String legalProvincia) {
        this.nombre = nombre;
        this.nit = nit;
        this.anio = anio;
        this.actividad = actividad;
        this.codigo = codigo;
        this.fiscalCalle = fiscalCalle;
        this.fiscalMunicipio = fiscalMunicipio;
        this.fiscalProvincia = fiscalProvincia;
        this.legalCalle = legalCalle;
        this.legalMunicipio = legalMunicipio;
        this.legalProvincia = legalProvincia;
    }
}