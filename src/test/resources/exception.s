.globl main
.text
main:
 	la t0,handler
 	csrrw zero, 5, t0 # set utvec
 	csrrsi zero, 0, 1 # set interrupt enable
 	lw zero, 0(zero)        # trigger trap
failure:
	li a0, 0
	li a7, 93
	ecall
success:
 	li a0, 42
 	li a7, 93
 	ecall
handler:
 	# move epc to success and return
	la t0, success 	
	csrrw zero, 65, t0
	uret
	
