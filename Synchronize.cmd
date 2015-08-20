@echo off

IF "%BS_PATH%" == "" GOTO error

SET ALL_JARS=-classpath "%BS_PATH%\BSframework-lib-1.2.jar;%BS_PATH%\timectrl-common.jar;%BS_PATH%\mysql-connector-java-5.1.26-bin.jar;%BS_PATH%\com4j.jar;%BS_PATH%\timectrl-bl.jar;"

java %ALL_JARS% cl.buildersoft.timectrl.business.console.Synchronize

goto end

:error
	echo No esta definida la variable de entorno BS_PATH
	
:end
SET ALL_JARS=

