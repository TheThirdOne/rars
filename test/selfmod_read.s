# Test read-only in the code segment (should not require selfmod to be set)
#stdout:1050771
	.text
foo:	li a7, 1 # PrintInt
	lw a0, foo
	ecall
	li a7, 10
	ecall
