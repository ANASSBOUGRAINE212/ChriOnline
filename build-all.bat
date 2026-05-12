@echo off
echo ========================================
echo Building ChriOnline JARs
echo ========================================
echo.

echo [1/3] Building Client JAR...
call mvn clean package -Pclient -q
if %errorlevel% neq 0 (
    echo ERROR: Client build failed!
    exit /b 1
)
echo ✓ Client JAR created: target\ChriOnline-Client.jar
echo.

echo [2/3] Building Admin JAR...
call mvn package -Padmin -q
if %errorlevel% neq 0 (
    echo ERROR: Admin build failed!
    exit /b 1
)
echo ✓ Admin JAR created: target\ChriOnline-Admin.jar
echo.

echo [3/3] Building Server JAR...
call mvn package -Pserver -q
if %errorlevel% neq 0 (
    echo ERROR: Server build failed!
    exit /b 1
)
echo ✓ Server JAR created: target\ChriOnline-Server.jar
echo.

echo ========================================
echo All JARs built successfully!
echo ========================================
echo.
echo Files created in target folder:
dir /b target\*.jar
echo.
pause
