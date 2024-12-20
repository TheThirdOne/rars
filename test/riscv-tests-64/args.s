#args:hello world
#stdout:2helloworld
mv s0, a0 # argc
mv s1, a1 # argv

li a7, 1
ecall
li a7, 4
ld a0, 0(s1)
ecall
ld a0, 8(s1)
ecall

li a7, 10
ecall
