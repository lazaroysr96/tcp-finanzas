# TCP Finanzas - Aplicación de Escritorio

Aplicación de escritorio para gestión de Declaración Jurada (Ingresos, Gastos, Impuestos).

## Requisitos

### Windows
- **JDK 17** o superior (no solo JRE)
  - Descargar: https://adoptium.net/
  - Recomendado: Eclipse Temurin JDK 17 (x64 MSI)
- **JavaFX SDK 17** (para ejecutar la app)
  - Descargar: https://gluonhq.com/products/javafx/

### Linux
```bash
sudo pacman -S jdk17-openjdk   # Arch
sudo apt install openjdk-17-jdk  # Ubuntu/Debian
```

---

## Cómo Ejecutar

### Opción 1: Con Maven (Recomendado)

1. Asegúrate de tener Maven instalado:
   ```bash
   mvn -v
   ```

2. Compilar y ejecutar:
   ```bash
   mvn clean compile
   mvn javafx:run
   ```

### Opción 2: Con JAR Ejecutable

1. Compilar:
   ```bash
   mvn clean package
   ```

2. Ejecutar el JAR:
   ```bash
   java --module-path $RUTA_JAVAFX --add-modules javafx.controls,javafx.graphics -jar target/tcp-finanzas-1.0-SNAPSHOT.jar
   ```

   En Windows:
   ```cmd
   java -jar target\tcp-finanzas-1.0-SNAPSHOT.jar
   ```

---

## Cómo Crear un .exe para Windows

### Opción 1: Con jpackage (JDK 14+)

```bash
jpackage --input target --main-jar tcp-finanzas-1.0-SNAPSHOT.jar ^
         --main-class com.tcpfinanzas.Main ^
         --name "TCPFinanzas" ^
         --type exe ^
         --icon src/main/resources/icon.ico
```

### Opción 2: Con launch4j (más portable)

1. Descargar launch4j: https://launch4j.sourceforge.io/

2. Crear archivo `launch4j-config.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<launch4jConfig>
  <dontUsePrivateJre>false</dontUsePrivateJre>
  <headerType>gui</headerType>
  <jar>target/tcp-finanzas-1.0-SNAPSHOT.jar</jar>
  <outfile>dist/TCPFinanzas.exe</outfile>
  <jre>
    <minVersion>17.0.0</minVersion>
    <maxHeapSize>512</maxHeapSize>
  </jre>
  <icon>src/main/resources/icon.ico</icon>
</launch4jConfig>
```

3. Ejecutar:
   ```bash
   launch4j launch4j-config.xml
   ```

---

## Estructura del Proyecto

```
tcp-finanzas/
├── pom.xml                    # Configuración Maven
├── src/main/java/com/tcpfinanzas/
│   ├── Main.java              # UI principal y navegación
│   ├── data/
│   │   └── LedgerRepository.java  # Lógica de datos
│   └── model/
│       ├── Registro.java        # Modelo principal
│       ├── DayEntry.java        # Entrada día/importe
│       ├── TributoRow.java      # Datos de impuestos
│       └── LedgerConstants.java # Meses y etiquetas
└── src/main/resources/        # Recursos (iconos, etc)
```

---

## Cómo Editar la Aplicación

### 1. Cambiar Colores/Estilos

En `Main.java`, método `createHeader()`:

```java
header.setStyle("-fx-background-color: #1976d2;");  // Azul
```

Otros colores comuns:
- Verde: `#4caf50`
- Rojo: `#f44336`
- Gris: `#f5f5f5`

### 2. Añadir Campos a Datos Generales

En `Main.java`, método `showGenerales()`:

```java
grid.add(new Label("Nuevo Campo:"), 0, 4);
grid.add(new TextField(), 1, 4);
```

### 3. Añadir Nuevo Mes

En `LedgerConstants.java`:

```java
public static final List<String> MONTHS = List.of(
    "ENE", "FEB", "MAR", "ABR", "MAY", "JUN",
    "JUL", "AGO", "SEP", "OCT", "NOV", "DIC"
    // Añadir "ENERO2" si es necesario
);
```

### 4. Cambiar Textos/Etiquetas

En `Main.java`:
- Título: `new Label("Declaración Jurada")`
- Botones: `new Button("Imprimir")`

### 5. Añadir Funcionalidad de Guardar/Cargar

En `LedgerRepository.java` añadir:

```java
public void saveToFile() {
    // Guardar a JSON o archivo
}

public void loadFromFile() {
    // Cargar desde archivo
}
```

---

## Ajustes Futuros Comunes

### Cambiar Título de la Ventana
```java
primaryStage.setTitle("TCP Finanzas - Mi Empresa");
```

### Cambiar Tamaño por Defecto
```java
Scene scene = new Scene(root, 1024, 768);  // Ancho x Alto
```

### Añadir Validación de Datos
En `showEntryDialog()`, añadir checks:
```java
if (importe <= 0) {
    showAlert("Error", "El importe debe ser mayor que 0");
    return;
}
```

### Conectar a Base de Datos
1. Añadir dependencia en `pom.xml`:
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.224</version>
</dependency>
```

2. Modificar `LedgerRepository.java` para usar JDBC.

### Cambiar a Otro Idioma
Editar textos en español en `Main.java`:
- "Ingresos" → "Income"
- "Gastos" → "Expenses"
- "Impuestos" → "Taxes"

---

## Auto-guardado

Los datos se guardan automáticamente en el archivo:
- `tcp_finanzas_data.json`

Cada vez que:
- Modificas Datos Generales y guardas
- Haces clic en "Guardar" del encabezado

---

## Exportar a PDF

El PDF incluye:
- Datos del contribuyente
- Domicilio fiscal y legal
- Resumen anual (totales)
- Tabla de ingresos por mes
- Tabla de gastos por mes
- Tabla de tributos

Se genera al hacer clic en "Exportar PDF" en el encabezado.

---

## Solución de Problemas

### "Error: no se encontró el módulo javafx"
Descargar JavaFX SDK y ejecutar:
```bash
java --module-path /ruta/a/javafx-sdk-17/lib ^
     --add-modules javafx.controls,javafx.graphics ^
     -jar target/tcp-finanzas-1.0-SNAPSHOT.jar
```

### "Error: no se encontró la clase Main"
Asegurarse de compilar:
```bash
mvn clean compile
```

### La ventana no responde
Verificar que no haya múltiples instancias ejecutándose.

---

## Compilar para distribución

### Windows (.exe)

1. Instalar JDK 17 y JavaFX SDK en el servidor de build
2. Compilar:
   ```bash
   mvn clean package
   ```

3. Crear ejecutable (bundlea JDK + JavaFX):
   ```bash
   jpackage --input target --main-jar tcp-finanzas-1.0.jar ^
         --main-class com.tcpfinanzas.Main ^
         --name TCPFinanzas ^
         --type exe ^
         --icon src/main/resources/icon.ico
   ```

   Esto genera un `.exe` instalable que incluye todo.

### Linux (.AppImage)

En Linux con Display:
```bash
jpackage --input target --main-jar tcp-finanzas-1.0.jar ^
         --main-class com.tcpfinanzas.Main ^
         --name TCPFinanzas ^
         --type app-image
```

---

## Technologies Usadas

- **Java 17**
- **JavaFX 17** (UI)
- **Maven** (build)
- **Gson** (JSON)
- **SQLite** (Nomenclador)

---

## Créditos

Basado en la app Android de SYSGD Accounting.