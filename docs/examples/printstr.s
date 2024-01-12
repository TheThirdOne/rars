.globl printStr
.text
printStr:
	mv t0, a0
	# loop until null byte
loop:
	lb t1, 0(t0)
	addi t0, t0, 1
	bnez t1, loop
	
	# Compute length
	sub a2, t0, a0
	addi a2, a2, -1
	
	# Write (64) to stdout (1)
	mv  a1, a0
	li a0, 1
	li a7, 64
	ecall

	ret
