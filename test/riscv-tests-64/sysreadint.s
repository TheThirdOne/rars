#stdin:1\n-1\n123456789\n2410117871984078658\n
#exit:42
	li a7, 5 # ReadInt

	ecall
	li a1, 1
	bne a0, a1, fail

	ecall
	li a1, -1
	bne a0, a1, fail

	ecall
	li a1, 123456789
	bne a0, a1, fail

	ecall
	li a1, 2410117871984078658
	bne a0, a1, fail

pass:
	li a0, 42
	li a7, 93
	ecall

fail:
	li a0, 0
	li a7, 93
	ecall
