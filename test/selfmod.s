
# TODO: make this run with self modifications 
.text 
main:
	la s0, toload
	la s1, torun
	
	li a0, 2
	call toload
	
	# First copy by word to data
	lw t0, (s0)
	sw t0, (s1)
	lw t0, 4(s0)
	sw t0, 4(s1)
	
	# try running it
	call torun
	
	# Copy by byte to data
	mv s2, a0
	mv a0, s0
	mv a1, s1
	call copy
	
	mv a0, s2 
	call torun
	
	# copy to .text
	mv s2, a0
	la a0, torun
	la a1, main
	call copy

	mv a0, s2 
	call main

	
	li a7, 93
	ecall
	
toload:
	addi a0, a0, 10
	ret
	
copy:
	li t0, 0
	li t3, 8
loop:
	add t1, a0, t0
	lb t1, (t1)
	add t2, a1, t0
	sb t1, (t2)
	addi t0, t0, 1
	blt t0, t3, loop
	ret

.data
torun:
	.space 8
