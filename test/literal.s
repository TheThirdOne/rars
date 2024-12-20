#stdout:168
#exit:42
.text
main:
	li a0, 42
	addi a0, a0, '*'
	addi a0, a0, 0x2a
	addi a0, a0, 0b101010
	li a7, 1 # print integer
	ecall

success:
	li a0, 42
	li a7, 93
	ecall
