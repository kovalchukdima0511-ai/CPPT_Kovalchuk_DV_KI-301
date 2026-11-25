@echo off
rem Quick start without Maven: compiles src\main\java and runs the program.
chcp 65001 >nul
cd /d "%~dp0"
if exist out\classes rmdir /s /q out\classes
mkdir out\classes
javac -encoding UTF-8 -d out\classes src\main\java\ua\lpnu\kzp\*.java
if errorlevel 1 exit /b 1
java -cp out\classes ua.lpnu.kzp.Main %*
