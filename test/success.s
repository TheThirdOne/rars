.globl main
.text
main:
	# Simple test to confirm the success code works
	li a0, 42 
	li a7, 93
	ecall
	
	li a0, 0
	li a7, 93
	ecall
