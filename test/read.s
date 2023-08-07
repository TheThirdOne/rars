#stdin:hello\nworldXremain
#stdout:11hello\nworld....\n1Xello\nworld....\n0Xello\nworld....\n6remainworld....\n0remainworld....\n

.eqv PrintInt, 1
.eqv Read, 63
.eqv PrintString, 4

# Expect to read one and half line "hello\nworld"
li a0, 0
la a1, buf
li a2, 11
li a7, Read
ecall
li a7, PrintInt
ecall
la a0, buf
li a7, PrintString
ecall

# Expect to read only a character
li a0, 0
la a1, buf
li a2, 1
li a7, Read
ecall
li a7, PrintInt
ecall
la a0, buf
li a7, PrintString
ecall

# Expect to read nothing
li a0, 0
la a1, buf
li a2, 0 # nothing
li a7, Read
ecall
li a7, PrintInt
ecall
la a0, buf
li a7, PrintString
ecall

# Expect to read what remains
li a0, 0
la a1, buf
li a2, 12
li a7, Read
ecall
li a7, PrintInt
ecall
la a0, buf
li a7, PrintString
ecall

# Expect to read nothing, it's EOF
li a0, 0
la a1, buf
li a2, 12
li a7, Read
ecall
li a7, PrintInt
ecall
la a0, buf
li a7, PrintString
ecall

li a0, 42 
li a7, 93	# Exit2
ecall

.data
buf: .string "...............\n"
