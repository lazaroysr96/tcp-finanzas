package com.tcpfinanzas.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Registro {
    public GeneralesData generales;
    public Map<String, List<DayEntry>> ingresos;
    public Map<String, List<DayEntry>> gastos;
    public List<TributoRow> tributos;

    public Registro() {
        this.generales = new GeneralesData();
        this.ingresos = new HashMap<>();
        this.gastos = new HashMap<>();
        this.tributos = new ArrayList<>();
        for (String month : LedgerConstants.MONTHS) {
            this.ingresos.put(month, new ArrayList<>());
            this.gastos.put(month, new ArrayList<>());
            this.tributos.add(new TributoRow());
        }
    }
}