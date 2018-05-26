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
	li t1, 0xFF00F007
        sw t1, 0(t0)
        lb t2, 0(t0)
        li t3, 7
        bne t2, t3, failure
	lb t2, 1(t0)
        li t3, 0xFFFFFFF0
        bne t2, t3, failure
        lbu t2, 1(t0)
	li t3, 0xF0
        bne t2, t3, failure
success:
	li a0, 42
	li a7, 93
	ecall
failure:	
	li a0, 0
	li a7, 93
	ecall
