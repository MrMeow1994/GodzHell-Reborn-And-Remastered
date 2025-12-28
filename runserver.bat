@echo off
title Ghreborn FileServer (Gradle Runtime)
color 0A

REM === Java executable ===
set JAVA_EXE="C:\Users\Sgsrocks\.jdks\corretto-15.0.2\bin\java.exe"

REM === Base project directory ===
set BASE_DIR=E:\Godzhell-reborn-refixed-v.9

REM === Load Gradle runtime classpath ===
REM Make sure you previously ran 'gradlew printRuntimeClasspath > classpath.txt'
set /p CP=<"%BASE_DIR%\classpath.txt"

REM === JVM Options ===
set JVM_OPTS=-Xmx7g -Xms2g

REM === Launch FileServer ===
%JAVA_EXE% %JVM_OPTS% -cp build\classes\java\main;deps/*; com.Ghreborn.jagcached.FileServer

pause
