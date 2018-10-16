all: emulator

CXX := g++
SBT := java -Xmx4096M -Xss8M -XX:MaxPermSize=128M -jar $(srcDir)/sbt-launch.jar $(SBT_FLAGS)

common      := .

ELF         := elf2hex

# pipe processor output through the spike-dasm program to turn 
# raw instruction bits into pretty, readable diassembly
disasm_exe := 2>
which_disasm := $(shell which spike-dasm)
ifneq ($(which_disasm),)
	disasm_exe := 3>&1 1>&2 2>&3 | $(which_disasm) $(DISASM_EXTENSION) >
endif

MK_TARGET_PROC?=single-stage
 
ifneq ($(shell which pkg-config),)
	ifneq ($(shell pkg-config --variable=includedir verilator),)
		VERILATOR_INCLUDE=$(shell pkg-config --variable=includedir verilator)
	else
		ifneq ($(wildcard $(VERILATOR_ROOT)/include/vltstd/vpi_user.h),)
			VERILATOR_INCLUDE=$(VERILATOR_ROOT)/include/
		else
			$(error VERILATOR_ROOT is set to $(VERILATOR_ROOT) but not finding expected_file in there.)
		endif
	endif
else
	ifneq ($(wildcard $(VERILATOR_ROOT)/include/vltstd/vpi_user.h),)
		VERILATOR_INCLUDE=$(VERILATOR_ROOT)/include/
	else
		$(error VERILATOR_ROOT is set to $(VERILATOR_ROOT) but not finding expected_file in there.)
	endif
endif
 
CXXFLAGS := $(CXXFLAGS) -O1 -std=c++11  -g

CXXSRCS := SimDTM
CXXFLAGS := $(CXXFLAGS)
CXXDEBUGFLAGS := -DDEBUG

LDFLAGS := $(LDFLAGS) -L. -lpthread -lfesvr
OBJS := $(addsuffix .o,$(CXXSRCS))
DEBUG_OBJS := $(addsuffix -debug.o,$(CXXSRCS))

CHISEL_ARGS := -td emulator/${MK_TARGET_PROC}/generated-src
CHISEL_ARGS_DEBUG := 

$(addsuffix .o,$(CXXSRCS)): %.o: %.cc
	$(CXX) $(CXXFLAGS) -I${VERILATOR_INCLUDE} -c -o $@ $<

generated_dir = .

# Run Verilator to produce a fast binary to emulate this circuit.
VERILATOR := verilator --cc --exe 
VERILATOR_FLAGS := --top-module Top \
  +define+PRINTF_COND=\$$c\(\"verbose\"\) \
  --assert \
  --output-split 20000 \
	--x-assign unique \
  -Ivsrc \
  -O3 -CFLAGS "$(CXXFLAGS) $(LDFLAGS) -DVERILATOR -include ../verilator.h"

emulator: Top.v emulator.cpp SimDTM.o vsrc/AsyncReadMem.sv vsrc/SyncMem.sv vsrc/SimDTM.v
	$(VERILATOR) $(VERILATOR_FLAGS) \
	-o $@ $< vsrc/SimDTM.v -LDFLAGS "$(LDFLAGS)" emulator.cpp ../SimDTM.o
	$(MAKE) -C obj_dir -f VTop.mk

emulator-debug: generated-src/Top.v $(srcDir)/emulator/common/emulator.cpp SimDTM.o $(srcDir)/vsrc/AsyncReadMem.sv $(srcDir)/vsrc/SyncMem.sv $(srcDir)/vsrc/SimDTM.v
	$(VERILATOR) $(VERILATOR_FLAGS) --trace \
	-o $(generated_dir)/$@ $< $(srcDir)/vsrc/SimDTM.v -LDFLAGS "$(LDFLAGS)" $(srcDir)/emulator/common/emulator.cpp $(srcDir)/riscv-fesvr/build/libfesvr.so $(generated_dir)/SimDTM.o
	$(MAKE) -C $(generated_dir)/obj_dir -f VTop.mk
 
clean:
	rm -rf *.o *.a emulator obj_dir emulator-debug generated-src generated-src-debug DVEfiles output

