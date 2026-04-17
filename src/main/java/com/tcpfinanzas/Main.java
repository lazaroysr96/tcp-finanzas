package com.tcpfinanzas;

import com.tcpfinanzas.data.LedgerRepository;
import com.tcpfinanzas.model.DayEntry;
import com.tcpfinanzas.model.GeneralesData;
import com.tcpfinanzas.model.LedgerConstants;
import com.tcpfinanzas.model.Registro;
import com.tcpfinanzas.model.TributoRow;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main extends Application {

    private LedgerRepository repository;
    private BorderPane root;
    private VBox contentPane;
    private NavMenu currentNavMenu = NavMenu.INGRESOS;
    private Set<String> expandedMonths = new HashSet<>();
    private Stage mainStage;
    private double xOffset = 0;
    private double yOffset = 0;

    private enum NavMenu {
        GENERALES, INGRESOS, GASTOS, IMPUESTOS, NOMENCLADOR
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        repository = LedgerRepository.getInstance();
        repository.addListener("main", this::refreshContent);

        mainStage = primaryStage;
        primaryStage.setTitle("TCP Finanzas");

        root = new BorderPane();

        MenuBar menuBar = createMenuBar();
        HBox topBar = new HBox(menuBar);
        topBar.setStyle("-fx-background-color: #e0e0e0;");

        HBox header = createHeader();
        VBox menu = new VBox(topBar, header);

        root.setTop(menu);

        VBox navMenu = createMenu();
        root.setLeft(navMenu);

        contentPane = new VBox();
        contentPane.setPadding(new Insets(16));
        contentPane.setSpacing(8);
        contentPane.setStyle("-fx-background-color: #fafafa;");

        ScrollPane scroll = new ScrollPane(contentPane);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #fafafa;");

        root.setCenter(scroll);

        refreshContent();

        Scene scene = new Scene(root, 900, 700);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu menuArchivo = new Menu("Archivo");
        MenuItem guardar = new MenuItem("Guardar");
        guardar.setOnAction(e -> repository.saveToFile());
        MenuItem exportarPdf = new MenuItem("Exportar PDF");
        exportarPdf.setOnAction(e -> {
            String filename = "declaracion_jurada_" + repository.getRegistro().generales.anio + ".pdf";
            repository.exportToPdf(filename);
            showAlert("PDF Generado", "El archivo se guardó como: " + filename);
        });
        MenuItem salir = new MenuItem("Salir");
        salir.setOnAction(e -> mainStage.close());
        menuArchivo.getItems().addAll(guardar, exportarPdf, new SeparatorMenuItem(), salir);

        Menu menuModulos = new Menu("Módulos");
        MenuItem declaraciones = new MenuItem("Declaración Jurada");
        declaraciones.setOnAction(e -> {
            currentNavMenu = NavMenu.INGRESOS;
            expandedMonths = new HashSet<>();
            refreshContent();
        });
        MenuItem nomenclador = new MenuItem("Nomenclador");
        nomenclador.setOnAction(e -> {
            currentNavMenu = NavMenu.NOMENCLADOR;
            refreshContent();
        });
        menuModulos.getItems().addAll(declaraciones, nomenclador);

        Menu menuAyuda = new Menu("Acerca de");
        MenuItem acercaDe = new MenuItem("Acerca de");
        acercaDe.setOnAction(e -> showAlert("Acerca de",
            "TCP Finanzas - Declaración Jurada\n\nVersión 1.0\n\nAplicación para gestión de declaración jurada."));
        menuAyuda.getItems().add(acercaDe);

        menuBar.getMenus().addAll(menuArchivo, menuModulos, menuAyuda);
        return menuBar;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(10));
        header.setSpacing(15);
        header.setStyle("-fx-background-color: #1976d2;");
        header.setPrefHeight(50);

        Label title = new Label("Declaración Jurada");
        title.setFont(new Font(18));
        title.setStyle("-fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button saveBtn = new Button("Guardar");
        saveBtn.setStyle("-fx-background-color: white; -fx-text-fill: #1976d2; -fx-cursor: hand;");
        saveBtn.setPrefWidth(80);
        saveBtn.setOnAction(e -> repository.saveToFile());

        Button printBtn = new Button("PDF");
        printBtn.setStyle("-fx-background-color: white; -fx-text-fill: #1976d2; -fx-cursor: hand;");
        printBtn.setPrefWidth(60);
        printBtn.setOnAction(e -> {
            String filename = "declaracion_jurada_" + repository.getRegistro().generales.anio + ".pdf";
            repository.exportToPdf(filename);
            showAlert("PDF Generado", "El archivo se guardó como: " + filename);
        });

        header.getChildren().addAll(title, spacer, saveBtn, printBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        return header;
    }

    private VBox createMenu() {
        VBox menu = new VBox();
        menu.setPadding(new Insets(10));
        menu.setSpacing(5);
        menu.setStyle("-fx-background-color: #e3f2fd;");
        menu.setPrefWidth(160);

        String[] options = {"Datos Generales", "Ingresos", "Gastos", "Impuestos"};

        for (String option : options) {
            Button btn = new Button(option);
            btn.setPrefWidth(140);
            btn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white;");
            btn.setOnAction(e -> {
                if (option.equals("Datos Generales")) {
                    currentNavMenu = NavMenu.GENERALES;
                } else if (option.equals("Ingresos")) {
                    currentNavMenu = NavMenu.INGRESOS;
                    expandedMonths = new HashSet<>();
                } else if (option.equals("Gastos")) {
                    currentNavMenu = NavMenu.GASTOS;
                    expandedMonths = new HashSet<>();
                } else if (option.equals("Impuestos")) {
                    currentNavMenu = NavMenu.IMPUESTOS;
                }
                refreshContent();
            });
            menu.getChildren().add(btn);
        }

        return menu;
    }

    private void refreshContent() {
        contentPane.getChildren().clear();
        Registro registro = repository.getRegistro();

        switch (currentNavMenu) {
            case GENERALES:
                showGenerales();
                break;
            case INGRESOS:
                showIngresos(registro);
                break;
            case GASTOS:
                showGastos(registro);
                break;
            case IMPUESTOS:
                showImpuestos(registro);
                break;
            case NOMENCLADOR:
                showNomenclador();
                break;
        }
    }

    private TextField fieldNombre, fieldNit, fieldAnio, fieldActividad, fieldCodigo;
    private TextField fieldFiscalCalle, fieldFiscalMunicipio, fieldFiscalProvincia;
    private TextField fieldLegalCalle, fieldLegalMunicipio, fieldLegalProvincia;

    private void showGenerales() {
        Registro registro = repository.getRegistro();
        var generales = registro.generales;

        Label title = new Label("Datos del Contribuyente");
        title.setFont(new Font(24));
        contentPane.getChildren().add(title);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white; -fx-border-color: #ccc;");

        fieldNombre = new TextField(generales.nombre);
        fieldNit = new TextField(generales.nit);
        fieldAnio = new TextField(String.valueOf(generales.anio));
        fieldActividad = new TextField(generales.actividad);
        fieldCodigo = new TextField(generales.codigo);

        fieldFiscalCalle = new TextField(generales.fiscalCalle);
        fieldFiscalMunicipio = new TextField(generales.fiscalMunicipio);
        fieldFiscalProvincia = new TextField(generales.fiscalProvincia);

        fieldLegalCalle = new TextField(generales.legalCalle);
        fieldLegalMunicipio = new TextField(generales.legalMunicipio);
        fieldLegalProvincia = new TextField(generales.legalProvincia);

        Label header1 = new Label("Datos del Contribuyente");
        header1.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        grid.add(header1, 0, 0, 2, 1);

        grid.add(new Label("Nombre completo:"), 0, 1);
        grid.add(fieldNombre, 1, 1);

        grid.add(new Label("NIT:"), 0, 2);
        grid.add(fieldNit, 1, 2);

        grid.add(new Label("Año:"), 0, 3);
        grid.add(fieldAnio, 1, 3);

        grid.add(new Label("Actividad económica:"), 0, 4);
        grid.add(fieldActividad, 1, 4);

        grid.add(new Label("Código ONAT:"), 0, 5);
        grid.add(fieldCodigo, 1, 5);

        Label header2 = new Label("Domicilio Fiscal");
        header2.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        grid.add(header2, 0, 6, 2, 1);

        grid.add(new Label("Calle, número:"), 0, 7);
        grid.add(fieldFiscalCalle, 1, 7);

        grid.add(new Label("Municipio:"), 0, 8);
        grid.add(fieldFiscalMunicipio, 1, 8);

        grid.add(new Label("Provincia:"), 0, 9);
        grid.add(fieldFiscalProvincia, 1, 9);

        Label header3 = new Label("Domicilio Legal (según CI)");
        header3.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        grid.add(header3, 0, 10, 2, 1);

        grid.add(new Label("Calle, número, apt."), 0, 11);
        grid.add(fieldLegalCalle, 1, 11);

        grid.add(new Label("Municipio:"), 0, 12);
        grid.add(fieldLegalMunicipio, 1, 12);

        grid.add(new Label("Provincia:"), 0, 13);
        grid.add(fieldLegalProvincia, 1, 13);

        contentPane.getChildren().add(grid);

        Button saveBtn = new Button("Guardar");
        saveBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10 30;");
        saveBtn.setOnAction(e -> {
            try {
                var generalesData = new com.tcpfinanzas.model.GeneralesData(
                    fieldNombre.getText(),
                    fieldNit.getText(),
                    Integer.parseInt(fieldAnio.getText()),
                    fieldActividad.getText(),
                    fieldCodigo.getText(),
                    fieldFiscalCalle.getText(),
                    fieldFiscalMunicipio.getText(),
                    fieldFiscalProvincia.getText(),
                    fieldLegalCalle.getText(),
                    fieldLegalMunicipio.getText(),
                    fieldLegalProvincia.getText()
                );
                repository.updateGenerales(generalesData);
                repository.saveToFile();
                showAlert("Guardado", "Datos guardados correctamente");
            } catch (NumberFormatException ex) {
                showAlert("Error", "El año debe ser un número válido");
            }
        });
        contentPane.getChildren().add(saveBtn);
    }

    private void showIngresos(Registro registro) {
        Label title = new Label("Ingresos");
        title.setFont(new Font(24));
        contentPane.getChildren().add(title);

        double total = repository.getTotalIngresos();
        Label totalLabel = new Label("Total: " + String.format("%.2f", total) + " CUP");
        totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        contentPane.getChildren().add(totalLabel);

        contentPane.getChildren().add(new Separator());

        Map<String, List<DayEntry>> entries = registro.ingresos;
        for (String month : LedgerConstants.MONTHS) {
            VBox monthCard = createMonthCard(month, entries.get(month), true);
            contentPane.getChildren().add(monthCard);
        }
    }

    private void showGastos(Registro registro) {
        Label title = new Label("Gastos");
        title.setFont(new Font(24));
        contentPane.getChildren().add(title);

        double total = repository.getTotalGastos();
        Label totalLabel = new Label("Total: " + String.format("%.2f", total) + " CUP");
        totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        contentPane.getChildren().add(totalLabel);

        contentPane.getChildren().add(new Separator());

        Map<String, List<DayEntry>> entries = registro.gastos;
        for (String month : LedgerConstants.MONTHS) {
            VBox monthCard = createMonthCard(month, entries.get(month), false);
            contentPane.getChildren().add(monthCard);
        }
    }

    private VBox createMonthCard(String month, List<DayEntry> entries, boolean isIngreso) {
        VBox card = new VBox();
        card.setPadding(new Insets(12));
        card.setSpacing(8);
        card.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 1;");
        card.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(card, Priority.ALWAYS);

        double total = entries.stream()
            .mapToDouble(e -> Double.parseDouble(e.importe))
            .sum();

        String monthLabel = LedgerConstants.MONTH_LABELS.getOrDefault(month, month);
        Label header = new Label(monthLabel + " - " + String.format("%.2f", total) + " CUP");
        header.setFont(new Font(16));
        header.setStyle("-fx-font-weight: bold;");

        boolean isExpanded = expandedMonths.contains(month);
        boolean hasEntries = !entries.isEmpty();

        VBox entriesBox = new VBox();
        entriesBox.setPadding(new Insets(8, 0, 8, 0));
        entriesBox.setSpacing(4);

        if (isExpanded && hasEntries) {
            for (DayEntry entry : entries) {
                HBox entryRow = new HBox();
                entryRow.setSpacing(16);
                entryRow.setAlignment(Pos.CENTER_LEFT);

                Label diaLabel = new Label("Día " + entry.dia);
                Label importeLabel = new Label(entry.importe + " CUP");
                importeLabel.setStyle("-fx-font-weight: bold;");

                Label notaLabel = new Label(entry.nota != null && !entry.nota.isEmpty() ? entry.nota : "");
                notaLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

                Button editBtn = new Button("E");
                editBtn.setPrefWidth(30);
                editBtn.setOnAction(e -> showEntryDialog(month, entry, isIngreso));

                Button deleteBtn = new Button("X");
                deleteBtn.setPrefWidth(30);
                deleteBtn.setOnAction(e -> {
                    if (isIngreso) {
                        repository.deleteIngreso(month, Integer.parseInt(entry.dia));
                    } else {
                        repository.deleteGasto(month, Integer.parseInt(entry.dia));
                    }
                });

                VBox entryBox = new VBox();
                entryBox.setSpacing(2);

                HBox row1 = new HBox();
                row1.setSpacing(16);
                row1.setAlignment(Pos.CENTER_LEFT);
                row1.getChildren().addAll(diaLabel, importeLabel, editBtn, deleteBtn);

                if (entry.nota != null && !entry.nota.isEmpty()) {
                    Label notaLabelSmall = new Label("Nota: " + entry.nota);
                    notaLabelSmall.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
                    entryBox.getChildren().addAll(row1, notaLabelSmall);
                } else {
                    entryBox.getChildren().add(row1);
                }

                entriesBox.getChildren().add(entryBox);
                entriesBox.getChildren().add(entryRow);
            }
        } else if (!hasEntries) {
            Label emptyLabel = new Label("Sin registros");
            emptyLabel.setStyle("-fx-text-fill: gray;");
            entriesBox.getChildren().add(emptyLabel);
        }

        Button expandBtn = new Button(hasEntries ? (isExpanded ? "▼" : "▶") : "");
        expandBtn.setPrefWidth(30);
        expandBtn.setOnAction(e -> {
            if (expandedMonths.contains(month)) {
                expandedMonths.remove(month);
            } else {
                expandedMonths.add(month);
            }
            refreshContent();
        });
        expandBtn.setDisable(!hasEntries);

        Button addBtn = new Button("+ Agregar");
        addBtn.setOnAction(e -> showEntryDialog(month, null, isIngreso));

        HBox actions = new HBox();
        actions.setSpacing(8);
        actions.getChildren().addAll(expandBtn, addBtn);

        card.getChildren().addAll(header, entriesBox, actions);

        return card;
    }

    private void showNomenclador() {
        Label title = new Label("Nomenclador");
        title.setFont(new Font(24));
        contentPane.getChildren().add(title);

        Label info = new Label("Consultas de referencia para CNAE y nomenclador contable.");
        contentPane.getChildren().add(info);

        HBox searchRow = new HBox();
        searchRow.setSpacing(10);

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar por código, descripción o estructura");
        searchField.setPrefWidth(300);

        Button buscarBtn = new Button("Buscar");
        buscarBtn.setOnAction(e -> {
            showAlert("Buscar", "Funcionalidad de búsqueda en desarrollo.\nBuscar: " + searchField.getText());
        });

        Button limpiarBtn = new Button("Limpiar");
        limpiarBtn.setOnAction(e -> searchField.setText(""));

        searchRow.getChildren().addAll(searchField, buscarBtn, limpiarBtn);
        contentPane.getChildren().add(searchRow);

        HBox filterRow = new HBox();
        filterRow.setSpacing(10);

        ToggleButton cnaeBtn = new ToggleButton("CNAE");
        ToggleButton contabBtn = new ToggleButton("Contabilidad");
        cnaeBtn.setSelected(true);

        filterRow.getChildren().addAll(cnaeBtn, contabBtn);
        contentPane.getChildren().add(filterRow);
    }

    private void showImpuestos(Registro registro) {
        Label title = new Label("Tributos y Otros Gastos");
        title.setFont(new Font(24));
        contentPane.getChildren().add(title);

        ComboBox<String> monthCombo = new ComboBox<>();
        monthCombo.getItems().addAll(LedgerConstants.MONTHS);
        monthCombo.setValue(LedgerConstants.MONTHS.get(0));
        contentPane.getChildren().add(monthCombo);

        contentPane.getChildren().add(new Separator());

        Label tribTitle = new Label("Tributos Pagados (Deducibles)");
        tribTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        contentPane.getChildren().add(tribTitle);

        GridPane gridTributos = new GridPane();
        gridTributos.setHgap(10);
        gridTributos.setVgap(10);
        gridTributos.setPadding(new Insets(10));
        gridTributos.setStyle("-fx-background-color: white; -fx-border-color: #ccc;");
        gridTributos.setPrefWidth(600);

        TextField fieldVentas = new TextField();
        TextField fieldFuerza = new TextField();
        TextField fieldSellos = new TextField();
        TextField fieldAnuncios = new TextField();
        TextField fieldCss20 = new TextField();
        TextField fieldCss14 = new TextField();
        TextField fieldOtros = new TextField();

        gridTributos.add(new Label("Impuesto Ventas/Servicios (10%):"), 0, 0);
        gridTributos.add(fieldVentas, 1, 0);
        gridTributos.add(new Label("Imp. Fuerza de Trabajo:"), 0, 1);
        gridTributos.add(fieldFuerza, 1, 1);
        gridTributos.add(new Label("Imp. Documentos y Sellos:"), 0, 2);
        gridTributos.add(fieldSellos, 1, 2);
        gridTributos.add(new Label("Tasa Radicación Anuncios:"), 0, 3);
        gridTributos.add(fieldAnuncios, 1, 3);
        gridTributos.add(new Label("Contribución Seg. Social (20%):"), 0, 4);
        gridTributos.add(fieldCss20, 1, 4);
        gridTributos.add(new Label("Contribución Seguridad Social (14%):"), 0, 5);
        gridTributos.add(fieldCss14, 1, 5);
        gridTributos.add(new Label("Otros Tributos:"), 0, 6);
        gridTributos.add(fieldOtros, 1, 6);

        contentPane.getChildren().add(gridTributos);

        Label otrosTitle = new Label("Otros Gastos Deducibles");
        otrosTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        contentPane.getChildren().add(otrosTitle);

        GridPane gridOtros = new GridPane();
        gridOtros.setHgap(10);
        gridOtros.setVgap(10);
        gridOtros.setPadding(new Insets(10));
        gridOtros.setStyle("-fx-background-color: white; -fx-border-color: #ccc;");
        gridOtros.setPrefWidth(600);

        TextField fieldCuota = new TextField();
        TextField fieldArrendamiento = new TextField();
        TextField fieldRestauracion = new TextField();
        TextField fieldExonerado = new TextField();
        TextField fieldOtrosMFP = new TextField();

        gridOtros.add(new Label("Cuota Mensual (5%):"), 0, 0);
        gridOtros.add(fieldCuota, 1, 0);
        gridOtros.add(new Label("Pago Arrendamiento (Estado):"), 0, 1);
        gridOtros.add(fieldArrendamiento, 1, 1);
        gridOtros.add(new Label("Contribución Restauración:"), 0, 2);
        gridOtros.add(fieldRestauracion, 1, 2);
        gridOtros.add(new Label("Exonerado (Reparaciones):"), 0, 3);
        gridOtros.add(fieldExonerado, 1, 3);
        gridOtros.add(new Label("Otros Gastos (MFP):"), 0, 4);
        gridOtros.add(fieldOtrosMFP, 1, 4);

        contentPane.getChildren().add(gridOtros);

        Button saveBtn = new Button("Guardar");
        saveBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10 30;");
        saveBtn.setOnAction(e -> {
            String month = monthCombo.getValue();
            TributoRow tributo = new TributoRow(
                month,
                getText(fieldVentas),
                getText(fieldFuerza),
                getText(fieldSellos),
                getText(fieldAnuncios),
                getText(fieldCss20),
                getText(fieldCss14),
                getText(fieldOtros),
                getText(fieldRestauracion),
                getText(fieldArrendamiento),
                getText(fieldExonerado),
                getText(fieldOtrosMFP),
                getText(fieldCuota)
            );
            repository.updateTributos(month, tributo);
            showAlert("Guardado", "Tributos guardados correctamente");
        });
        contentPane.getChildren().add(saveBtn);
    }

    private String getText(TextField field) {
        return field.getText() != null ? field.getText() : "";
    }

    private void showEntryDialog(String month, DayEntry existingEntry, boolean isIngreso) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(isIngreso ? (existingEntry != null ? "Editar Ingreso" : "Agregar Ingreso")
                             : (existingEntry != null ? "Editar Gasto" : "Agregar Gasto"));

        VBox content = new VBox();
        content.setPadding(new Insets(16));
        content.setSpacing(12);

        ComboBox<String> monthCombo = new ComboBox<>();
        monthCombo.getItems().addAll(LedgerConstants.MONTHS);
        monthCombo.setValue(month);
        monthCombo.setDisable(true);

        TextField diaField = new TextField();
        if (existingEntry != null) {
            diaField.setText(existingEntry.dia);
        }
        diaField.setPromptText("Día (1-31)");

        TextField importeField = new TextField();
        if (existingEntry != null) {
            importeField.setText(existingEntry.importe);
        }
        importeField.setPromptText("Importe (CUP)");

        TextField notaField = new TextField();
        if (existingEntry != null && existingEntry.nota != null) {
            notaField.setText(existingEntry.nota);
        }
        notaField.setPromptText("Nota (opcional)");

        Button saveBtn = new Button(existingEntry != null ? "Guardar" : "Agregar");
        saveBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 25;");
        saveBtn.setOnAction(e -> {
            try {
                int dia = Integer.parseInt(diaField.getText());
                double importe = Double.parseDouble(importeField.getText());
                String nota = notaField.getText() != null ? notaField.getText().trim() : "";
                if (dia >= 1 && dia <= 31 && importe > 0) {
                    if (isIngreso) {
                        if (existingEntry != null) {
                            repository.editIngreso(month, Integer.parseInt(existingEntry.dia), dia, importe, nota);
                        } else {
                            repository.addIngreso(month, dia, importe, nota);
                        }
                    } else {
                        if (existingEntry != null) {
                            repository.editGasto(month, Integer.parseInt(existingEntry.dia), dia, importe, nota);
                        } else {
                            repository.addGasto(month, dia, importe, nota);
                        }
                    }
                    dialog.close();
                }
            } catch (NumberFormatException ex) {
                showAlert("Datos inválidos", "Ingrese valores numéricos válidos.");
            }
        });

        Button cancelBtn = new Button("Cancelar");
        cancelBtn.setStyle("-fx-font-size: 14px; -fx-padding: 8 20;");
        cancelBtn.setOnAction(e -> dialog.close());

        HBox buttons = new HBox();
        buttons.setSpacing(10);
        buttons.getChildren().addAll(saveBtn, cancelBtn);

        content.getChildren().addAll(
            new Label("Mes:"), monthCombo,
            new Label("Día:"), diaField,
            new Label("Importe (CUP):"), importeField,
            new Label("Nota (opcional):"), notaField,
            buttons
        );

        Scene scene = new Scene(content, 350, 280);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}