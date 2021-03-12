#############################################################################
# Mastermind game for RARS RISC-V simulator
#  (https://github.com/TheThirdOne/rars)
# (c) Emmanuel Lazard
# Licence CC-BY
#############################################################################
# The purpose of the game is to guess a secret code chosen by the computer.
# A code is composed of 4 digits, each one being between 0 and 7.
# Repetitions are allowed (so that 1213 is a valid code for example).
#
# At each of your turns, you may enter 4 digits that will be your guess.
# The computer will then compare its secret code and your combination and
# will tell you the number of correct digits (same digit in the same position)
# and the number of incorrectly placed digits (digit is correct but not in the
# correct position). Each digit can only be used in one match
# (ex a correctly placed digit cannot also be considered incorrectly placed
# towards another digit in the secret code).
# You win when you have found the secret code, i.e. when the computer's answer
# is 4-0 (4 correctly placed digits).
#############################################################################


.eqv    NUMBER_DIGITS, 4
.eqv    DIGIT_MAX, 8

# Some macro definitions to print strings
.macro printStr (%str) 					# print a string
        .data
myLabel:
        .asciz %str
        .text
        li a7, 4
        la a0, myLabel
        ecall
        delayDisplay
.end_macro

.macro printLn 					# print a carriage-return
         .data
CRstring:
        .string "\n"
        .text
        li a7, 4
        la a0, CRstring
        ecall
        delayDisplay
.end_macro

.macro delayDisplay				# this macro is used to delay display
        li a0, 150				# preventing a bug found in rars 0e138d8
        li a7, 32				# see issue #108
        ecall
.end_macro


############### Start of program ###############
.data

SecretCode: .word 0,0
CopyCode:   .word 0,0
userNumber: .word 0,0	# we need more than 4 bytes because of the final '\n'

secretString: .string "solu\n"

###############
.text
.globl _start  # start address for linker

_start:
# First generate secret code and store it in SecretCode buffer
        jal ra, generateCode
        
#read user number and check for correctness
user:
        printStr("Please enter your guess: ")
        la a0, userNumber
        li a1, 10
        li a7, 8                 		# ReadString
        ecall                           # syscall

# Check if user input is the secret string
        la a0, userNumber				# compare the user string
        la a1, secretString				# with the secret string
        jal ra, strcmp
        bnez a0, noSecretString
        jal ra, printSol				# if so, print solution
        j user					        # and loop back
        
# First check if length of user string == 4 (in fact 5 with \n)
noSecretString:
        la a0, userNumber
        jal ra, strlen					# get string length
        addi a0, a0, -5					# and substract 5
        bnez a0, userError				# if not 0, length isn't 5 so error

# Next check if each digit is between 0 and DIGIT_MAX-1
        li t2, '\n'						# '\n' to check for end of string
        li t3, DIGIT_MAX				
        la t0, userNumber
nextDigit:
        lb t1, 0(t0)					# load next digit
        beq t1, t2, afterCheck			# if '\n', we're done checking
        addi t1, t1, -48				# transform digit from ASCII to number
        sb t1, 0(t0)					# and store it back
        addi t0, t0, 1					# advance pointer to next digit
        bltz t1, userError				# is digit < 0? Yes -> userError	
        bge t1, t3, userError			# is digit >= DIGIT_MAX? Yes -> userError	
        j nextDigit						#  otherwise loop back


afterCheck:
# We now have a user number of 4 digits, each between 0 and DIGIT_MAX-1
#  stored in userNumber buffer.
# As we'll modify the secret code to check for correspondances,
#  we'll work on a copy of the code.

# Copy SecretCode into CopyCode
#  We use the fact that the code is 4 digits long to use word transfer
        la a6, SecretCode
        la a0, CopyCode
        lw t0, 0(a6)					# load full 4 bytes secret code
        sw t0, 0(a0)					# and store it in CopyCode


# First part of the algorithm: check for correct matches 
#  -> same digit at the same place
#  and put result in s11 register to be displayed

        la a1, userNumber				# a0=CopyCode, a1=userNumber 
        li s11, 0						# matches counter
        li t1, -1						# dummy values to replace digit
        li t2, -2						#  when a match is found
        li t3, 4						# number of digits to check
loopCorrectMatches:
        lb t4, 0(a0)					# load both digits
        lb t5, 0(a1)					# and compare them
        bne t4, t5, noMatch
        	# Here we have a match between digits
        addi s11, s11, 1					# increment matches counter
        sb t1, 0(a0)					#  and replace digits
        sb t2, 0(a1)					#  with dummy values
noMatch:
        addi a0, a0, 1					# point on next digits
        addi a1, a1, 1
        addi t3, t3, -1					# until no more to check
        bnez t3, loopCorrectMatches

# We now have number of correct matches in s11
        printStr("Number of correct matches: ")
        mv a0, s11
        li a7, 1						# PrintInt
        ecall							# syscall
        delayDisplay                    # to prevent printing bug
        printLn  						# print carriage-return
        addi s11, s11, -4				# substract 4 to number of correct matches
        beqz s11, userWin				# and if null, it's a win!


# We now have to check for incorrect matches
#  -> same digit but not the same place
# It's a double loop going over both numbers.

        li s11, 0						# matches counter
        li t1, -1						# dummy values to replace digit
        li t2, -2						#  when a match is found
        la a0, CopyCode					# a0=CopyCode
        li s0, 4						# 4 digits to check

outerLoop:
        la a1, userNumber				# a1=userNumber
        li s1, 4						# 4 digits to check
innerLoop:
        beq s0, s1, noMatch2			# don't check digit at the same place	
        lb t4, 0(a0)
        lb t5, 0(a1)
        bne t4, t5, noMatch2
        	# Here we have a match between digits (at different places)
        addi s11, s11, 1
        sb t1, 0(a0)
        sb t2, 0(a1)
noMatch2:
        addi a1, a1, 1					# advance inner loop pointer
        addi s1, s1, -1					# until no more digit
        bnez s1, innerLoop

        addi a0, a0, 1					# advance outer loop pointer
        addi s0, s0, -1					# until no more digit
        bnez s0, outerLoop

# We now have number of incorrect position matches in s11
        printStr("Number of incorrect positions: ")
        mv a0, s11
        li a7, 1						# PrintInt
        ecall							# syscall
        delayDisplay                    # to prevent printing bug
        printLn   						# print carriage-return
        j user							# and go back to have another guess!


# display error message
userError:
        printStr("Guess should be 4 digits, each between 0 and 7\n")
        j user							# and go back to have another guess!
    

# We arrive here if the user won by correctly guessing all 4 digits
userWin:
        printStr("Congratulations!\n")
        li a7, 10                 		# Exit 
        ecall                           # syscall


############### functions ###############

#####
# generateCode(): generate a random secret code
#  having NUMBER_DIGITS digits, each from 0 to DIGIT_MAX-1
#  and store it in SecretCode buffer, one digit per byte.
generateCode:
        la t0, SecretCode               # point to storage buffer
        li t1, NUMBER_DIGITS            # number of digits to generate
loop:
        li a0, 0
        li a1, DIGIT_MAX                # upper bound for random digit
        li a7, 42                       # RandIntRange
        ecall                           # syscall
        sb a0, 0(t0)                    # store digit
        addi t0, t0, 1                  # update pointer
        addi t1, t1, -1                 # and jump back
        bnez t1, loop                   #  unless no more to generate
        ret
#####

#####
# printSol(): print solution stored in SecretCode buffer
#                         one digit at a time
printSol:
        la t0, SecretCode               # load address of buffer
        li t1, NUMBER_DIGITS            # initialize number of digits to print
loopSol:
        lb a0, 0(t0)                    # load digit
        li a7, 1                        # printInt
        ecall                           # syscall
        delayDisplay                    # to prevent printing bug
        addi t0, t0, 1                  # go to next digit
        addi t1, t1, -1                 # and jump back
        bnez t1, loopSol                #  unless no more to print
        printLn
        ret

############### String utility fonctions ###############

#####
 # strlen(): compute string length
 #      a0: pointer on start of string
 #      -> a0: returns length of string
strlen:
        li t1, -1                       # initialize counter t1 <- -1
loopStrlen:
        lbu t0, 0(a0)                   # load character
        addi a0, a0, 1                  # update pointer to next char
        addi t1, t1, 1                  #  and increment counter 
        bnez t0, loopStrlen             # until end of string
        mv a0, t1                       # return counter value
        ret
#####


#####
 # strcpy(): copy a string
 #      a0: pointer on start of source string
 #      a1: pointer on start of destination buffer
strcpy:
        lb t0, 0(a0)                    # load character
        sb t0, 0(a1)                    # and store it in buffer
        addi a0, a0, 1                  # update pointers
        addi a1, a1, 1                  #  to next character
        bnez t0, strcpy                 # jump back to start unless
        ret                             #  end of string is reached
#####

#####
 # strcmp(): compare two strings
 #      a0: pointer on start of string 1
 #      a1: pointer on start of string 2
 #      -> a0: 0 if both strings are equal, different from 0 otherwise
strcmp:
        lbu t0, 0(a0)                   # load character from string 1
        lbu t1, 0(a1)                   # and load character from string 2
        sub t1, t0, t1                  # "compare" characters
        addi a0, a0, 1                  # update pointers
        addi a1, a1, 1                  #  to next character
        beqz t0, endStrcmp              # jump if end of string is reached
        beqz t1, strcmp                 # otherwise jump back to start
endStrcmp:                              #  unless characters are different
        mv      a0, t1                  # return result of last comparaison
        ret
#####
