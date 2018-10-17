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
   const char* loadmem = NULL;
   FILE *vcdfile = NULL, *logfile = stderr;
   const char* failure = NULL;

   std::vector<std::string> to_dtm;
   for (int i = 1; i < argc; i++)
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
         to_dtm.push_back(argv[i]+9);
      }
   }

   const int disasm_len = 24;

   if(!to_dtm.size()){
      fprintf(stderr,"No binary specified for emulator\n");
      return 1;
   }


   VTop dut; // design under test, aka, your chisel code

   //Instantiated DTM
   dtm = new dtm_t(to_dtm);
   fprintf(stderr, "Instantiated DTM.\n");

#if VM_TRACE
   Verilated::traceEverOn(true); // Verilator must compute traced signals
   std::unique_ptr<VerilatedVcdFILE> vcdfd(new VerilatedVcdFILE(vcdfile));
   std::unique_ptr<VerilatedVcdC> tfp(new VerilatedVcdC(vcdfd.get()));
   if (vcdfile) {
      dut.trace(tfp.get(), 99);  // Trace 99 levels of hierarchy
      tfp->open("");
   }
#endif

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

  trace_count = 0;

   while (!dtm->done() && !dut.io_success && !Verilated::gotFinish()) {
      dut.clock = 0;
      dut.eval();
#if VM_TRACE
      bool dump = tfp && trace_count >= start;
      if (dump)
         tfp->dump(static_cast<vluint64_t>(trace_count * 2));
#endif
      dut.clock = 1;
      dut.eval();
#if VM_TRACE
      if (dump)
         tfp->dump(static_cast<vluint64_t>(trace_count * 2 + 1));
#endif
      trace_count++;
      if (max_cycles != 0 && trace_count == max_cycles)
      {
         failure = "timeout";
         break;
      }
   }

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

#if VM_TRACE
  if (tfp)
    tfp->close();
  if (vcdfile)
    fclose(vcdfile);
#endif

   if (failure)
   {
      fprintf(logfile, "*** FAILED *** (%s) after %lld cycles\n", failure, (long long)trace_count);
      return -1;
   }
   else if (dtm->exit_code() <= 1)
   {
      fprintf(logfile, "*** PASSED ***\n");
   }
   else
   {
     return dtm->exit_code();
   }

#if 0

#endif

   delete dtm;

   return 0;
}
