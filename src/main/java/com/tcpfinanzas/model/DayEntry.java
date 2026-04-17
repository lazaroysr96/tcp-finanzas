package com.tcpfinanzas.model;

public class DayEntry {
    public String dia;
    public String importe;
    public String nota;

    public DayEntry(String dia, String importe) {
        this.dia = dia;
        this.importe = importe;
    }

    public DayEntry(String dia, String importe, String nota) {
        this.dia = dia;
        this.importe = importe;
        this.nota = nota;
    }
}