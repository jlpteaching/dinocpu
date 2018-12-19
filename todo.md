# These are things I'd like to improve about this

## General to do

[ ] Package better for students.
[x] Add better way to exit from my emulator. I think we need to call a syscall (ecall?) to get the SimDTM to pick up on it and exit cleanly. See `RVTEST_PASS` `in riscv-tools/riscv-tests/env/p/riscv_test.h`
[ ] Add an option to the emulator and tester to output the tracefile. Make it a command line option instead of build time.
[ ] A better pipeline viewer

## Very important things!
[x] Fix everything so it is loaded at 0x80000000 instead of 0x8000000. Oops!
  - Note: Actually just made it 0 instead of 8000 something.

## Re-writing the emulator

Things we want from the emulator
 - Cycle counts for executing a program
 - Bubble counts
 - Instruction mix?
 - Run full workloads

So, I've re-written the emulator, but I still need to do the above.

## Ideas for restructuring for assignments

- [ ] Start with just the hazard detection unit and detect all hazards instead of forwarding
- [ ] The first assignment should have something that takes a diagram and then the students will convert that into chisel code
- [ ] We need to re-do the diagrams from the book to better match the design that we have

## Minor modification

- [ ] Update all names of signals to be `<stage>_<name>`
- [ ] Add documentation about using vals in pipelined CPU

## Testing

- I'd like to have a less hacky way to read and write the registers
  - DONE!
- Related, improving the debug interface would be nice.
- Add a way to check memory values
  - DONE!

### Tests to add

- [x] add test that "writes" register 0
- [x] beq test
- [x] sub test
- [x] and test
- [x] or test
- [x] Forwarding tests
  - I want to add a test that accumulates lots of registers into x1, for instance
- [ ] Unit test for the forwarding logic
- [ ] Unit test for the control logic

## Things to think about

- Other forwarding paths

## Documentation to add

- [ ] All structures in components need to have details about their I/O

# Some notes on improving testing

I've tried to use the `loadMemoryFromFile` function for debugging.
However, the file must be specified when the memory is created, which is causing me a bit of a headache since I can't specify the file when I'm creating the test.
Also, this requires using the master on Chisel, which I would rather not do.

I also can't seem to poke a register in the register file, which isn't shocking, but it's a little annoying.

So, I think the solution is to do a setup phase where I "load" the memory with the data from the file, and I also update the registers to be the right values.
Then, I can "reset" the CPU by setting the PC back to 0 and then running.

Ok, so, the above won't work because it seems like you can't peek/poke things that are not the top level.
I think you would have to use something like the `BoringUtils` to get it to work.
However, that's 1) a huge pain and really awkward and 2) it's not in master right now, so I don't want to use it.

There's a couple of other options.
It may be possible to use the testers2 interface to do what I want.
Overall, it seems better and it allows you to factor things out, so it may work.

The other option is to poke instructions and then check all of the control signals (e.g., the writeback) to make sure they are correct.
This will also be easier with testers2 since it allows you to do pipelined things easily.

# Long term to do

- I'd like to replace the gradescope java files with scala. There's no need for them to be as complicated as they are. All we need to do is output a simple json file.
