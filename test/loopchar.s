#stdin:10\n20\n0
#stdout:1020
loop:
	li a7, 5
	ecall

	beqz a0, end

	li a7, 1
	ecall
	j loop
end:
	li a7, 10
	ecall
