#!/bin/bash
riscv64-linux-gnu-as -march=rv32g _start.s -o _start.o
javac -cp rars.jar test/Test.java test/ElfTest.java
java  -cp test:rars.jar Test
java  -cp test:rars.jar ElfTest
rm test/Test.class
rm test/ElfTest.class
rm testobject.o
rm testobject
rm _start.o
rm testinput.s

