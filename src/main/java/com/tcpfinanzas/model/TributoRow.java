package com.tcpfinanzas.model;

public class TributoRow {
    public String mes;
    public String ventas;
    public String fuerza;
    public String sellos;
    public String anuncios;
    public String css20;
    public String css14;
    public String otros;
    public String restauracion;
    public String arrendamiento;
    public String exonerado;
    public String otrosMFP;
    public String cuotaMensual;

    public TributoRow() {
        this.mes = "";
        this.ventas = "";
        this.fuerza = "";
        this.sellos = "";
        this.anuncios = "";
        this.css20 = "";
        this.css14 = "";
        this.otros = "";
        this.restauracion = "";
        this.arrendamiento = "";
        this.exonerado = "";
        this.otrosMFP = "";
        this.cuotaMensual = "";
    }

    public TributoRow(String mes, String ventas, String fuerza, String sellos, String anuncios,
                    String css20, String css14, String otros, String restauracion,
                    String arrendamiento, String exonerado, String otrosMFP, String cuotaMensual) {
        this.mes = mes;
        this.ventas = ventas;
        this.fuerza = fuerza;
        this.sellos = sellos;
        this.anuncios = anuncios;
        this.css20 = css20;
        this.css14 = css14;
        this.otros = otros;
        this.restauracion = restauracion;
        this.arrendamiento = arrendamiento;
        this.exonerado = exonerado;
        this.otrosMFP = otrosMFP;
        this.cuotaMensual = cuotaMensual;
    }

    public double getTotalTributos() {
        return parse(ventas) + parse(fuerza) + parse(sellos) + parse(anuncios) + parse(css20) + parse(css14) + parse(otros);
    }

    public double getTotalOtros() {
        return parse(cuotaMensual) + parse(arrendamiento) + parse(restauracion) + parse(exonerado) + parse(otrosMFP);
    }

    private double parse(String value) {
        if (value == null || value.isEmpty()) return 0;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}