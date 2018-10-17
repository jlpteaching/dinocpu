#include "VTop__Dpi.h"
#if VM_TRACE
#include <verilated_vcd_c.h>
#endif
#include "VTop.h" // chisel-generated code...
#include "verilator.h"
#include <fesvr/dtm.h>
#include "verilated.h"
#include <fcntl.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#include <fstream>
#include <iostream>

uint32_t get_reg_value(VTop& dut, int reg_num);
uint32_t write_reg_value(VTop& dut, int reg_num, uint32_t val);

extern dtm_t *dtm;
uint64_t trace_count = 0;
bool verbose = false;
double sc_time_stamp ()
{
  return double( trace_count );
}

void handle_sigterm(int sig)
{
   dtm->stop();
}

extern "C" int vpi_get_vlog_info(void* arg)
{
  return 0;
}

int main(int argc, char** argv)
{
   unsigned random_seed = (unsigned)time(NULL) ^ (unsigned)getpid();
   uint64_t max_cycles = 0;
   int start = 0;
   std::string loadmem;
   FILE *vcdfile = NULL, *logfile = stderr;
   const char* failure = NULL;

   std::vector<std::string> to_dtm;

   assert(argc >= 1);

   std::string scriptfile = argv[1];

   std::ifstream file(scriptfile, std::ios::in);

   int regs_to_set = 0;
   file >> loadmem >> regs_to_set;
   std::map<int, uint32_t> reg_vals;
   for (int i = 0; i < regs_to_set; i++) {
     int reg_num = 0;
     uint32_t val = 0;
     file >> reg_num >> val;
     reg_vals[reg_num] = val;
   }

   std::string should_be_run;
   file >> should_be_run >> max_cycles;
   assert(should_be_run == "run");

   int regs_to_check = 0;
   file >> regs_to_check;
   std::map<int, uint32_t> check_vals;
   for (int i = 0; i < regs_to_check; i++) {
     int reg_num = 0;
     uint32_t val = 0;
     file >> reg_num >> val;
     check_vals[reg_num] = val;
   }

   // Overrides values in the scriptfile
   for (int i = 2; i < argc; i++)
   {
      std::string arg = argv[i];
      if (arg.substr(0, 2) == "-v")
         vcdfile = fopen(argv[i]+2,(const char*)"w+");
      else if (arg.substr(0, 2) == "-s")
         random_seed = atoi(argv[i]+2);
      else if (arg == "+verbose")
         verbose = true;
      else if (arg.substr(0, 12) == "+max-cycles=")
         max_cycles = atoll(argv[i]+12);
      else if (arg.substr(0, 9) == "+loadmem="){
         loadmem = argv[i]+9;
      }
   }

   to_dtm.push_back(loadmem.c_str());

   const int disasm_len = 24;

   if(!to_dtm.size()){
      fprintf(stderr,"No binary specified for emulator\n");
      return 1;
   }


   VTop dut; // design under test, aka, your chisel code

   //Instantiated DTM
   dtm = new dtm_t(to_dtm);
   fprintf(stderr, "Instantiated DTM.\n");

   signal(SIGTERM, handle_sigterm);

   // reset for a few cycles to support pipelined reset
   for (int i = 0; i < 10; i++) {
    dut.reset = 1;
    dut.clock = 0;
    dut.eval();
    dut.clock = 1;
    dut.eval();
    dut.reset = 0;
  }

  for (auto it: reg_vals) {
    write_reg_value(dut, it.first, it.second);
  }

  trace_count = 0;

   while (!dtm->done() && !dut.io_success && !Verilated::gotFinish() &&
          trace_count < max_cycles) {
      dut.clock = 0;
      dut.eval();
      dut.clock = 1;
      dut.eval();
      trace_count++;
   }

   if (trace_count != max_cycles)
   {
      fprintf(logfile, "*** FAILED *** after %lld cycles\n", (long long)trace_count);
      return -1;
   }

   bool failed = false;
   for (auto it: check_vals) {
     int reg_num = it.first;
     uint32_t value = it.second;
     if (get_reg_value(dut, reg_num) != value) {
       failed = true;
       printf("Failure: reg[%i]=%i (correct: %i)\n", reg_num, get_reg_value(dut, reg_num), value);
     }
   }

   if (failed) {
     printf("Test FAILED!\n");
     return 1;
   } else {
     printf("Test PASSED!\n");
   }

   if (verbose) {
      printf("Registers at the end of simulation: \n");
      printf("[ 0] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_0);
      printf("[ 1] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_1);
      printf("[ 2] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_2);
      printf("[ 3] %08x\n", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_3);
      printf("[ 4] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_4);
      printf("[ 5] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_5);
      printf("[ 6] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_6);
      printf("[ 7] %08x\n", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_7);
      printf("[ 8] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_8);
      printf("[ 9] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_9);
      printf("[10] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_10);
      printf("[11] %08x\n", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_11);
      printf("[12] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_12);
      printf("[13] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_13);
      printf("[14] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_14);
      printf("[15] %08x\n", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_15);
      printf("[16] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_16);
      printf("[17] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_17);
      printf("[18] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_18);
      printf("[19] %08x\n", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_19);
      printf("[20] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_20);
      printf("[21] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_21);
      printf("[22] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_22);
      printf("[23] %08x\n", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_23);
      printf("[24] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_24);
      printf("[25] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_25);
      printf("[26] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_26);
      printf("[27] %08x\n", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_27);
      printf("[28] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_28);
      printf("[29] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_29);
      printf("[30] %08x", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_30);
      printf("[31] %08x\n", dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_31);
   }

   delete dtm;

   return 0;
}

uint32_t get_reg_value(VTop& dut, int reg_num)
{
  switch(reg_num) {
    case 0:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_0;
    case 1:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_1;
    case 2:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_2;
    case 3:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_3;
    case 4:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_4;
    case 5:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_5;
    case 6:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_6;
    case 7:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_7;
    case 8:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_8;
    case 9:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_9;
    case 10:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_10;
    case 11:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_11;
    case 12:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_12;
    case 13:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_13;
    case 14:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_14;
    case 15:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_15;
    case 16:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_16;
    case 17:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_17;
    case 18:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_18;
    case 19:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_19;
    case 20:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_20;
    case 21:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_21;
    case 22:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_22;
    case 23:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_23;
    case 24:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_24;
    case 25:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_25;
    case 26:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_26;
    case 27:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_27;
    case 28:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_28;
    case 29:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_29;
    case 30:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_30;
    case 31:
      return dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_31;
    default:
      assert(0);
  }
}

uint32_t write_reg_value(VTop& dut, int reg_num, uint32_t val)
{
  switch(reg_num) {
    case 0:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_0 = val;
      break;
    case 1:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_1 = val;
      break;
    case 2:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_2 = val;
      break;
    case 3:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_3 = val;
      break;
    case 4:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_4 = val;
      break;
    case 5:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_5 = val;
      break;
    case 6:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_6 = val;
      break;
    case 7:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_7 = val;
      break;
    case 8:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_8 = val;
      break;
    case 9:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_9 = val;
      break;
    case 10:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_10 = val;
      break;
    case 11:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_11 = val;
      break;
    case 12:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_12 = val;
      break;
    case 13:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_13 = val;
      break;
    case 14:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_14 = val;
      break;
    case 15:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_15 = val;
      break;
    case 16:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_16 = val;
      break;
    case 17:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_17 = val;
      break;
    case 18:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_18 = val;
      break;
    case 19:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_19 = val;
      break;
    case 20:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_20 = val;
      break;
    case 21:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_21 = val;
      break;
    case 22:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_22 = val;
      break;
    case 23:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_23 = val;
      break;
    case 24:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_24 = val;
      break;
    case 25:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_25 = val;
      break;
    case 26:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_26 = val;
      break;
    case 27:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_27 = val;
      break;
    case 28:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_28 = val;
      break;
    case 29:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_29 = val;
      break;
    case 30:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_30 = val;
      break;
    case 31:
      dut.Top__DOT__tile__DOT__cpu__DOT__registers__DOT__regs_31 = val;
      break;
    default:
      assert(0);
  }
}
