.globl printf
.text
# input: a0 = template string, a1-a7=arguments in order for that template
# no output
printf:
	mv t0, a0

	# loop through the template until we hit a %
loop:
	lb t1, 0(t0)
	beqz t1, done
	li t2, 0x25 # %
	beq t1, t2, outputNumber
	addi t0, t0, 1
	j loop
outputNumber:
	# Save arguments	
	addi sp, sp, -36
	sw t0, 4(sp)
	sw a1, 8(sp)
	sw a2, 12(sp)
	sw a3, 16(sp)
	sw a4, 20(sp)
	sw a5, 24(sp)
	sw a6, 28(sp)
	sw a7, 32(sp)
	sw ra, 36(sp)

	# Print out all of the characters we have interated over thus far
	# Write (64) from a0-t0 to stdout (1)
	sub a2, t0, a0 # get the length to print
        mv a1, a0      # the start of the buffer
        li a0, 1
        li a7, 64
        ecall
        
        # Print out the number
        lw a0, 8(sp)
        call printNum
        
        # Reload offset (shift out a1)
        lw t0, 4(sp)
	lw a1, 12(sp)
	lw a2, 16(sp)
	lw a3, 20(sp)
	lw a4, 24(sp)
	lw a5, 28(sp)
	lw a6, 32(sp)
	lw ra, 36(sp)
	addi sp, sp, 36
	
	# Skip past the %
	addi a0, t0, 1
	j printf
done:
	# Print out the characters till the end
	# Write (64) from the buffer to stdout (1)
	sub a2, t0, a0 # get the length to print
        mv a1, a0 # the start of the buffer
        li a0, 1
        li a7, 64
        ecall
       	
	ret
