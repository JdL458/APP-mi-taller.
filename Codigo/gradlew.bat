@rem
@rem Copyright 2015 del autor o autores originales.
@rem
@rem Licenciado bajo la Licencia Apache, Versión 2.0 (la "Licencia");
@rem No puede usar este archivo excepto en cumplimiento con la Licencia.
@rem Puede obtener una copia de la Licencia en:
@rem
@rem https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem A menos que lo exija la ley aplicable o se acuerde por escrito, el software
@rem distribuido bajo la Licencia se distribuye "TAL CUAL",
@rem SIN GARANTÍAS NI CONDICIONES DE NINGÚN TIPO, ya sean expresas o implícitas.
@rem Consulte la Licencia para conocer el idioma específico que rige los permisos y
@rem las limitaciones bajo la Licencia.
@rem

@if "%DEBUG%" == "" @echo off
@rem ###########################################################################
@rem
@rem Script de inicio de Gradle para Windows
@rem
@rem ##########################################################################

@rem Establece el ámbito local para las variables con el shell de Windows NT.
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Elimina cualquier "." y ".." en APP_HOME para que sea más corto.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Agrega aquí las opciones predeterminadas de la JVM. También puedes usar JAVA_OPTS y GRADLE_OPTS para pasar opciones de la JVM a este script.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem Encuentra java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Configurar la línea de comandos

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar


@rem Ejecutar Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
@rem Finaliza el ámbito local para las variables con el shell de Windows NT.
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
@rem Establezca la variable GRADLE_EXIT_CONSOLE si necesita el código de retorno del script en lugar de
@rem ¡El código de retorno de _cmd.exe /c_!
if  not "" == "%GRADLE_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
