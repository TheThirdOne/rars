#exit:42
	.data
	# autoalign by default
z:	.byte 0x11
b:	.byte 0x12
	.byte 0x13
h:	.half 0x2222
w:	.word 0x44444444
d:	.dword 0x8888888888888888

	.text
	la s0, z
	la s1, b
	la s2, h
	la s3, w
	la s4, d

	sub a0, s1, s0
	li t0, 1
	bne a0, t0, fail

	sub a0, s2, s0
	li t0, 4
	bne a0, t0, fail

	sub a0, s3, s0
	li t0, 8
	bne a0, t0, fail

	sub a0, s4, s0
	li t0, 16
	bne a0, t0, fail

	li a7, 93 # Exit2
	li a0, 42
	ecall

fail:	
	li a7, 93 # Exit2
	li a0, 0
	ecall
	
