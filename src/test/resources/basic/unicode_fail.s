#error on lines: 4
.data
test1:
.string "© █ \u0054 \u2605 \n\u37"

.text
main:
	li a7, 64
	li a0, 1
	la a1, test1
	li a2, 14
	ecall

success:
	li a0, 42
	li a7, 93
	ecall
