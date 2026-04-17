# Guía de Edición - TCP Finanzas

## Primeros Pasos

1. **Instalar IntelliJ IDEA** (recomendado)
   - https://www.jetbrains.com/idea/download/

2. **Abrir el proyecto**
   - File → Open → seleccionar carpeta `tcp-finanzas`
   - Maven会自动下载 dependencias

3. **Ejecutar para probar**
   - Run → Run 'TCPFinanzas'

---

## Estructura de Archivos

### Main.java
**Ubicación:** `src/main/java/com/tcpfinanzas/Main.java`

**Qué hace:** Muestra la interfaz, maneja botones y navegación

**Para editar:**
- Títulos de pantalla (líneas 98, 119, 132, 162)
- Colores (líneas 87, 104)
- Ancho de menús (línea 111)

---

## Ejemplos de Cambios

### Ejemplo 1: Cambiar Color del Encabezado

**Buscar en Main.java:**
```java
header.setStyle("-fx-background-color: #1976d2;");
```

**Cambiar a:**
```java
header.setStyle("-fx-background-color: #2e7d32;");  // Verde
```

### Ejemplo 2: Añadir Nuevo Campo en Datos Generales

**Buscar método `showGenerales()` y añadir:**

```java
grid.add(new Label("Email:"), 0, 4);
grid.add(new TextField(), 1, 4);
grid.add(new Label("Teléfono:"), 0, 5);
grid.add(new TextField(), 1, 5);
```

### Ejemplo 3: Cambiar Texto de Botón

**Buscar:**
```java
Button printBtn = new Button("Imprimir");
```

**Cambiar a:**
```java
Button printBtn = new Button("Exportar PDF");
```

### Ejemplo 4: Añadir Nueva Pestaña

1. **En el menú (createMenu):**
```java
Button btn = new Button("Bancos");
btn.setOnAction(e -> currentMenu = Menu.BANCOS);
```

2. **Añadir enum:**
```java
private enum Menu {
    GENERALES, INGRESOS, GASTOS, IMPUESTOS, BANCOS
}
```

3. **Añadir caso en refreshContent:**
```java
case BANCOS:
    showBancos();
    break;
```

4. **Crear método:**
```java
private void showBancos() {
    Label title = new Label("Cuentas Bancarias");
    title.setFont(new Font(24));
    contentPane.getChildren().add(title);
    // Añadir más elementos...
}
```

---

## Cómo Funciona el Sistema

### Menú Lateral
```
createMenu() → botones que cambian currentMenu
refreshContent() → muestra contenido segúncurrentMenu
```

### Datos
```
Main.java → llama a repository
LedgerRepository.java → guarda/recupera datos
Registro.java → modelo de datos en memoria
```

---

## Patrones Comunes

### Crear un Campo de Texto
```java
TextField campo = new TextField();
campo.setPromptText("Escribe aquí...");
grid.add(new Label("Etiqueta:"), 0, fila);
grid.add(campo, 1, fila);
```

### Crear una Lista
```java
ComboBox<String> opciones = new ComboBox<>();
opciones.getItems().addAll("Opción 1", "Opción 2", "Opción 3");
opciones.setValue("Opción 1");
```

### Crear un Botón
```java
Button btn = new Button("Guardar");
btn.setOnAction(e -> {
    // código al hacer clic
});
```

### Mostrar Alerta
```java
Alert alert = new Alert(Alert.AlertType.INFORMATION);
alert.setTitle("Título");
alert.setContentText("Mensaje");
alert.showAndWait();
```

---

## Valores que Puedes Cambiar

### Colores
| Color | Código |
|------|--------|
| Azul | #1976d2 |
| Verde | #4caf50 |
| Rojo | #f44336 |
| Naranja | #ff9800 |
| Gris | #757575 |
| Blanco | #ffffff |
| Negro | #000000 |

### Fuentes
| Tamaño | Uso |
|--------|-----|
| 24 | Títulos de pantalla |
| 18 | Subtítulos |
| 14 | Texto normal |
| 12 | Detalles |

---

## Persistencia de Datos (Guardar a Archivo)

Para guardar datos entre ejecuciones:

### Paso 1: Añadir a LedgerRepository.java

```java
import java.io.*;

public void saveToFile(String filename) {
    try (ObjectOutputStream out = new ObjectOutputStream(
            new FileOutputStream(filename))) {
        out.writeObject(registro);
    } catch (IOException e) {
        e.printStackTrace();
    }
}

public void loadFromFile(String filename) {
    try (ObjectInputStream in = new ObjectInputStream(
            new FileInputStream(filename))) {
        registro = (Registro) in.readObject();
        notifyListeners();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

### Paso 2: Llamar desde Main.java

```java
// Al iniciar
repository.loadFromFile("datos.dat");

// Al cerrar
repository.saveToFile("datos.dat");
```

---

## Exportar a PDF

Necesitas añadir dependencia en pom.xml:

```xml
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>7.2.5</version>
    <type>pom</type>
</dependency>
```

Luego crear método `exportToPdf()`.

---

## Siguientes Pasos Sugeridos

1. ✓ Añadir persistencia (guardar datos)
2. ✓ Exportar a PDF
3. ✓ Conexión a base de datos
4. ✓ Autenticación de usuario
5. ✓ Sincronización con servidor

---

## Dónde Pedir Ayuda

- JavaFX: https://openjfx.io/
- Documentación: https://docs.oracle.com/javase/8/javase-api.htm
- Stack Overflow: busqua "javafx"