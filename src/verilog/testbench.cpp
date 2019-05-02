#include "VTop.h"
#include <verilated.h>
 vluint64_t main_time = 0;       // Current simulation time
        // This is a 64-bit integer to reduce wrap over issues and
        // allow modulus.  You can also use a double, if you wish.

        double sc_time_stamp () {       // Called by $time in Verilog
            return main_time;           // converts to double, to match
                                        // what SystemC does
        }


int main(int argc, char** argv, char** env){
    Verilated::commandArgs(argc, argv);
    VTop* top = new VTop;
    //reset
    top->reset = 1;
    top->clock = 0;
    top->eval();
    top->clock = 1;
    top->eval();
    top->reset = 0;

    while(!Verilated::gotFinish()){
	top->clock = 0;
        top->eval();
	
	top->clock = 1;
    	top->eval();
    }
    delete top;
    exit(0);
}
