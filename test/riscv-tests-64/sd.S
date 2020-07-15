# 1 "isa/rv64ui/sd.S"
# 1 "<built-in>"
# 1 "<command-line>"
# 31 "<command-line>"
# 1 "/usr/riscv64-linux-gnu/usr/include/stdc-predef.h" 1 3 4
# 32 "<command-line>" 2
# 1 "isa/rv64ui/sd.S"
# See LICENSE for license details.

#*****************************************************************************
# sd.S
#-----------------------------------------------------------------------------

# Test sd instruction.


# 1 "./riscv_test.h" 1





# 1 "./env/encoding.h" 1
# 7 "./riscv_test.h" 2
# 11 "isa/rv64ui/sd.S" 2
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
# 12 "isa/rv64ui/sd.S" 2


.text
 .globl _start
 _start: nop

  #-------------------------------------------------------------
  # Basic tests
  #-------------------------------------------------------------

  test_2: la x1, tdat
 li x2, 0x00aa00aa00aa00aa
 sd x2, 0(x1)
 ld x14, 0(x1)
 li x7, 0x00aa00aa00aa00aa
 li gp, 2
 bne x14, x7, fail

  test_3: la x1, tdat
 li x2, 0xaa00aa00aa00aa00
 sd x2, 8(x1)
 ld x14, 8(x1)
 li x7, 0xaa00aa00aa00aa00
 li gp, 3
 bne x14, x7, fail

  test_4: la x1, tdat
 li x2, 0x0aa00aa00aa00aa0
 sd x2, 16(x1)
 ld x14, 16(x1)
 li x7, 0x0aa00aa00aa00aa0
 li gp, 4
 bne x14, x7, fail

  test_5: la x1, tdat
 li x2, 0xa00aa00aa00aa00a
 sd x2, 24(x1)
 ld x14, 24(x1)
 li x7, 0xa00aa00aa00aa00a
 li gp, 5
 bne x14, x7, fail


  # Test with negative offset

  test_6: la x1, tdat8
 li x2, 0x00aa00aa00aa00aa
 sd x2, -24(x1)
 ld x14, -24(x1)
 li x7, 0x00aa00aa00aa00aa
 li gp, 6
 bne x14, x7, fail

  test_7: la x1, tdat8
 li x2, 0xaa00aa00aa00aa00
 sd x2, -16(x1)
 ld x14, -16(x1)
 li x7, 0xaa00aa00aa00aa00
 li gp, 7
 bne x14, x7, fail

  test_8: la x1, tdat8
 li x2, 0x0aa00aa00aa00aa0
 sd x2, -8(x1)
 ld x14, -8(x1)
 li x7, 0x0aa00aa00aa00aa0
 li gp, 8
 bne x14, x7, fail

  test_9: la x1, tdat8
 li x2, 0xa00aa00aa00aa00a
 sd x2, 0(x1)
 ld x14, 0(x1)
 li x7, 0xa00aa00aa00aa00a
 li gp, 9
 bne x14, x7, fail


  # Test with a negative base

  test_10: la x1, tdat9
 li x2, 0x1234567812345678
 addi x4, x1, -32
 sd x2, 32(x4)
 ld x5, 0(x1)
 li x7, 0x1234567812345678
 li gp, 10
 bne x5, x7, fail








  # Test with unaligned base

  test_11: la x1, tdat9
 li x2, 0x5821309858213098
 addi x1, x1, -3
 sd x2, 11(x1)
 la x4, tdat10
 ld x5, 0(x4)
 li x7, 0x5821309858213098
 li gp, 11
 bne x5, x7, fail

# 53 "isa/rv64ui/sd.S"
  #-------------------------------------------------------------
  # Bypassing tests
  #-------------------------------------------------------------

  test_12: li gp, 12
 li x4, 0
 li x1, 0xabbccdd
 la x2, tdat
 sd x1, 0(x2)
 ld x14, 0(x2)
 li x7, 0xabbccdd
 bne x14, x7, fail

  test_13: li gp, 13
 li x4, 0
 li x1, 0xaabbccd
 la x2, tdat
 nop
 sd x1, 8(x2)
 ld x14, 8(x2)
 li x7, 0xaabbccd
 bne x14, x7, fail

  test_14: li gp, 14
 li x4, 0
 li x1, 0xdaabbcc
 la x2, tdat
 nop
 nop
 sd x1, 16(x2)
 ld x14, 16(x2)
 li x7, 0xdaabbcc
 bne x14, x7, fail

  test_15: li gp, 15
 li x4, 0
 li x1, 0xddaabbc
 nop
 la x2, tdat
 sd x1, 24(x2)
 ld x14, 24(x2)
 li x7, 0xddaabbc
 bne x14, x7, fail

  test_16: li gp, 16
 li x4, 0
 li x1, 0xcddaabb
 nop
 la x2, tdat
 nop
 sd x1, 32(x2)
 ld x14, 32(x2)
 li x7, 0xcddaabb
 bne x14, x7, fail

  test_17: li gp, 17
 li x4, 0
 li x1, 0xccddaab
 nop
 nop
 la x2, tdat
 sd x1, 40(x2)
 ld x14, 40(x2)
 li x7, 0xccddaab
 bne x14, x7, fail


  test_18: li gp, 18
 li x4, 0
 la x2, tdat
 li x1, 0x00112233
 sd x1, 0(x2)
 ld x14, 0(x2)
 li x7, 0x00112233
 bne x14, x7, fail

  test_19: li gp, 19
 li x4, 0
 la x2, tdat
 li x1, 0x30011223
 nop
 sd x1, 8(x2)
 ld x14, 8(x2)
 li x7, 0x30011223
 bne x14, x7, fail

  test_20: li gp, 20
 li x4, 0
 la x2, tdat
 li x1, 0x33001122
 nop
 nop
 sd x1, 16(x2)
 ld x14, 16(x2)
 li x7, 0x33001122
 bne x14, x7, fail

  test_21: li gp, 21
 li x4, 0
 la x2, tdat
 nop
 li x1, 0x23300112
 sd x1, 24(x2)
 ld x14, 24(x2)
 li x7, 0x23300112
 bne x14, x7, fail

  test_22: li gp, 22
 li x4, 0
 la x2, tdat
 nop
 li x1, 0x22330011
 nop
 sd x1, 32(x2)
 ld x14, 32(x2)
 li x7, 0x22330011
 bne x14, x7, fail

  test_23: li gp, 23
 li x4, 0
 la x2, tdat
 nop
 nop
 li x1, 0x12233001
 sd x1, 40(x2)
 ld x14, 40(x2)
 li x7, 0x12233001
 bne x14, x7, fail


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
tdat1: .dword 0xdeadbeefdeadbeef
tdat2: .dword 0xdeadbeefdeadbeef
tdat3: .dword 0xdeadbeefdeadbeef
tdat4: .dword 0xdeadbeefdeadbeef
tdat5: .dword 0xdeadbeefdeadbeef
tdat6: .dword 0xdeadbeefdeadbeef
tdat7: .dword 0xdeadbeefdeadbeef
tdat8: .dword 0xdeadbeefdeadbeef
tdat9: .dword 0xdeadbeefdeadbeef
tdat10: .dword 0xdeadbeefdeadbeef

.align 4
 .global end_signature
 end_signature:
