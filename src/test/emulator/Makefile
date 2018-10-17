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
LDFLAGS := $(LDFLAGS) -L. -lpthread -lfesvr

$(addsuffix .o,$(CXXSRCS)): %.o: %.cc
	$(CXX) $(CXXFLAGS) -I${VERILATOR_INCLUDE} -c -o $@ $<

VERILATOR := verilator --cc --exe
VERILATOR_FLAGS := --top-module Top \
  +define+PRINTF_COND=\$$c\(\"verbose\"\) \
  --assert \
  --output-split 20000 \
	--x-assign unique \
  -Ivsrc \
  -O3 -CFLAGS "$(CXXFLAGS) $(LDFLAGS) -DVERILATOR -include ../verilator.h"

tester: Top.v tester.cpp SimDTM.cc vsrc/AsyncReadMem.sv vsrc/SyncMem.sv vsrc/SimDTM.v
	$(VERILATOR) $(VERILATOR_FLAGS) \
	-o $@ $< vsrc/SimDTM.v -LDFLAGS "$(LDFLAGS)" tester.cpp ../SimDTM.cc
	$(MAKE) -C obj_dir -f VTop.mk
