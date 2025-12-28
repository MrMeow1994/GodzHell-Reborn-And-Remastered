@echo off
title Ghreborn FileServer (Full Java + Kotlin + Deps)
color 0A

REM === Java executable ===
set JAVA_EXE="C:\Users\Sgsrocks\.jdks\corretto-15.0.2\bin\java.exe"

REM === JVM Options ===
set JVM_OPTS=-Xmx7g -Xms2g

REM === Launch FileServer (including Kotlin output) ===
%JAVA_EXE% %JVM_OPTS% -cp build\classes\java\main;build\classes\kotlin\main;deps/* server
pause
