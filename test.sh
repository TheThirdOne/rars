#!/bin/bash
javac -encoding utf-8 -cp rars.jar test/Test.java
if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]]; then
	java -Dfile.encoding=UTF8 -cp "test;rars.jar" Test
else
	java -Dfile.encoding=UTF8 -cp test:rars.jar Test
fi
rm test/Test.class
