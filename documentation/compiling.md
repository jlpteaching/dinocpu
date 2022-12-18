---
author: Jason Lowe-Power, Hoa Nguyen
title: Compiling code to run on DINO CPU
---

# Compiling code to run on DINO CPU

## Getting baremetal programs working

Here's an example baremetal program that loads -1.
We have to have the `_start` symbol declared or the linker gets mad at us.

```
  .text
  .align 2       # Make sure we're aligned to 4 bytes
  .globl _start
_start:
    lw t0, 0x100(zero)   # t0 (x5) <= mem[0x100]

  .data
.byte 0xFF,0xFF,0xFF,0xFF
```

## STEP 0: Install Toolchain
Visit https://github.com/riscv/riscv-gnu-toolchain to install the cross-compiler toolchain. Configure the 64-bit version with `--with-abi=lp64` (software emulated floating point) and `--with-arch=rv64i`(integer ISA only). You can add supported extensions if you've extended dinocpu to support those instructions. Make sure to install the 64-bit version.

## STEP 1: Assemble

Now, to compile, first we have to assemble it.

```
riscv64-unknown-elf-as -march=rv64i src/main/risc-v/test.riscv -o test.o
```

### STEP 2: Link

Then, we have to link it.
When linking, we have to specify that the text should be located at `0x00` and that the data should be located at `0x400`.
We chose `0x400` arbitrarily.
This can be anything within the size of memory (16 KB by default).

```
riscv64-unknown-elf-ld  -Ttext 0x0 -Tdata 0x400 test.o -o test
```

### STEP 3: Check output

Finally, we can disassemble it to make sure it looks right.

```
riscv64-unknown-elf-objdump -Dx test
```

We get the following:

```
lw1:     file format elf32-littleriscv
lw1
architecture: riscv:rv32, flags 0x00000112:
EXEC_P, HAS_SYMS, D_PAGED
start address 0x00000000

Program Header:
    LOAD off    0x00001000 vaddr 0x00000000 paddr 0x00000000 align 2**12
         filesz 0x00000404 memsz 0x00000404 flags rwx

Sections:
Idx Name          Size      VMA       LMA       File off  Algn
  0 .text         00000018  00000000  00000000  00001000  2**2
                  CONTENTS, ALLOC, LOAD, READONLY, CODE
  1 .data         00000004  00000400  00000400  00001400  2**0
                  CONTENTS, ALLOC, LOAD, DATA
SYMBOL TABLE:
00000000 l    d  .text  00000000 .text
00000400 l    d  .data  00000000 .data
00000000 l    df *ABS*  00000000 lw1.o
00000018 l       .text  00000000 _last
00000c04 g       .data  00000000 __global_pointer$
00000000 g       .text  00000000 _start
00000404 g       .data  00000000 __bss_start
00000404 g       .data  00000000 _edata
00000404 g       .data  00000000 _end



Disassembly of section .text:

00000000 <_start>:
   0:   40002283                lw      t0,1024(zero) # 400 <_last+0x3e8>
   4:   00000013                nop
   8:   00000013                nop
   c:   00000013                nop
  10:   00000013                nop
  14:   00000013                nop

Disassembly of section .data:

00000400 <__bss_start-0x4>:
 400:   ffff                    0xffff
 402:   ffff                    0xffff

```

## Compiling C programs

The DINO CPU's simulator makes some assumptions about the initial PC and when to stop executing.
In order to build an application that will work with the DINO CPU's simulator you need to jump through some hoops.

There is now an automated way to do the above for C programs.
You can add a new benchmark by adding a C file in `src/test/resources/c`.

When you run `make` in that directory, it will create both a RISC-V binary (`*.riscv`) that is compatible with the DINO CPU simulator, and it will create a file `*.dump` which contains the disassembled version of the workload.

Note: Not all workloads are currently supported.
Some care is needed for different code sections and the size of the stack.
