@echo off
setlocal

:: === Configuration ===
set "APP_NAME=CineConcertManager"
set "APP_VERSION=1.1.0"
set "MAIN_JAR=target\CineConcertManager-1.1.0.jar"
set "MAIN_CLASS=org.davelogapps.cineconcertmanager.Launcher"
set "ICON_PATH=src\main\resources\iconeMjc.ico"

:: === Paths ===
set "JAVA_HOME=C:\Users\Merendir\.jdks\ms-21.0.10"
set "JAVAFX_JMODS=E:\Documents\Programmation\Java\JavaFX21\javafx-jmods-21.0.10"

set "OUTPUT_DIR=out"
set "APP_DIR=target\app"

if not exist "%JAVA_HOME%\bin\jpackage.exe" (
  echo ERROR : jpackage.exe not found in "%JAVA_HOME%\bin"
  pause
  exit /b 1
)

if not exist "%JAVAFX_JMODS%" (
  echo ERROR : JAVAFX_JMODS directory not found in "%JAVAFX_JMODS%"
  pause
  exit /b 1
)

:: === Cleaning ===
echo Cleaning...
if exist "%OUTPUT_DIR%\%APP_NAME%" rd /s /q "%OUTPUT_DIR%\%APP_NAME%"
if exist "%APP_DIR%" rd /s /q "%APP_DIR%"
mkdir "%APP_DIR%"

:: === Copying JAR ===
echo Copying JAR into %APP_DIR%
copy "%MAIN_JAR%" "%APP_DIR%" > nul

:: === Packaging ===
echo Executable generation with JPackage...
"%JAVA_HOME%\bin\jpackage.exe" ^
  --type app-image ^
  --name "%APP_NAME%" ^
  --app-version "%APP_VERSION%" ^
  --input "%APP_DIR%" ^
  --main-jar "CineConcertManager-1.1.0.jar" ^
  --main-class "%MAIN_CLASS%" ^
  --module-path "%JAVAFX_JMODS%;%JAVA_HOME%\jmods" ^
  --add-modules javafx.controls,javafx.fxml,javafx.media,javafx.graphics,java.logging ^
  --icon "%ICON_PATH%" ^
  --dest "%OUTPUT_DIR%" ^
  --win-console ^
  --verbose

echo Finished! Launch .exe from %OUTPUT_DIR%\%APP_NAME%\bin
pause
