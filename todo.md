# These are things I'd like to improve about this

## General to do

[ ] Package better for students.
[ ] Add better way to exit from my emulator. I think we need to call a syscall (ecall?) to get the SimDTM to pick up on it and exit cleanly. See `RVTEST_PASS` `in riscv-tools/riscv-tests/env/p/riscv_test.h`
[ ] Add an option to the emulator and tester to output the tracefile. Make it a command line option instead of build time.
[ ] A better pipeline viewer

## Very important things!
[ ] Fix everything so it is loaded at 0x80000000 instead of 0x8000000. Oops!

## Ideas for restructuring for assignments

- [ ] Start with just the hazard detection unit and detect all hazards instead of forwarding

## Minor modification

- [ ] Update all names of signals to be `<stage>_<name>`
- [ ] Add documentation about using vals in pipelined CPU

## Testing

- I'd like to have a less hacky way to read and write the registers
- Related, improving the debug interface would be nice.
- Add a way to check memory values

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
