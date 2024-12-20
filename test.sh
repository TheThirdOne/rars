#!/bin/bash
javac -cp rars.jar test/RarsTest.java
java -cp test:rars.jar RarsTest "$@"
err=$?
rm test/RarsTest.class
exit "$err"
