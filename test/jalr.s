.globl main
.text
main:
        la t0, test_2
        jalr t0, t0, 8
test_1:
        j failure
        j success
test_2:
        j failure
        j failure
	la t1, test_1
	bne t0, t1, failure
	la t0, test_2
	jalr t0, t0, -4
failure:	
	li a0, 0
	li a7, 93
	ecall
success:
	li a0, 42
	li a7, 93
	ecall
