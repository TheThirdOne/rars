#stdin:a b\n\n!
#stdout:97a32 98b10\n10\n33!-1�-1�
#exit:42

.eqv PrintInt, 1
.eqv ReadChar, 12
.eqv PrintChar, 11

li s0, 8
loop:
beqz s0, end
addi s0, s0, -1
li a7, ReadChar
ecall
li a7, PrintInt
ecall
# Note: lowest byte of -1 is ff, and U+FF is ÿ
li a7, PrintChar
ecall
j loop

end:
li a0, 42 
li a7, 93	# Exit2
ecall
