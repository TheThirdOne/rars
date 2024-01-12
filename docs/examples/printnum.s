
.globl printDec
.globl printHex

.data
	#buffer for writing the digits, the address is behind intentionally because we are going to write the number backwards
	.space 12
temp: 
	.space 1  # null byte
.text 
# input: a0 = number to print
# no output
printDec:
    	# t0 = abs(a0);
        mv t0, a0
        bgez t0, positive
	xori t0, t0, -1
	addi t0, t0, 1
positive:      

        # convert t0 to digits in the buffer
        la a1, temp
        li t2, 10
decLoop:
        rem t3, t0, t2
        div t0, t0, t2 
        addi t3, t3, 0x30 # += '0'; converts a numerical 0-9 to the characters '0'-'9'
        addi a1, a1, -1
        sb t3, 0(a1)
        bnez t0, decLoop
        
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


printHex:
        # convert t0 to digits in the buffer
        la a1, temp
        li t2, 10
hexLoop:
        andi t3, a0, 0xF
        srli a0, a0, 4 
	blt t3, t2, offset
	addi t3, t3, 7 # += 'A' - '0' - 10; preps a numerical 10-15 to be converted to the characters 'A'-'F'
offset:	
        addi t3, t3, 0x30 # += '0'; converts a numerical 0-9 to the characters '0'-'9'
        addi a1, a1, -1
	sb t3, 0(a1)
        bnez a0, hexLoop

	# Add the 0x to the funct	
	addi a1, a1, -2
	li t0, 0x78 # x
	sb t0, 1(a1) 
	li t0, 0x30 # 0
	sb t0, 0(a1)

	# Write (64) from the temporary buffer to stdout (1) 
	li a0, 1
	la a2, temp
	sub a2, a2, a1
	li a7, 64
	ecall
	ret
