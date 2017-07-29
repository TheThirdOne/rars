.globl main
.data 
errMsg:
.asciz "You must include at least one argument\n"
buf:
.space 512

.text
main:   
	beqz a0, error
	
	# trim off the name off the exectuable
	addi a0, a0, -1
	addi a1, a1,  4
	beqz a0, error

	# Free up s0, s1, s2 to be used	
	sw s0, -4(sp)
	sw s1, -8(sp)
	sw s2,-12(sp)
	addi sp, sp, -12
	
	mv s1, a0 # argc
        mv s2, a1 # argv
	
	

filesLoop:
	# Get the file descriptor to read from	
	lw a0, 0(s2)# name of the file
	li a1, 0    # read-only
	li a7, 1024 # open
	ecall
        
	# And save that file descriptor
	mv s0, a0
	
	# Skip if invalid name
	addi a0, a0, 1
	beqz a0, skip
	
bufferLoop:
	# Read (63) to buffer from the file
	mv a0, s0
	la a1, buf # destination buffer
	li a2, 512 # max buffer size
	li a7, 63
	ecall
	
	mv a2, a0  # move the length to write to a2
		
	# Write (64) from buffer to sysout (1)
	li a0,1      # stdout
 	la a1, buf   # the buffer
	li a7,64
	ecall
	
	# Copy more if the buffer was filled up
	addi a0, a0, -512
	beqz a0, bufferLoop
	
	# Close (57) the open file descriptor
	mv a0, s0
	li a7, 57
	ecall 

skip:
	# Try the next argument if there is one
	addi s1, s1, -1
	addi s2, s2, 4
	bnez s1, filesLoop

	addi sp, sp, 12
	lw s0, -4(sp)
	lw s1, -8(sp)
	lw s2, -12(sp)
	j done

error:
	# Write (64) the error message to stdout (1)
	li a0, 1
	la a1, errMsg
        li a2, 39
 	li a7, 64
	ecall
	
done:
	# Exit (93) with error code 0
	li a0,0 
	li a7, 93
	ecall
