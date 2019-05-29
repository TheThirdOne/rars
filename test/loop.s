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
	
