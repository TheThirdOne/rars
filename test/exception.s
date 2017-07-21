.globl main
.text
main:
 	la t0,handler
 	csrrw zero, t0, 5 # set utvec
 	csrrsi zero, 1, 0 # set interrupt enable
 	lw zero, 0        # trigger trap
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
	csrrw zero, t0, 65	
	uret
	
