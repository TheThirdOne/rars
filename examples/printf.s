.globl printf
.text
# input: a0 = template string, a1-a7=arguments in order for that template
# no output
printf:
	# Make room to save stuff
	addi sp, sp, -36
	sw s0, 4(sp)
	sw s1, 8(sp)
	sw s2, 12(sp)
	sw s3, 16(sp)
	sw s4, 20(sp)
	sw s5, 24(sp)
	sw s6, 28(sp)
	sw s7, 32(sp)
	sw ra, 36(sp)

	# Save template string
	mv s0, a0
	mv t0, a0

	# Save arguments	
	mv s1, a1
	mv s2, a2
	mv s3, a3
	mv s4, a4
	mv s5, a5
	mv s6, a6
	mv s7, a7

	# loop through the template until we hit a %
loop:
	lb t1, 0(t0)
	beqz t1, done
	li t2, 0x25 # %
	beq t1, t2, outputArg
	addi t0, t0, 1
	j loop
outputArg:

	# Print out all of the characters we have interated over thus far
	# Write (64) from a0-t0 to stdout (1)
        li a0, 1
        mv a1, s0      # the start of the buffer
	sub a2, t0, s0 # get the length to print
        li a7, 64
        ecall
       
	# Shift the index on the template forward
	addi s0, t0, 2

	lb t0, -1(s0)
	li t1, 0x64 # 'd'
	beq t0, t1,  outputNum
	li t1, 0x78 # 'x'
	beq t0, t1, outputHex
	# Exit if the character following if not d or x
	mv t0, s0
	j done
outputHex:
	mv a0, s1
	call printHex
	j noOutput
outputNum:
        mv a0, s1
        call printDec
noOutput:
	# Shift arguments down 
	mv s1, s2
	mv s2, s3
	mv s3, s4
	mv s4, s5
	mv s5, s6
	mv s6, s7

	# Reinit t0	
	mv t0, s0

	j loop
done:
	# Print out the characters till the end
	# Write (64) from the buffer to stdout (1)
        li a0, 1
        mv a1, s0 # the start of the buffer
	sub a2, t0, s0 # get the length to print
        li a7, 64
        ecall
       
	# Restore saved state 
	lw s0, 4(sp)
	lw s1, 8(sp)
	lw s2, 12(sp)
	lw s3, 16(sp)
	lw s4, 20(sp)
	lw s5, 24(sp)
	lw s6, 28(sp)
	lw s7, 32(sp)
	lw ra, 36(sp)
	addi sp, sp, 36

	ret
