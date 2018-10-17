# File format

```
<path to the binary>
<Number of registers to set> (can be 0 or more)
<reg num> <value> (must have a number of these equal to the previous number)
...
run <cycles> (the word "run" must appear here. It's used for sanity checking)
<Number of register to check>
<reg num> <value> (must have a number of these equal to the previous number)
```

**Important**: All values must be in decimal!

The register numbers are the flat register numbers for RISC-V.
