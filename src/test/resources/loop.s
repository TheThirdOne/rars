.globl main
.text
main:
	# Tests simple looping behaviour
	li t0, 60
	li t1, 0
loop:
	addi t1, t1, 5
	addi t0, t0, -1
	bne t1, t0, loop
	bne t1, zero, success
failure:
	li a0, 0
	li a7, 93 
	ecall
	
success:
	li a0, 42 
	li a7, 93
	ecall
	
