#!/bin/bash
javac -cp rars.jar test/Test.java
java -cp test:rars.jar Test
rm test/Test.class
