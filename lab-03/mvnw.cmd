@echo off
rem Minimal Maven Wrapper for Windows.
rem Downloads the Maven version from .mvn\wrapper\maven-wrapper.properties once
rem into %USERPROFILE%\.m2\wrapper\dists and runs it with the given arguments.
setlocal
set "BASEDIR=%~dp0"
for /f "usebackq tokens=1,* delims==" %%A in ("%BASEDIR%.mvn\wrapper\maven-wrapper.properties") do if "%%A"=="distributionUrl" set "URL=%%B"
for %%F in ("%URL%") do set "ZIPNAME=%%~nF"
set "NAME=%ZIPNAME:-bin=%"
set "DIST=%USERPROFILE%\.m2\wrapper\dists\%NAME%"
if exist "%DIST%\bin\mvn.cmd" goto run
echo Downloading %URL%
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference=Stop; [Net.ServicePointManager]::SecurityProtocol=Tls12; $t=Join-Path $env:TEMP (mvnw-+[guid]::NewGuid()); New-Item -ItemType Directory $t | Out-Null; Invoke-WebRequest -UseBasicParsing %URL% -OutFile (Join-Path $t maven.zip); Expand-Archive (Join-Path $t maven.zip) $t; New-Item -ItemType Directory -Force %USERPROFILE%\.m2\wrapper\dists | Out-Null; Move-Item (Join-Path $t %NAME%) %DIST%; Remove-Item -Recurse -Force $t"
if errorlevel 1 exit /b 1
:run
call "%DIST%\bin\mvn.cmd" -f "%BASEDIR%pom.xml" %*
exit /b %ERRORLEVEL%
