.globl main
.data
buffer: .space 8
.text
main:
	la t0, buffer
	li t1, 8
	sw t1, 0(t0)
	lw t2, 0(t0)
	bne t1, t2, failure
	li t3, 56
	sw t3, 4(t0)
	addi t0, t0, 4
	lw t4, 0(t0)
	bne t3, t3, failure
	lw t5, -4(t0)
	bne t5,t1, failure
success:
	li a0, 42
	li a7, 93
	ecall
failure:	
	li a0, 0
	li a7, 93
	ecall
