
.globl  main
.data
template:
	.string "% bottles of beer of the wall. % bottles of beer. Take one down pass it around. % bottles of beer on the wall\n"

.text
main:
	# Save state to free up s0, s1, and ra for use
        addi     sp, sp, -16
        sw      s1,4(sp)
        sw      s0,8(sp)
        sw      ra,12(sp)


	# Call printf(template, x, x, x-1) for 0 < x < 100 
        li      s0,99
loop:
        mv      a1,s0
	mv	a2,s0
	addi	s0,s0, -1
        mv      a3,s0
        la      a0, template
        call    printf
        bnez    s0,loop

	# Load state
        lw      ra,12(sp)
        lw      s0,8(sp)
        lw      s1,4(sp)
        addi     sp,sp,16
        
	# Exit (93) with code 0
        li a0, 0
        li a7, 93
        ecall
        ebreak
        
      
