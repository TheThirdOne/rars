#stdout:© █ T ★ \n
.data
test1:
.string "© █ \u0054 \u2605 \n"

.text
main:
	li a7, 64
	li a0, 1
	la a1, test1
	li a2, 14
	ecall

	# TODO: fix not being redirected to the string stdout
	#li a7, 4
	#mv a0, a1
	#ecall

# TODO: tests with read syscall
# TODO: tests with open
# TODO: tests designed to fail; test error handling

#TODO: tests for unimplemented features currently TODOs in the java code

success:
	li a0, 42
	li a7, 93
	ecall
