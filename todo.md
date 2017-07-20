# Todos

Just an internal collection of high level stability and polish todos  that need to be
done if you aren't a developer pay this no mind.

> I'm mainly committing this for accountability and to ensure that I keep track of
> everything. I have been leaving more things half-finished recently, so relying on
> my personal notes is not going to cut it.
> 
>  --Benjamin Landers

Transition MIPS->RISCV
  - Finish cleaning mips references from comments
  - Rename Coprocessor0&1 to better names
  - Eliminate the .ktext section
  - Figure out a good name and logo
  - Fix some outdated documentation which became incorrect during conversion

Code cleanliness
  - Take some time to figure out how to best organize the classes and make it so
  - Clean up venus some more (a start was made, but there is more to do)
  - Add asserts to document hidden dependecies and validate assumptions; possibly?
  - Make a logger to unify System.out & printing to GUI console

Robustness / following the spec
  - Floating point stuff
    - There is a ton here to be done, but its a low priority
  - User level interrupts
    - Should be faithful, but a fair number of things could be wrong with the implementation 
    - Known bad
      - "Execution of URET will place the uepc, ucause, and utval back into initial state."
  - CSRs 
    - Currently access control is not faithful, all bits can be modified at will.
    - realtimeclock, cycle count, and retired cycle count are not made yet
    - some psuedo-ops not included yet

Misc
  - Convert MARS system calls to linux system calls if possible
  - Track down what causes the weird preffered sizes issue on Linux
