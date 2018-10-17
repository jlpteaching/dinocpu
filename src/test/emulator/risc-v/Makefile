
RISCV ?= /opt/riscv
RISCVBIN = $(RISCV)/bin

SOURCES = $(wildcard *.riscv)
OBJECTS = $(SOURCES:%.riscv=%.o)
TESTS = $(SOURCES:%.riscv=%)

all: $(TESTS)

$(OBJECTS): %.o: %.riscv
	$(RISCVBIN)/riscv32-unknown-elf-as -march=rv32i $< -o $@

$(TESTS): %: %.o
	$(RISCVBIN)/riscv32-unknown-elf-ld -Ttext 0x8000000 -Tdata 0x80000100 $< -o $@
