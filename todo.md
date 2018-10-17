# These are things I'd like to improve about this

[ ] Package better for students.
[ ] Add better way to exit from my emulator. I think we need to call a syscall (ecall?) to get the SimDTM to pick up on it and exit cleanly. See `RVTEST_PASS` `in riscv-tools/riscv-tests/env/p/riscv_test.h`

## Testing

- I'd like to have a less hacky way to read and write the registers
- Related, improving the debug interface would be nice.
- Add a way to check memory values

### Tests to add

- [ ] add test that "writes" register 0
- [ ] beq test
- [ ] sub test
- [ ] and test
- [ ] or test
