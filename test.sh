#!/bin/bash
RUN="java -jar -ea ./out/artifacts/mars/mars.jar" 
ERRORS=""
for f in ./test/*.s
do	
	$RUN $f > /dev/null
	if [ $? -eq 42 ]
	then 
		printf "."
	else
		printf "X"	
		ERRORS="$ERRORS\nFailure on file $f"
	fi
done

printf "$ERRORS\n"
