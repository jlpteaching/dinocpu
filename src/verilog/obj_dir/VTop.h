// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Primary design header
//
// This header should be included by all source files instantiating the design.
// The class here is then constructed to instantiate the design.
// See the Verilator manual for examples.

#ifndef _VTop_H_
#define _VTop_H_

#include "verilated_heavy.h"

class VTop__Syms;

//----------

VL_MODULE(VTop) {
  public:
    
    // PORTS
    // The application code writes and reads these signals to
    // propagate new values into/out from the Verilated model.
    VL_IN8(clock,0,0);
    VL_IN8(reset,0,0);
    VL_OUT8(io_success,0,0);
    
    // LOCAL SIGNALS
    // Internals; generally not touched by application code
    VL_SIG8(Top__DOT__cpu__DOT__control_io_toreg,1,0);
    VL_SIG8(Top__DOT__cpu__DOT__control_io_memwrite,0,0);
    VL_SIG8(Top__DOT__cpu__DOT__control_io_immediate,0,0);
    VL_SIG8(Top__DOT__cpu__DOT__control_io_alusrc1,1,0);
    VL_SIG8(Top__DOT__cpu__DOT__control_io_jump,1,0);
    VL_SIG8(Top__DOT__cpu__DOT__registers_io_wen,0,0);
    VL_SIG8(Top__DOT__cpu__DOT__aluControl_io_operation,3,0);
    VL_SIG(Top__DOT__mem_io_imem_instruction,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers_io_writedata,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers_io_readdata1,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers_io_readdata2,31,0);
    VL_SIG(Top__DOT__cpu__DOT__alu_io_inputx,31,0);
    VL_SIG(Top__DOT__cpu__DOT__alu_io_inputy,31,0);
    VL_SIG(Top__DOT__cpu__DOT__immGen_io_sextImm,31,0);
    VL_SIG(Top__DOT__cpu__DOT__pcPlusFour_io_inputx,31,0);
    VL_SIG(Top__DOT__cpu__DOT__branchAdd_io_result,31,0);
    VL_SIG(Top__DOT__cpu__DOT__pc,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_0,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_1,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_2,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_3,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_4,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_5,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_6,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_7,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_8,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_9,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_10,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_11,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_12,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_13,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_14,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_15,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_16,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_17,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_18,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_19,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_20,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_21,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_22,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_23,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_24,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_25,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_26,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_27,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_28,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_29,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_30,31,0);
    VL_SIG(Top__DOT__cpu__DOT__registers__DOT__regs_31,31,0);
    VL_SIG(Top__DOT__cpu__DOT__alu__DOT___T_3,31,0);
    VL_SIG(Top__DOT__cpu__DOT__immGen__DOT___T_26,31,0);
    VL_SIG(Top__DOT__mem__DOT__memory___05FT_49_data,31,0);
    VL_SIG(Top__DOT__mem__DOT___GEN_14,31,0);
    VL_SIG64(Top__DOT__cpu__DOT__alu__DOT___GEN_10,62,0);
    VL_SIG(Top__DOT__mem__DOT__memory[16384],31,0);
    
    // LOCAL VARIABLES
    // Internals; generally not touched by application code
    VL_SIG8(__Vclklast__TOP__clock,0,0);
    
    // INTERNAL VARIABLES
    // Internals; generally not touched by application code
    VTop__Syms* __VlSymsp;  // Symbol table
    
    // PARAMETERS
    // Parameters marked /*verilator public*/ for use by application code
    
    // CONSTRUCTORS
  private:
    VTop& operator= (const VTop&);  ///< Copying not allowed
    VTop(const VTop&);  ///< Copying not allowed
  public:
    /// Construct the model; called by application code
    /// The special name  may be used to make a wrapper with a
    /// single model invisible WRT DPI scope names.
    VTop(const char* name="TOP");
    /// Destroy the model; called (often implicitly) by application code
    ~VTop();
    
    // API METHODS
    /// Evaluate the model.  Application must call when inputs change.
    void eval();
    /// Simulation complete, run final blocks.  Application must call on completion.
    void final();
    
    // INTERNAL METHODS
  private:
    static void _eval_initial_loop(VTop__Syms* __restrict vlSymsp);
  public:
    void __Vconfigure(VTop__Syms* symsp, bool first);
  private:
    static QData _change_request(VTop__Syms* __restrict vlSymsp);
    void _ctor_var_reset();
  public:
    static void _eval(VTop__Syms* __restrict vlSymsp);
  private:
#ifdef VL_DEBUG
    void _eval_debug_assertions();
#endif // VL_DEBUG
  public:
    static void _eval_initial(VTop__Syms* __restrict vlSymsp);
    static void _eval_settle(VTop__Syms* __restrict vlSymsp);
    static void _initial__TOP__1(VTop__Syms* __restrict vlSymsp);
    static void _sequent__TOP__2(VTop__Syms* __restrict vlSymsp);
    static void _settle__TOP__3(VTop__Syms* __restrict vlSymsp);
} VL_ATTR_ALIGNED(128);

#endif // guard
