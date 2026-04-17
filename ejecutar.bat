@echo off
REM TCP Finanzas - Ejecutar en Windows
REM Requiere: JDK 17 + JavaFX SDK

set SCRIPT_DIR=%~dp0
set JAVA_HOME=%SCRIPT_DIR%jdk
set JAVAFX=%SCRIPT_DIR%javafx\lib
set JAR=%SCRIPT_DIR%tcp-finanzas-1.0.jar

if not exist "%JAVA_HOME%" (
    echo ERROR: No se encontró JDK en la carpeta jdk
    echo Descarga JDK 17 de: https://adoptium.net/
    pause
    exit /b 1
)

if not exist "%JAVAFX%" (
    echo ERROR: No se encontró JavaFX en la carpeta javafx
    echo Descarga JavaFX SDK de: https://gluonhq.com/products/javafx/
    pause
    exit /b 1
)

"%JAVA_HOME%\bin\java" --module-path "%JAVAFX%" --add-modules javafx.controls,javafx.graphics -jar "%JAR%" %*

pause