.globl main
.text
main:

 	li t0, 1
 	csrrw t1, 64, t0
 	li gp, 1
 	bnez t1, failure # CSRRW didn't move the existing 0 in uscratch
 	csrrwi t1, 64, 0
 	li gp, 2
 	bne t0, t1, failure # CSRRW ddin't omve 1 into uscratch
 	csrrsi t1, 64, 2
 	li gp, 3
 	bnez t1, failure  # CSRRWI failed to reset uscratch to 0
 	csrr t1, 64
 	li t2, 2
	li gp, 4

# TODO : a few more comments
 	bne t1,t2, failure
 	csrrs t1, 64, t0
	li gp, 5
 	bne t1,t2, failure
 	csrrci t1, 64, 2
 	li t2, 3
	li gp, 6
 	bne t1, t2, failure
 	csrrc t1, 64, t0
	li gp, 7
 	bne t1, t0, failure
 	csrr t1, 64
	li gp, 8
 	bnez t1, failure
 	
 	# Table 25.3
 	csrr t1, 1 # fflags
 	csrr t1, 2 # frm
 	csrr t1, 3 # fcsr
 	csrr t1, 0xC00 # cycle
 	csrr t1, 0xC02 # instret
 	csrr t1, 0xC80 # cycleh
 	csrr t1, 0xC82 # instreth

	# Section 22.1
 	csrr t1, 0    # ustatus
 	csrr t1, 4    # uie
 	csrr t1, 5    # utvec
 	csrr t1, 0x40 # uscratch
 	csrr t1, 0x41 # uepc
 	csrr t1, 0x42 # ucause
 	csrr t1, 0x43 # utval
 	csrr t1, 0x44 # uip
 	

 	# Ensure csrr[cs]i? do not write to the CSR
 	csrrc t1, 0xC00, x0
 	csrrs t1, 0xC00, x0
 	
 	csrrci t1, 0xC00, 0
 	csrrsi t1, 0xC00, 0
 	
 	
 	# WPRI confirmations
 	li t0, -1
 	# Section 22.2
 	csrrw t1, 0, t0
 	csrr t1, 0
 	li t2, 0x11
 	bne t1, t2, failure
 	csrrwi t1, 0, 0
 	# Section 11.2
 	csrrw t1, 3, t0
 	csrr t1,3
 	li t2, 0xFF
 	bne t1, t2, failure
 	csrrwi t1, 3, 0
 	# TODO: the other trap handling registers following 22.3 more complete writeup
 	
 	# TODO: fsr <-> frm,fflags mappings if not already handled
 	
 	la t0,handler
 	csrrw zero, 5, t0 # set utvec
 	csrrsi zero, 0, 1 # set interrupt enable
 	
 	# Illegal CSRs should error on read or write
 	csrr x0, 6
 	csrrw t1, 6, x0
 	
 	# All counters should not be writeable
 	csrrwi t1, 0xC00, 0 # cycle
 	csrrwi t1, 0xC02, 0 # instret
 	csrrwi t1, 0xC80, 0 # cycleh
 	csrrwi t1, 0xC82, 0 # instreth
 	
	# Check other instructions aswell to ensure they are cause an exception
 	csrrw t1, 0xC00, x0
 	
 	li t0, 1
 	csrrc t1, 0xC00, t1
 	csrrs t1, 0xC00, t1
 	
 	csrrci t1, 0xC00, 1
 	csrrsi t1, 0xC00, 1
 	
 	li t0, 11 # There should be 11 total erroring lines above
 	bne t0, s0, failure
 	j success
 	
failure:
	li a0, 0
	li a7, 93
	ecall
success:
 	li a0, 42
 	li a7, 93
 	ecall
 handler:
 	addi s0, s0, 1
	csrr t0, 65
	addi t0, t0, 4
	csrrw zero, 65, t0
	uret
	
