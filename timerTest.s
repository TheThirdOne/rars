  .data
loopStr:  	.asciz "Loop\n"
hello:  .asciz "Hello\n"
newLine:.asciz "\n"
time:	.word 0xFFFF0018
cmp:    .word 0xFFFF0020
.text
main:
	lw  a0, cmp
  	li  a1, 5000
  	sw  a1, 0(a0)
  	
	la	t0, handle
	csrrs	zero, 5, t0
	csrrsi	zero, 4, 0x10
	csrrsi	zero, 0, 0x1
	
	
loop:
	li	a7, 1
	lw	a0, time
	lw	a0, 0(a0)
	ecall
	li	a7, 4
	la	a0, newLine
	ecall	
	j	loop


handle:
	addi	sp, sp, -20
	sw	t0, 16(sp)
	sw	t1, 12(sp)
	sw	t2, 8(sp)
	sw	a0, 4(sp)
	sw	a7, 0(sp)
	
	li	a7, 4
	la	a0, hello
	ecall
	lw a0 time
	lw t2 0(a0)
	li t1 5000
	add t1 t2 t1
	lw t0 cmp
	sw t1 0(t0)
	
	lw	t0, 16(sp)
	lw	t1, 12(sp)
	lw	t2, 8(sp)
	lw	a0, 4(sp)
	lw	a7, 0(sp)
	addi	sp, sp, 20	
	uret

done:
	li	a7, 10
	ecall
