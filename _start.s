.global _start
.global main

.text 
_start:  
	lw a0, (sp)
	addi a1, sp, 4
	.option push
        .option norelax
        la gp, __global_pointer$
        .option pop
        call main

