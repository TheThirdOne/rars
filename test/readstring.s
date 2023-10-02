#stdin:hello\n\nworldNOTWORLD\nXXXX\nYYYY\nremain
#stdout:0hello\n1\n2world34A5remain\n6\n

.eqv PrintInt, 1
.eqv ReadString, 8
.eqv PrintString, 4

# Expect to read one line "hello\n"
la a0, buf
li a1, 256
li a7, ReadString
ecall
li a0, 0
li a7, PrintInt
ecall
la a0, buf
li a7, PrintString
ecall

# Expect to read an empty line
la a0, buf
li a1, 256
li a7, ReadString
ecall
li a0, 1
li a7, PrintInt
ecall
la a0, buf
li a7, PrintString
ecall

# Expect to read "world" only
la a0, buf
li a1, 6 # include space for '\0'
li a7, ReadString
ecall
li a0, 2
li a7, PrintInt
ecall
la a0, buf
li a7, PrintString
ecall

# Expect to read nothing
la a0, buf
li a1, 1 # only space for a nullbyte
li a7, ReadString
ecall
li a0, 3
li a7, PrintInt
ecall
la a0, buf
li a7, PrintString
ecall

# Expect to read nothing
la a0, buf
li a1, 'A'
sb a1, 0(a0)
sb zero, 1(a0) # Initialize buffer with "A\0"
li a1, 0 # no space for a nullbyte
li a7, ReadString
ecall
li a0, 4
li a7, PrintInt
ecall
la a0, buf
li a7, PrintString
ecall

# Expect to read the last line, a "\n" is added
la a0, buf
li a1, 256
li a7, ReadString
ecall
li a0, 5
li a7, PrintInt
ecall
la a0, buf
li a7, PrintString
ecall

# Expect to read only an added "\n". ReadString has no concept of EOF
la a0, buf
li a1, 256
li a7, ReadString
ecall
li a0, 6
li a7, PrintInt
ecall
la a0, buf
li a7, PrintString
ecall

li a0, 42 
li a7, 93	# Exit2
ecall

.data
buf: .space 256
