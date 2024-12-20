#stdin:Hello\nthe\nworld
#stdout:Hello\nthe\nworld
	.data
buffer: .space 256

	.text
loop:
	li a7, 8
	la a0, buffer
	li a1, 256
	ecall

	lb t0, 0(a0)
	li t1, '\n'
	beq t0, t1, end

	li a7, 4
	la a0, buffer
	ecall
	j loop

end:
	li a7, 10
	ecall
