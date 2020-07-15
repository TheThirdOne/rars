# 1 "isa/rv64ui/sb.S"
# 1 "<built-in>"
# 1 "<command-line>"
# 31 "<command-line>"
# 1 "/usr/riscv64-linux-gnu/usr/include/stdc-predef.h" 1 3 4
# 32 "<command-line>" 2
# 1 "isa/rv64ui/sb.S"
# See LICENSE for license details.

#*****************************************************************************
# sb.S
#-----------------------------------------------------------------------------

# Test sb instruction.


# 1 "./riscv_test.h" 1





# 1 "./env/encoding.h" 1
# 7 "./riscv_test.h" 2
# 11 "isa/rv64ui/sb.S" 2
# 1 "./isa/macros/scalar/test_macros.h" 1






#-----------------------------------------------------------------------
# Helper macros
#-----------------------------------------------------------------------
# 20 "./isa/macros/scalar/test_macros.h"
# We use a macro hack to simpify code generation for various numbers
# of bubble cycles.
# 36 "./isa/macros/scalar/test_macros.h"
#-----------------------------------------------------------------------
# RV64UI MACROS
#-----------------------------------------------------------------------

#-----------------------------------------------------------------------
# Tests for instructions with immediate operand
#-----------------------------------------------------------------------
# 86 "./isa/macros/scalar/test_macros.h"
#-----------------------------------------------------------------------
# Tests for an instruction with register operands
#-----------------------------------------------------------------------
# 110 "./isa/macros/scalar/test_macros.h"
#-----------------------------------------------------------------------
# Tests for an instruction with register-register operands
#-----------------------------------------------------------------------
# 199 "./isa/macros/scalar/test_macros.h"
#-----------------------------------------------------------------------
# Test memory instructions
#-----------------------------------------------------------------------
# 307 "./isa/macros/scalar/test_macros.h"
#-----------------------------------------------------------------------
# Test jump instructions
#-----------------------------------------------------------------------
# 330 "./isa/macros/scalar/test_macros.h"
#-----------------------------------------------------------------------
# RV64UF MACROS
#-----------------------------------------------------------------------

#-----------------------------------------------------------------------
# Tests floating-point instructions
#-----------------------------------------------------------------------


# 0f:7fc00000

# 0f:7f800001

# 0d:7ff8000000000000

# 0d:7ff0000000000001
# 594 "./isa/macros/scalar/test_macros.h"
#-----------------------------------------------------------------------
# Pass and fail code (assumes test num is in gp)
#-----------------------------------------------------------------------
# 606 "./isa/macros/scalar/test_macros.h"
#-----------------------------------------------------------------------
# Test data section
#-----------------------------------------------------------------------
# 12 "isa/rv64ui/sb.S" 2


.text
 .globl _start
 _start: nop

  #-------------------------------------------------------------
  # Basic tests
  #-------------------------------------------------------------

  test_2: la x1, tdat
 li x2, 0xffffffffffffffaa
 sb x2, 0(x1)
 lb x14, 0(x1)
 li x7, 0xffffffffffffffaa
 li gp, 2
 bne x14, x7, fail

  test_3: la x1, tdat
 li x2, 0x0000000000000000
 sb x2, 1(x1)
 lb x14, 1(x1)
 li x7, 0x0000000000000000
 li gp, 3
 bne x14, x7, fail

  test_4: la x1, tdat
 li x2, 0xffffffffffffefa0
 sb x2, 2(x1)
 lh x14, 2(x1)
 li x7, 0xffffffffffffefa0
 li gp, 4
 bne x14, x7, fail

  test_5: la x1, tdat
 li x2, 0x000000000000000a
 sb x2, 3(x1)
 lb x14, 3(x1)
 li x7, 0x000000000000000a
 li gp, 5
 bne x14, x7, fail


  # Test with negative offset

  test_6: la x1, tdat8
 li x2, 0xffffffffffffffaa
 sb x2, -3(x1)
 lb x14, -3(x1)
 li x7, 0xffffffffffffffaa
 li gp, 6
 bne x14, x7, fail

  test_7: la x1, tdat8
 li x2, 0x0000000000000000
 sb x2, -2(x1)
 lb x14, -2(x1)
 li x7, 0x0000000000000000
 li gp, 7
 bne x14, x7, fail

  test_8: la x1, tdat8
 li x2, 0xffffffffffffffa0
 sb x2, -1(x1)
 lb x14, -1(x1)
 li x7, 0xffffffffffffffa0
 li gp, 8
 bne x14, x7, fail

  test_9: la x1, tdat8
 li x2, 0x000000000000000a
 sb x2, 0(x1)
 lb x14, 0(x1)
 li x7, 0x000000000000000a
 li gp, 9
 bne x14, x7, fail


  # Test with a negative base

  test_10: la x1, tdat9
 li x2, 0x12345678
 addi x4, x1, -32
 sb x2, 32(x4)
 lb x5, 0(x1)
 li x7, 0x78
 li gp, 10
 bne x5, x7, fail








  # Test with unaligned base

  test_11: la x1, tdat9
 li x2, 0x00003098
 addi x1, x1, -6
 sb x2, 7(x1)
 la x4, tdat10
 lb x5, 0(x4)
 li x7, 0xffffffffffffff98
 li gp, 11
 bne x5, x7, fail

