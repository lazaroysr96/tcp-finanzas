package com.tcpfinanzas.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tcpfinanzas.model.DayEntry;
import com.tcpfinanzas.model.GeneralesData;
import com.tcpfinanzas.model.LedgerConstants;
import com.tcpfinanzas.model.Registro;
import com.tcpfinanzas.model.TributoRow;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LedgerRepository {
    private static LedgerRepository instance;
    private static final String DATA_FILE = "tcp_finanzas_data.json";

    private Registro registro;
    private Map<String, Runnable> listeners;
    private Gson gson;

    private LedgerRepository() {
        this.registro = new Registro();
        this.listeners = new HashMap<>();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadFromFile();
    }

    public static LedgerRepository getInstance() {
        if (instance == null) {
            instance = new LedgerRepository();
        }
        return instance;
    }

    public Registro getRegistro() {
        return registro;
    }

    public void addListener(String key, Runnable listener) {
        listeners.put(key, listener);
    }

    public void removeListener(String key) {
        listeners.remove(key);
    }

    private void notifyListeners() {
        for (Runnable listener : listeners.values()) {
            if (listener != null) {
                listener.run();
            }
        }
    }

    public void saveToFile() {
        try (FileWriter writer = new FileWriter(DATA_FILE)) {
            gson.toJson(registro, writer);
            System.out.println("Datos guardados en " + DATA_FILE);
        } catch (IOException e) {
            System.err.println("Error al guardar: " + e.getMessage());
        }
    }

    public void loadFromFile() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Registro loaded = gson.fromJson(reader, Registro.class);
                if (loaded != null) {
                    this.registro = loaded;
                    System.out.println("Datos cargados desde " + DATA_FILE);
                }
            } catch (IOException e) {
                System.err.println("Error al cargar: " + e.getMessage());
            }
        }
    }

    public void exportToPdf(String filename) {
        try {
            PdfWriter writer = new PdfWriter(filename);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            var generales = registro.generales;
            document.add(new Paragraph("DECLARACIÓN JURADA")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Año: " + generales.anio)
                .setFontSize(12));
            document.add(new Paragraph("Nombre: " + generales.nombre));
            document.add(new Paragraph("NIT: " + generales.nit));
            document.add(new Paragraph("Actividad: " + generales.actividad));
            document.add(new Paragraph("Código ONAT: " + generales.codigo));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("DOMICILIO FISCAL")
                .setFontSize(14).setBold());
            document.add(new Paragraph(generales.fiscalCalle + ", " +
                generales.fiscalMunicipio + ", " + generales.fiscalProvincia));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("RESUMEN ANUAL")
                .setFontSize(14).setBold());
            document.add(new Paragraph("Total Ingresos: " +
                String.format("%.2f", getTotalIngresos()) + " CUP"));
            document.add(new Paragraph("Total Gastos: " +
                String.format("%.2f", getTotalGastos()) + " CUP"));
            document.add(new Paragraph("Total Tributos: " +
                String.format("%.2f", getTotalTributos()) + " CUP"));
            document.add(new Paragraph("Otros Gastos: " +
                String.format("%.2f", getTotalOtros()) + " CUP"));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("INGRESOS POR MES")
                .setFontSize(14).setBold());
            Table tableIngresos = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
            tableIngresos.setWidth(UnitValue.createPercentValue(100));
            tableIngresos.addHeaderCell("Mes");
            tableIngresos.addHeaderCell("Importe");

            Map<String, Double> monthlyIngresos = getMonthlyIngresos();
            for (String month : LedgerConstants.MONTHS) {
                tableIngresos.addCell(LedgerConstants.MONTH_LABELS.getOrDefault(month, month));
                tableIngresos.addCell(String.format("%.2f", monthlyIngresos.getOrDefault(month, 0.0)) + " CUP");
            }
            tableIngresos.addCell("TOTAL");
            tableIngresos.addCell(String.format("%.2f", getTotalIngresos()) + " CUP");
            document.add(tableIngresos);
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("GASTOS POR MES")
                .setFontSize(14).setBold());
            Table tableGastos = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
            tableGastos.setWidth(UnitValue.createPercentValue(100));
            tableGastos.addHeaderCell("Mes");
            tableGastos.addHeaderCell("Importe");

            Map<String, Double> monthlyGastos = getMonthlyGastos();
            for (String month : LedgerConstants.MONTHS) {
                tableGastos.addCell(LedgerConstants.MONTH_LABELS.getOrDefault(month, month));
                tableGastos.addCell(String.format("%.2f", monthlyGastos.getOrDefault(month, 0.0)) + " CUP");
            }
            tableGastos.addCell("TOTAL");
            tableGastos.addCell(String.format("%.2f", getTotalGastos()) + " CUP");
            document.add(tableGastos);
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("TRIBUTOS")
                .setFontSize(14).setBold());
            Table tableTributos = new Table(UnitValue.createPercentArray(new float[]{2, 1, 1, 1, 1, 1, 1, 1}));
            tableTributos.setWidth(UnitValue.createPercentValue(100));
            tableTributos.addHeaderCell("Mes");
            tableTributos.addHeaderCell("Ventas");
            tableTributos.addHeaderCell("Fuerza");
            tableTributos.addHeaderCell("Sellos");
            tableTributos.addHeaderCell("CSS 20%");
            tableTributos.addHeaderCell("CSS 14%");
            tableTributos.addHeaderCell("Anuncios");
            tableTributos.addHeaderCell("Otros");

            for (int i = 0; i < LedgerConstants.MONTHS.size() && i < registro.tributos.size(); i++) {
                TributoRow t = registro.tributos.get(i);
                tableTributos.addCell(LedgerConstants.MONTH_LABELS.getOrDefault(LedgerConstants.MONTHS.get(i), ""));
                tableTributos.addCell(t.ventas);
                tableTributos.addCell(t.fuerza);
                tableTributos.addCell(t.sellos);
                tableTributos.addCell(t.css20);
                tableTributos.addCell(t.css14);
                tableTributos.addCell(t.anuncios);
                tableTributos.addCell(t.otros);
            }
            document.add(tableTributos);

            document.close();
            System.out.println("PDF generado: " + filename);
        } catch (Exception e) {
            System.err.println("Error al generar PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateGenerales(GeneralesData generales) {
        registro.generales = generales;
        notifyListeners();
    }

    public void addIngreso(String month, int dia, double importe) {
        List<DayEntry> entries = registro.ingresos.get(month);
        if (entries == null) {
            entries = new ArrayList<>();
            registro.ingresos.put(month, entries);
        }
        entries.add(new DayEntry(String.valueOf(dia), String.format("%.2f", importe)));
        notifyListeners();
    }

    public void addGasto(String month, int dia, double importe) {
        List<DayEntry> entries = registro.gastos.get(month);
        if (entries == null) {
            entries = new ArrayList<>();
            registro.gastos.put(month, entries);
        }
        entries.add(new DayEntry(String.valueOf(dia), String.format("%.2f", importe)));
        notifyListeners();
    }

    public void deleteIngreso(String month, int dia) {
        List<DayEntry> entries = registro.ingresos.get(month);
        if (entries != null) {
            entries.removeIf(e -> e.dia.equals(String.valueOf(dia)));
            notifyListeners();
        }
    }

    public void deleteGasto(String month, int dia) {
        List<DayEntry> entries = registro.gastos.get(month);
        if (entries != null) {
            entries.removeIf(e -> e.dia.equals(String.valueOf(dia)));
            notifyListeners();
        }
    }

    public void editIngreso(String month, int oldDia, int newDia, double importe) {
        List<DayEntry> entries = registro.ingresos.get(month);
        if (entries != null) {
            entries.removeIf(e -> e.dia.equals(String.valueOf(oldDia)));
            entries.add(new DayEntry(String.valueOf(newDia), String.format("%.2f", importe)));
            notifyListeners();
        }
    }

    public void editGasto(String month, int oldDia, int newDia, double importe) {
        List<DayEntry> entries = registro.gastos.get(month);
        if (entries != null) {
            entries.removeIf(e -> e.dia.equals(String.valueOf(oldDia)));
            entries.add(new DayEntry(String.valueOf(newDia), String.format("%.2f", importe)));
            notifyListeners();
        }
    }

    public void updateTributos(String month, TributoRow tributo) {
        int index = LedgerConstants.MONTHS.indexOf(month);
        if (index >= 0 && index < registro.tributos.size()) {
            registro.tributos.set(index, tributo);
            notifyListeners();
        }
    }

    public double getTotalIngresos() {
        return registro.ingresos.values().stream()
            .flatMap(List::stream)
            .mapToDouble(e -> Double.parseDouble(e.importe))
            .sum();
    }

    public double getTotalGastos() {
        return registro.gastos.values().stream()
            .flatMap(List::stream)
            .mapToDouble(e -> Double.parseDouble(e.importe))
            .sum();
    }

    public double getTotalTributos() {
        return registro.tributos.stream()
            .mapToDouble(TributoRow::getTotalTributos)
            .sum();
    }

    public double getTotalOtros() {
        return registro.tributos.stream()
            .mapToDouble(TributoRow::getTotalOtros)
            .sum();
    }

    public Map<String, Double> getMonthlyIngresos() {
        Map<String, Double> result = new HashMap<>();
        for (String month : LedgerConstants.MONTHS) {
            List<DayEntry> entries = registro.ingresos.get(month);
            double total = entries.stream()
                .mapToDouble(e -> Double.parseDouble(e.importe))
                .sum();
            result.put(month, total);
        }
        return result;
    }

    public Map<String, Double> getMonthlyGastos() {
        Map<String, Double> result = new HashMap<>();
        for (String month : LedgerConstants.MONTHS) {
            List<DayEntry> entries = registro.gastos.get(month);
            double total = entries.stream()
                .mapToDouble(e -> Double.parseDouble(e.importe))
                .sum();
            result.put(month, total);
        }
        return result;
    }
}