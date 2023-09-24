#stdout:2147483647 0x7fffffff 01111111111111111111111111111111 2147483647\n-2147483648 0x80000000 10000000000000000000000000000000 2147483648\n-2147483648 0x80000000 10000000000000000000000000000000 2147483648\n
li s0, 0x7FFFFFFF
jal printAll
addi s0, s0, 1
jal printAll
neg s0, s0
jal printAll

li a0, 42
li a7, 93
ecall

printAll:
li a7, 1 # PrintInt
mv a0, s0
ecall
li a7, 11 # PrintChar
li a0, ' '
ecall
li a7, 34 # PrintIntHex
mv a0, s0
ecall
li a7, 11 # PrintChar
li a0, ' '
ecall
li a7, 35 # PrintIntBinary
mv a0, s0
ecall
li a7, 11 # PrintChar
li a0, ' '
ecall
li a7, 36 # PrintIntUnsigned
mv a0, s0
ecall
li a7, 11 # PrintChar
li a0, '\n'
ecall
ret
