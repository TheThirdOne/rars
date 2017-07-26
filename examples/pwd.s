.globl main
.data
buf:
.space 100
error:
.asciz "An error occurred"

.text
main:
	# Get the current working directory (17) and store it into a buffer
        la a0, buf # the buffer to store the string
        li a1, 100 # the length of that buffer
        li a7, 17
        ecall
	
	# If there was an error (most likely buffer too short), switch to an error message
	la a1, buf
	beq a0, a1, not_error
	la a0, error
	la a1, error 
not_error:

	jal printStr
       	
       	# Store a newline in the buffer to print
       	la a1, buf
       	li t0, '\n'
       	sb t0, (a1)
       
	# Write (64) the buffer to stdout (1)	
       	li a0, 1
       	li a2, 1  # length of \n
       	li a7, 64
       	ecall 
       
	# Exit (93) with code 42 
        li a0, 42
        li a7, 93
        ecall


# input: a0 points to a null-terminated string
# output: a0 is the length of that string without the null termination
# only changes t0, t1, and a0
strlen:
	mv t0, a0
loop:
	lb t1, 0(a0)
	beqz t1, end
	addi a0, a0, 1
	j loop
end:
	sub a0, a0, t0
	ret
