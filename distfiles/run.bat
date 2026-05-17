@echo off
REM wue launcher (Windows). Requires Java 17+ on PATH.
setlocal
set "DIR=%~dp0"
cd /d "%DIR%"
where java >nul 2>nul
if errorlevel 1 (
    echo wue: 'java' not found on PATH. Install Java 17+ and retry.
    exit /b 1
)
java -Djava.awt.headless=true -jar wue.jar %*
endlocal
