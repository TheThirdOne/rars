#exit:42
	.eqv SUBSTITUTE, 'a'    # a is 97

	.text
        li s0, SUBSTITUTE       # this must build

        li a0, 42
        li a7, 93
        ecall