# 53 "isa/rv64ui/sb.S"
  #-------------------------------------------------------------
  # Bypassing tests
  #-------------------------------------------------------------

  test_12: li gp, 12
 li x4, 0
 li x1, 0xffffffffffffffdd
 la x2, tdat
 sb x1, 0(x2)
 lb x14, 0(x2)
 li x7, 0xffffffffffffffdd
 bne x14, x7, fail

  test_13: li gp, 13
 li x4, 0
 li x1, 0xffffffffffffffcd
 la x2, tdat
 nop
 sb x1, 1(x2)
 lb x14, 1(x2)
 li x7, 0xffffffffffffffcd
 bne x14, x7, fail

  test_14: li gp, 14
 li x4, 0
 li x1, 0xffffffffffffffcc
 la x2, tdat
 nop
 nop
 sb x1, 2(x2)
 lb x14, 2(x2)
 li x7, 0xffffffffffffffcc
 bne x14, x7, fail

  test_15: li gp, 15
 li x4, 0
 li x1, 0xffffffffffffffbc
 nop
 la x2, tdat
 sb x1, 3(x2)
 lb x14, 3(x2)
 li x7, 0xffffffffffffffbc
 bne x14, x7, fail

  test_16: li gp, 16
 li x4, 0
 li x1, 0xffffffffffffffbb
 nop
 la x2, tdat
 nop
 sb x1, 4(x2)
 lb x14, 4(x2)
 li x7, 0xffffffffffffffbb
 bne x14, x7, fail

  test_17: li gp, 17
 li x4, 0
 li x1, 0xffffffffffffffab
 nop
 nop
 la x2, tdat
 sb x1, 5(x2)
 lb x14, 5(x2)
 li x7, 0xffffffffffffffab
 bne x14, x7, fail


  test_18: li gp, 18
 li x4, 0
 la x2, tdat
 li x1, 0x33
 sb x1, 0(x2)
 lb x14, 0(x2)
 li x7, 0x33
 bne x14, x7, fail

  test_19: li gp, 19
 li x4, 0
 la x2, tdat
 li x1, 0x23
 nop
 sb x1, 1(x2)
 lb x14, 1(x2)
 li x7, 0x23
 bne x14, x7, fail

  test_20: li gp, 20
 li x4, 0
 la x2, tdat
 li x1, 0x22
 nop
 nop
 sb x1, 2(x2)
 lb x14, 2(x2)
 li x7, 0x22
 bne x14, x7, fail

  test_21: li gp, 21
 li x4, 0
 la x2, tdat
 nop
 li x1, 0x12
 sb x1, 3(x2)
 lb x14, 3(x2)
 li x7, 0x12
 bne x14, x7, fail

  test_22: li gp, 22
 li x4, 0
 la x2, tdat
 nop
 li x1, 0x11
 nop
 sb x1, 4(x2)
 lb x14, 4(x2)
 li x7, 0x11
 bne x14, x7, fail

  test_23: li gp, 23
 li x4, 0
 la x2, tdat
 nop
 nop
 li x1, 0x01
 sb x1, 5(x2)
 lb x14, 5(x2)
 li x7, 0x01
 bne x14, x7, fail


  li a0, 0xef
  la a1, tdat
  sb a0, 3(a1)

  bne x0, gp, pass
 fail: li a0, 0
 li a7, 93
 ecall
 pass: li a0, 42
 li a7, 93
 ecall



  .data
 .data 
 .align 4
 .global begin_signature
 begin_signature:

 

tdat:
tdat1: .byte 0xef
tdat2: .byte 0xef
tdat3: .byte 0xef
tdat4: .byte 0xef
tdat5: .byte 0xef
tdat6: .byte 0xef
tdat7: .byte 0xef
tdat8: .byte 0xef
tdat9: .byte 0xef
tdat10: .byte 0xef

.align 4
 .global end_signature
 end_signature:
