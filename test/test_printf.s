#lib:../examples/printf.s
#lib:../examples/printstr.s
#lib:../examples/printnum.s
#stdout:Hello_World!42

	la a0, hello_world
	li a1, 42
	call printf

	li a7, 10
	ecall

	.data
hello_world: .string "Hello_World!%d\n"
