REM TCP Finanzas - Build para Windows
REM Ejecutar en Developer Command Prompt o PowerShell

REM 1. Descargar dependencias
echo Descargando JavaFX SDK...
curl -L "https://download2.gluonhq.com/openjfx/17.0.2/openjfx-sdk-17.0.2_windows-x64_bin.zip" -o javafx.zip

echo Descomprimiendo...
powershell -Command "Expand-Archive -Force javafx.zip ."

REM 2. Compilar
echo Compilando...
mvn clean package -DskipTests

REM 3. Crear ejecutable
echo Creando ejecutable...
set JAVAFX=openjfx-sdk-17.0.2\lib

jpackage --input target --main-jar tcp-finanzas-1.0.jar --main-class com.tcpfinanzas.Main --name TCPFinanzas --type exe

echo Listo! TCPFinanzas.exe creado