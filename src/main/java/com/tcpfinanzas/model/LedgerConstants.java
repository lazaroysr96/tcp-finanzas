package com.tcpfinanzas.model;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LedgerConstants {
    public static final List<String> MONTHS = List.of(
        "ENE", "FEB", "MAR", "ABR", "MAY", "JUN",
        "JUL", "AGO", "SEP", "OCT", "NOV", "DIC"
    );

    public static final Map<String, String> MONTH_LABELS = Stream.of(
        new AbstractMap.SimpleEntry<>("ENE", "Enero"),
        new AbstractMap.SimpleEntry<>("FEB", "Febrero"),
        new AbstractMap.SimpleEntry<>("MAR", "Marzo"),
        new AbstractMap.SimpleEntry<>("ABR", "Abril"),
        new AbstractMap.SimpleEntry<>("MAY", "Mayo"),
        new AbstractMap.SimpleEntry<>("JUN", "Junio"),
        new AbstractMap.SimpleEntry<>("JUL", "Julio"),
        new AbstractMap.SimpleEntry<>("AGO", "Agosto"),
        new AbstractMap.SimpleEntry<>("SEP", "Septiembre"),
        new AbstractMap.SimpleEntry<>("OCT", "Octubre"),
        new AbstractMap.SimpleEntry<>("NOV", "Noviembre"),
        new AbstractMap.SimpleEntry<>("DIC", "Diciembre")
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
}