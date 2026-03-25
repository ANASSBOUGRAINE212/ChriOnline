@echo off
echo Starting ChriOnline UI...
echo.
echo Make sure the server is running on localhost:5000
echo.

set JAVAFX_PATH=C:\Users\dell\Downloads\openjfx-21.0.10_windows-x64_bin-sdk\javafx-sdk-21.0.10\lib
set MAIN_PROJECT=..\bin

java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml -cp "bin;%MAIN_PROJECT%" application.Main

pause
