// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Symbol table internal header
//
// Internal details; most calling programs do not need this header

#ifndef _VTop__Syms_H_
#define _VTop__Syms_H_

#include "verilated_heavy.h"

// INCLUDE MODULE CLASSES
#include "VTop.h"

// SYMS CLASS
class VTop__Syms : public VerilatedSyms {
  public:
    
    // LOCAL STATE
    const char* __Vm_namep;
    bool __Vm_didInit;
    
    // SUBCELL STATE
    VTop*                          TOPp;
    
    // SCOPE NAMES
    VerilatedScope __Vscope_Top__mem;
    
    // CREATORS
    VTop__Syms(VTop* topp, const char* namep);
    ~VTop__Syms() {}
    
    // METHODS
    inline const char* name() { return __Vm_namep; }
    
} VL_ATTR_ALIGNED(64);

#endif // guard
