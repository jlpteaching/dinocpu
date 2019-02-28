/*======================================================================*/
/* Proxy kernel linker script                                           */
/*======================================================================*/
/* This is the linker script used when building the proxy kernel. */

/*----------------------------------------------------------------------*/
/* Setup                                                                */
/*----------------------------------------------------------------------*/

/* The OUTPUT_ARCH command specifies the machine architecture where the
   argument is one of the names used in the BFD library. More
   specifically one of the entires in bfd/cpu-mips.c */

OUTPUT_ARCH( "riscv" )
ENTRY(_start)

/*----------------------------------------------------------------------*/
/* Sections                                                             */
/*----------------------------------------------------------------------*/

SECTIONS
{

  /* text: test code section */
  . = 0x0;
  .text : {
    *(.text.init)
    *(.text)
    *(.text.startup)
    *(.text.finish)
  }

  . += 0x100;
  .data : {
    *(.data)
    *(.rodata)
    *(.rodata.cst8)
    *(.srodata.cst8)

    . += 0x100; /* Space between data and stack */
    FILL(0x0)
    . += 0x2000; /* stack size is 8k */
    STACK = .;
  }

  /* End of uninitalized data segement */
  _end = .;
}

