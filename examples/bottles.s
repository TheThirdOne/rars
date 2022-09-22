
.globl  main
.data
templ1:
	.string " bottles of beer of the wall. "
templ2:
	.string " bottles of beer. Take one down pass it around. "
templ3:	
	.string " bottles of beer on the wall\n"

.text
main:
	# Save state to free up s0, s1, and ra for use
        addi     sp, sp, -16
        sw      s1,4(sp)
        sw      s0,8(sp)
        sw      ra,12(sp)

	# load strings and int(99) for the loop
        li      s0,99
        la      s6, templ1 #templ1 address
        la	s7, templ2 #templ2 address
        la	s8, templ3 #templ3 address
loop:
        mv      a1,s0
	addi	s0,s0, -1
        mv      a3,s0
        mv	s9, a1     #bottle
        mv	s10, a3    #bottle-1
        jal print
        bnez    s0,loop

	# Load state
        lw      ra,12(sp)
        lw      s0,8(sp)
        lw      s1,4(sp)
        addi     sp,sp,16
        
exit:	# Exit (93) with code 0
        li a0, 0
        li a7, 93
        ecall
        ebreak
        
print:	#Set print mode, load stuff to print, print
	li	a7, 1 #num
	mv	a0, s9
	ecall
	
	li	a7, 4 #templ1
	mv	a0, s6
	ecall
	
	li	a7, 1 #num
	mv	a0, s9
	ecall
	
	li	a7, 4 #templ2
	mv	a0, s7
	ecall

	li	a7, 1 #num
	mv	a0, s10
	ecall
		
	li	a7, 4 #templ3
	mv	a0, s8
	ecall	
	
	ret
