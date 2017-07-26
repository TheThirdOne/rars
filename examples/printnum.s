
.globl printNum
.data
	#buffer for writing the digits, the address is behind intentionally because we are going to write the number backwards
	.space 12
temp: 
	.space 1  # null byte
.text 
# input: a0 = number to print
# no output
printNum:
    	# t0 = abs(a0);
        mv t0, a0
        bgez t0, positive
	xori t0, t0, -1
	addi t0, t0, 1
positive:      

        # convert t0 to digits in the buffer
        la a1, temp
        li t2, 10
loop:
        rem t3, t0, t2
        div t0, t0, t2 
        addi t3, t3, 0x30 # += '0'; converts a numerical 0-9 to the characters '0'-'9'
        addi a1, a1, -1
        sb t3, 0(a1)
        bnez t0, loop
        
        # Add a negative sign if it was negative
        bgez a0, notNegative
        li t3, 0x2D # '-'
        addi a1, a1, -1
        sb t3, 0(a1)
notNegative:
	
	# Write (64) from the temporary buffer to stdout (1) 
	li a0, 1
	la a2, temp
	sub a2, a2, a1
	li a7, 64
	ecall
	ret
