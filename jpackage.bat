@echo off
setlocal

:: === Configuration ===
set "APP_NAME=CineConcertCreator"
set "APP_VERSION=1.1.0"
set "MAIN_JAR=target\CineConcertManager-1.1.0.jar"
set "MAIN_CLASS=org.davelogapps.cineconcertmanager.App"
set "ICON_PATH=src\main\resources\iconeMjc.ico"

:: === Paths ===
set "JAVA_HOME=C:\Program Files\Java\jdk-21"
set "JAVAFX_SDK=E:\Documents\Programmation\Java\JavaFX21\javafx-sdk-21.0.6"
set "OUTPUT_DIR=out"
set "APP_DIR=target\app"

:: === Cleaning ===
echo 🧹 Nettoyage...
if exist "%OUTPUT_DIR%\%APP_NAME%" rd /s /q "%OUTPUT_DIR%\%APP_NAME%"
if exist "%APP_DIR%" rd /s /q "%APP_DIR%"
mkdir "%APP_DIR%"

:: === Copying JAR ===
echo 📦 Copie du .jar dans %APP_DIR%
copy "%MAIN_JAR%" "%APP_DIR%" > nul

:: === Packaging ===
echo 🚀 Création de l'exécutable avec jpackage...
"%JAVA_HOME%\bin\jpackage.exe" ^
  --type app-image ^
  --name "%APP_NAME%" ^
  --app-version "%APP_VERSION" ^
  --input "%APP_DIR%" ^
  --main-jar "CineConcertManager-1.1.0.jar" ^
  --main-class "%MAIN_CLASS%" ^
  --module-path "%JAVAFX_SDK%\lib" ^
  --add-modules javafx.controls,javafx.media,javafx.graphics ^
  --icon "%ICON_PATH%" ^
  --dest "%OUTPUT_DIR%" ^
  --win-console ^
  --verbose

echo ✅ Terminé ! Lance le .exe depuis %OUTPUT_DIR%\%APP_NAME%\bin
pause
