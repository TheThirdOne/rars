#error:Runtime exception at 0x00400008: Load address not aligned to word boundary 0x10010001
.data
d: .word 42
.text
la a0, d
lw a1, 1(a0)
