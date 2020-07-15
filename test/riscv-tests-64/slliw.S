# 1 "isa/rv64ui/slliw.S"
# 1 "<built-in>"
# 1 "<command-line>"
# 31 "<command-line>"
# 1 "/usr/riscv64-linux-gnu/usr/include/stdc-predef.h" 1 3 4
# 32 "<command-line>" 2
# 1 "isa/rv64ui/slliw.S"
# See LICENSE for license details.

#*****************************************************************************
# slliw.S
#-----------------------------------------------------------------------------

# Test slliw instruction.


# 1 "./riscv_test.h" 1





# 1 "./env/encoding.h" 1
# 7 "./riscv_test.h" 2
# 11 "isa/rv64ui/slliw.S" 2
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
# 12 "isa/rv64ui/slliw.S" 2


.text
 .globl _start
 _start: nop

  #-------------------------------------------------------------
  # Arithmetic tests
  #-------------------------------------------------------------

  test_2: li x1, 0x0000000000000001
 slliw x14, x1, 0
 li x7, 0x0000000000000001
 li gp, 2
 bne x14, x7, fail

  test_3: li x1, 0x0000000000000001
 slliw x14, x1, 1
 li x7, 0x0000000000000002
 li gp, 3
 bne x14, x7, fail

  test_4: li x1, 0x0000000000000001
 slliw x14, x1, 7
 li x7, 0x0000000000000080
 li gp, 4
 bne x14, x7, fail

  test_5: li x1, 0x0000000000000001
 slliw x14, x1, 14
 li x7, 0x0000000000004000
 li gp, 5
 bne x14, x7, fail

  test_6: li x1, 0x0000000000000001
 slliw x14, x1, 31
 li x7, 0xffffffff80000000
 li gp, 6
 bne x14, x7, fail


  test_7: li x1, 0xffffffffffffffff
 slliw x14, x1, 0
 li x7, 0xffffffffffffffff
 li gp, 7
 bne x14, x7, fail

  test_8: li x1, 0xffffffffffffffff
 slliw x14, x1, 1
 li x7, 0xfffffffffffffffe
 li gp, 8
 bne x14, x7, fail

  test_9: li x1, 0xffffffffffffffff
 slliw x14, x1, 7
 li x7, 0xffffffffffffff80
 li gp, 9
 bne x14, x7, fail

  test_10: li x1, 0xffffffffffffffff
 slliw x14, x1, 14
 li x7, 0xffffffffffffc000
 li gp, 10
 bne x14, x7, fail

  test_11: li x1, 0xffffffffffffffff
 slliw x14, x1, 31
 li x7, 0xffffffff80000000
 li gp, 11
 bne x14, x7, fail


  test_12: li x1, 0x0000000021212121
 slliw x14, x1, 0
 li x7, 0x0000000021212121
 li gp, 12
 bne x14, x7, fail

  test_13: li x1, 0x0000000021212121
 slliw x14, x1, 1
 li x7, 0x0000000042424242
 li gp, 13
 bne x14, x7, fail

  test_14: li x1, 0x0000000021212121
 slliw x14, x1, 7
 li x7, 0xffffffff90909080
 li gp, 14
 bne x14, x7, fail

  test_15: li x1, 0x0000000021212121
 slliw x14, x1, 14
 li x7, 0x0000000048484000
 li gp, 15
 bne x14, x7, fail

  test_16: li x1, 0x0000000021212121
 slliw x14, x1, 31
 li x7, 0xffffffff80000000
 li gp, 16
 bne x14, x7, fail


  # Verify that shifts ignore top 32 (using true 64-bit values)

  test_44: li x1, 0xffffffff12345678
 slliw x14, x1, 0
 li x7, 0x0000000012345678
 li gp, 44
 bne x14, x7, fail

  test_45: li x1, 0xffffffff12345678
 slliw x14, x1, 4
 li x7, 0x0000000023456780
 li gp, 45
 bne x14, x7, fail

  test_46: li x1, 0x0000000092345678
 slliw x14, x1, 0
 li x7, 0xffffffff92345678
 li gp, 46
 bne x14, x7, fail

  test_47: li x1, 0x0000000099345678
 slliw x14, x1, 4
 li x7, 0xffffffff93456780
 li gp, 47
 bne x14, x7, fail


  #-------------------------------------------------------------
  # Source/Destination tests
  #-------------------------------------------------------------

  test_17: li x1, 0x00000001
 slliw x1, x1, 7
 li x7, 0x00000080
 li gp, 17
 bne x1, x7, fail


  #-------------------------------------------------------------
  # Bypassing tests
  #-------------------------------------------------------------

  test_18: li x4, 0
 li x1, 0x0000000000000001
 slliw x14, x1, 7
 addi x6, x14, 0
 li x7, 0x0000000000000080
 li gp, 18
 bne x6, x7, fail

  test_19: li x4, 0
 li x1, 0x0000000000000001
 slliw x14, x1, 14
 nop
 addi x6, x14, 0
 li x7, 0x0000000000004000
 li gp, 19
 bne x6, x7, fail

  test_20: li x4, 0
 li x1, 0x0000000000000001
 slliw x14, x1, 31
 nop
 nop
 addi x6, x14, 0
 li x7, 0xffffffff80000000
 li gp, 20
 bne x6, x7, fail


  test_21: li x4, 0
 li x1, 0x0000000000000001
 slliw x14, x1, 7
 li x7, 0x0000000000000080
 li gp, 21
 bne x14, x7, fail

  test_22: li x4, 0
 li x1, 0x0000000000000001
 nop
 slliw x14, x1, 14
 li x7, 0x0000000000004000
 li gp, 22
 bne x14, x7, fail

  test_23: li x4, 0
 li x1, 0x0000000000000001
 nop
 nop
 slliw x14, x1, 31
 li x7, 0xffffffff80000000
 li gp, 23
 bne x14, x7, fail


  test_24: slliw x1, x0, 31
 li x7, 0
 li gp, 24
 bne x1, x7, fail

  test_25: li x1, 31
 slliw x0, x1, 28
 li x7, 0
 li gp, 25
 bne x0, x7, fail


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

 

.align 4
 .global end_signature
 end_signature:
