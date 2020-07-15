# 1 "isa/rv64ui/ori.S"
# 1 "<built-in>"
# 1 "<command-line>"
# 31 "<command-line>"
# 1 "/usr/riscv64-linux-gnu/usr/include/stdc-predef.h" 1 3 4
# 32 "<command-line>" 2
# 1 "isa/rv64ui/ori.S"
# See LICENSE for license details.

#*****************************************************************************
# ori.S
#-----------------------------------------------------------------------------

# Test ori instruction.


# 1 "./riscv_test.h" 1





# 1 "./env/encoding.h" 1
# 7 "./riscv_test.h" 2
# 11 "isa/rv64ui/ori.S" 2
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
# 12 "isa/rv64ui/ori.S" 2


.text
 .globl _start
 _start: nop

  #-------------------------------------------------------------
  # Logical tests
  #-------------------------------------------------------------

  test_2: li x1, 0xffffffffff00ff00
 ori x14, x1, 0xffffff0f
 li x7, 0xffffffffffffff0f
 li gp, 2
 bne x14, x7, fail

  test_3: li x1, 0x000000000ff00ff0
 ori x14, x1, 0x0f0
 li x7, 0x000000000ff00ff0
 li gp, 3
 bne x14, x7, fail

  test_4: li x1, 0x0000000000ff00ff
 ori x14, x1, 0x70f
 li x7, 0x0000000000ff07ff
 li gp, 4
 bne x14, x7, fail

  test_5: li x1, 0xfffffffff00ff00f
 ori x14, x1, 0x0f0
 li x7, 0xfffffffff00ff0ff
 li gp, 5
 bne x14, x7, fail


  #-------------------------------------------------------------
  # Source/Destination tests
  #-------------------------------------------------------------

  test_6: li x1, 0xff00ff00
 ori x1, x1, 0x0f0
 li x7, 0xff00fff0
 li gp, 6
 bne x1, x7, fail


  #-------------------------------------------------------------
  # Bypassing tests
  #-------------------------------------------------------------

  test_7: li x4, 0
 li x1, 0x000000000ff00ff0
 ori x14, x1, 0x0f0
 addi x6, x14, 0
 li x7, 0x000000000ff00ff0
 li gp, 7
 bne x6, x7, fail

  test_8: li x4, 0
 li x1, 0x0000000000ff00ff
 ori x14, x1, 0x70f
 nop
 addi x6, x14, 0
 li x7, 0x0000000000ff07ff
 li gp, 8
 bne x6, x7, fail

  test_9: li x4, 0
 li x1, 0xfffffffff00ff00f
 ori x14, x1, 0x0f0
 nop
 nop
 addi x6, x14, 0
 li x7, 0xfffffffff00ff0ff
 li gp, 9
 bne x6, x7, fail


  test_10: li x4, 0
 li x1, 0x000000000ff00ff0
 ori x14, x1, 0x0f0
 li x7, 0x000000000ff00ff0
 li gp, 10
 bne x14, x7, fail

  test_11: li x4, 0
 li x1, 0x0000000000ff00ff
 nop
 ori x14, x1, 0xffffff0f
 li x7, 0xffffffffffffffff
 li gp, 11
 bne x14, x7, fail

  test_12: li x4, 0
 li x1, 0xfffffffff00ff00f
 nop
 nop
 ori x14, x1, 0x0f0
 li x7, 0xfffffffff00ff0ff
 li gp, 12
 bne x14, x7, fail


  test_13: ori x1, x0, 0x0f0
 li x7, 0x0f0
 li gp, 13
 bne x1, x7, fail

  test_14: li x1, 0x00ff00ff
 ori x0, x1, 0x70f
 li x7, 0
 li gp, 14
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
