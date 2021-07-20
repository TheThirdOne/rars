.data
.align 3
amo_operand: .dword 0

.text
main:
  #-------------------------------------------------------------
  # Atomic tests
  #-------------------------------------------------------------

test_2:
 li a0, 0xffffffff80000000
 li a1, 0xfffffffffffff800
 la a3, amo_operand
 sw a0, 0(a3)
 amoxor.w a4, a1, (a3)
 li t2, 0xffffffff80000000
 li gp, 2
 bne a4, t2, fail

test_3:
 lw a5, 0(a3)
 li t2, 0x7ffff800
 li gp, 3
 bne a5, t2, fail

test_4:
 li a1, 0xc0000001
 amoxor.w a4, a1, (a3)
 li t2, 0x7ffff800
 li gp, 4
 bne a4, t2, fail

test_5:
 lw a5, 0(a3)
 li t2, 0xffffffffbffff801
 li gp, 5
 bne a5, t2, fail
 bne zero, gp, pass

fail:
 fence 1, 1
 slli gp, gp, 0x1
 ori gp, gp, 1
 li a7, 93
 mv a0, gp
 ecall

pass:
 fence 1, 1
 li gp, 1
 li a7, 93
 li a0, 42
 ecall