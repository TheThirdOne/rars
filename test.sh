#!/bin/bash
RUN="java -jar -ea ./rars.jar"
ERRORS=""
for f in ./test/*.s ./test/riscv-tests/*.s
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
