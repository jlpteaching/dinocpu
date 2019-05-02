// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Design implementation internals
// See VTop.h for the primary calling header

#include "VTop.h"              // For This
#include "VTop__Syms.h"


//--------------------
// STATIC VARIABLES


//--------------------

VL_CTOR_IMP(VTop) {
    VTop__Syms* __restrict vlSymsp = __VlSymsp = new VTop__Syms(this, name());
    VTop* __restrict vlTOPp VL_ATTR_UNUSED = vlSymsp->TOPp;
    // Reset internal values
    
    // Reset structure values
    _ctor_var_reset();
}

void VTop::__Vconfigure(VTop__Syms* vlSymsp, bool first) {
    if (0 && first) {}  // Prevent unused
    this->__VlSymsp = vlSymsp;
}

VTop::~VTop() {
    delete __VlSymsp; __VlSymsp=NULL;
}

//--------------------


void VTop::eval() {
    VL_DEBUG_IF(VL_DBG_MSGF("+++++TOP Evaluate VTop::eval\n"); );
    VTop__Syms* __restrict vlSymsp = this->__VlSymsp;  // Setup global symbol table
    VTop* __restrict vlTOPp VL_ATTR_UNUSED = vlSymsp->TOPp;
#ifdef VL_DEBUG
    // Debug assertions
    _eval_debug_assertions();
#endif // VL_DEBUG
    // Initialize
    if (VL_UNLIKELY(!vlSymsp->__Vm_didInit)) _eval_initial_loop(vlSymsp);
    // Evaluate till stable
    int __VclockLoop = 0;
    QData __Vchange = 1;
    while (VL_LIKELY(__Vchange)) {
	VL_DEBUG_IF(VL_DBG_MSGF("+ Clock loop\n"););
	_eval(vlSymsp);
	__Vchange = _change_request(vlSymsp);
	if (VL_UNLIKELY(++__VclockLoop > 100)) VL_FATAL_MT(__FILE__,__LINE__,__FILE__,"Verilated model didn't converge");
    }
}

void VTop::_eval_initial_loop(VTop__Syms* __restrict vlSymsp) {
    vlSymsp->__Vm_didInit = true;
    _eval_initial(vlSymsp);
    int __VclockLoop = 0;
    QData __Vchange = 1;
    while (VL_LIKELY(__Vchange)) {
	_eval_settle(vlSymsp);
	_eval(vlSymsp);
	__Vchange = _change_request(vlSymsp);
	if (VL_UNLIKELY(++__VclockLoop > 100)) VL_FATAL_MT(__FILE__,__LINE__,__FILE__,"Verilated model didn't DC converge");
    }
}

//--------------------
// Internal Methods

void VTop::_initial__TOP__1(VTop__Syms* __restrict vlSymsp) {
    VL_DEBUG_IF(VL_DBG_MSGF("+    VTop::_initial__TOP__1\n"); );
    VTop* __restrict vlTOPp VL_ATTR_UNUSED = vlSymsp->TOPp;
    // Body
    // INITIAL at Top.v:1440
    vlTOPp->io_success = 0U;
}

VL_INLINE_OPT void VTop::_sequent__TOP__2(VTop__Syms* __restrict vlSymsp) {
    VL_DEBUG_IF(VL_DBG_MSGF("+    VTop::_sequent__TOP__2\n"); );
    VTop* __restrict vlTOPp VL_ATTR_UNUSED = vlSymsp->TOPp;
    // Variables
    VL_SIG8(__Vdlyvset__Top__DOT__mem__DOT__memory__v0,0,0);
    VL_SIG8(__Vdlyvset__Top__DOT__mem__DOT__memory__v1,0,0);
    VL_SIG16(__Vdlyvdim0__Top__DOT__mem__DOT__memory__v0,13,0);
    VL_SIG16(__Vdlyvdim0__Top__DOT__mem__DOT__memory__v1,13,0);
    VL_SIG(__Vdlyvval__Top__DOT__mem__DOT__memory__v0,31,0);
    VL_SIG(__Vdlyvval__Top__DOT__mem__DOT__memory__v1,31,0);
    VL_SIGW(__Vtemp1,95,0,3);
    VL_SIGW(__Vtemp2,95,0,3);
    VL_SIGW(__Vtemp3,95,0,3);
    VL_SIGW(__Vtemp5,95,0,3);
    VL_SIGW(__Vtemp6,95,0,3);
    VL_SIGW(__Vtemp7,95,0,3);
    VL_SIGW(__Vtemp8,95,0,3);
    VL_SIGW(__Vtemp9,95,0,3);
    VL_SIGW(__Vtemp15,95,0,3);
    VL_SIGW(__Vtemp16,95,0,3);
    // Body
    // ALWAYS at Top.v:1335
    if (VL_UNLIKELY((((0x33U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
		      & ((0x13U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
			 & (3U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)))) 
		     & (~ ((0x10000U > (IData)(vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10)) 
			   | (IData)(vlTOPp->reset)))))) {
	VL_FWRITEF(0x80000002U,"Assertion failed\n    at memory.scala:70 assert(io.dmem.address < size.U)\n");
    }
    if (VL_UNLIKELY((((0x33U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
		      & ((0x13U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
			 & (3U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)))) 
		     & (~ ((0x10000U > (IData)(vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10)) 
			   | (IData)(vlTOPp->reset)))))) {
	VL_WRITEF("[%0t] %%Error: Top.v:1357: Assertion failed in %NTop.mem\n",
		  64,VL_TIME_Q(),vlSymsp->name());
	VL_STOP_MT("Top.v",1357,"");
    }
    if (VL_UNLIKELY(((IData)(vlTOPp->Top__DOT__cpu__DOT__control_io_memwrite) 
		     & (~ ((0x10000U > (IData)(vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10)) 
			   | (IData)(vlTOPp->reset)))))) {
	VL_FWRITEF(0x80000002U,"Assertion failed\n    at memory.scala:99 assert(io.dmem.address < size.U)\n");
    }
    if (VL_UNLIKELY(((IData)(vlTOPp->Top__DOT__cpu__DOT__control_io_memwrite) 
		     & (~ ((0x10000U > (IData)(vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10)) 
			   | (IData)(vlTOPp->reset)))))) {
	VL_WRITEF("[%0t] %%Error: Top.v:1379: Assertion failed in %NTop.mem\n",
		  64,VL_TIME_Q(),vlSymsp->name());
	VL_STOP_MT("Top.v",1379,"");
    }
    __Vdlyvset__Top__DOT__mem__DOT__memory__v0 = 0U;
    __Vdlyvset__Top__DOT__mem__DOT__memory__v1 = 0U;
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0U == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			     >> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_0 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((1U == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			     >> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_1 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((2U == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			     >> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_2 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((3U == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			     >> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_3 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((4U == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			     >> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_4 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((5U == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			     >> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_5 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((6U == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			     >> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_6 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((7U == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			     >> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_7 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((8U == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			     >> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_8 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((9U == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			     >> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_9 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0xaU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			       >> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_10 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0xbU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			       >> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_11 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0xcU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			       >> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_12 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0xdU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			       >> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_13 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0xeU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			       >> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_14 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0xfU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			       >> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_15 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0x10U == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				>> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_16 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0x11U == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				>> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_17 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0x12U == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				>> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_18 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0x13U == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				>> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_19 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0x14U == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				>> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_20 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0x15U == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				>> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_21 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0x16U == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				>> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_22 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0x17U == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				>> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_23 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0x18U == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				>> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_24 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0x19U == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				>> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_25 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0x1aU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				>> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_26 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0x1bU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				>> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_27 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0x1cU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				>> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_28 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0x1dU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				>> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_29 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0x1eU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				>> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_30 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:490
    if (vlTOPp->Top__DOT__cpu__DOT__registers_io_wen) {
	if ((0x1fU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				>> 7U)))) {
	    vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_31 
		= vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata;
	}
    }
    // ALWAYS at Top.v:1136
    vlTOPp->Top__DOT__cpu__DOT__pc = ((IData)(vlTOPp->reset)
				       ? 0U : (((((0U 
						   == 
						   (7U 
						    & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						       >> 0xcU)))
						   ? 
						  (vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata1 
						   == vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata2)
						   : 
						  ((1U 
						    == 
						    (7U 
						     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							>> 0xcU)))
						    ? 
						   (vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata1 
						    != vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata2)
						    : 
						   ((4U 
						     == 
						     (7U 
						      & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							 >> 0xcU)))
						     ? 
						    VL_LTS_III(1,32,32, vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata1, vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata2)
						     : 
						    ((5U 
						      == 
						      (7U 
						       & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							  >> 0xcU)))
						      ? 
						     VL_GTES_III(1,32,32, vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata1, vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata2)
						      : 
						     ((6U 
						       == 
						       (7U 
							& (vlTOPp->Top__DOT__mem_io_imem_instruction 
							   >> 0xcU)))
						       ? 
						      (vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata1 
						       < vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata2)
						       : 
						      (vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata1 
						       >= vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata2)))))) 
						 & ((0x33U 
						     != 
						     (0x7fU 
						      & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
						    & ((0x13U 
							!= 
							(0x7fU 
							 & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
						       & ((3U 
							   != 
							   (0x7fU 
							    & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
							  & ((0x23U 
							      != 
							      (0x7fU 
							       & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
							     & (0x63U 
								== 
								(0x7fU 
								 & vlTOPp->Top__DOT__mem_io_imem_instruction))))))) 
						| (2U 
						   == (IData)(vlTOPp->Top__DOT__cpu__DOT__control_io_jump)))
					        ? vlTOPp->Top__DOT__cpu__DOT__branchAdd_io_result
					        : (
						   (3U 
						    == (IData)(vlTOPp->Top__DOT__cpu__DOT__control_io_jump))
						    ? 
						   (0xfffffffeU 
						    & (IData)(vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10))
						    : 
						   ((IData)(4U) 
						    + vlTOPp->Top__DOT__cpu__DOT__pcPlusFour_io_inputx))));
    // ALWAYS at Top.v:1335
    if (((IData)(vlTOPp->Top__DOT__cpu__DOT__control_io_memwrite) 
	 & (2U != (3U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			 >> 0xcU))))) {
	VL_EXTEND_WI(71,32, __Vtemp1, vlTOPp->Top__DOT__mem__DOT__memory___05FT_49_data);
	__Vtemp2[0U] = 0xffU;
	__Vtemp2[1U] = 0U;
	__Vtemp2[2U] = 0U;
	VL_SHIFTL_WWI(71,71,6, __Vtemp3, __Vtemp2, 
		      (0x18U & ((IData)(vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10) 
				<< 3U)));
	__Vtemp5[0U] = (__Vtemp1[0U] & (~ __Vtemp3[0U]));
	__Vtemp5[1U] = (__Vtemp1[1U] & (~ __Vtemp3[1U]));
	__Vtemp5[2U] = (__Vtemp1[2U] & (~ __Vtemp3[2U]));
	VL_EXTEND_WW(79,71, __Vtemp6, __Vtemp5);
	VL_EXTEND_WI(79,32, __Vtemp7, vlTOPp->Top__DOT__mem__DOT__memory___05FT_49_data);
	__Vtemp8[0U] = 0xffffU;
	__Vtemp8[1U] = 0U;
	__Vtemp8[2U] = 0U;
	VL_SHIFTL_WWI(79,79,6, __Vtemp9, __Vtemp8, 
		      (0x18U & ((IData)(vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10) 
				<< 3U)));
	VL_EXTEND_WI(95,32, __Vtemp15, vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata2);
	VL_SHIFTL_WWI(95,95,6, __Vtemp16, __Vtemp15, 
		      (0x18U & ((IData)(vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10) 
				<< 3U)));
	__Vdlyvval__Top__DOT__mem__DOT__memory__v0 
	    = (((0U == (3U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			      >> 0xcU))) ? __Vtemp6[0U]
		 : (__Vtemp7[0U] & (~ __Vtemp9[0U]))) 
	       | __Vtemp16[0U]);
	__Vdlyvset__Top__DOT__mem__DOT__memory__v0 = 1U;
	__Vdlyvdim0__Top__DOT__mem__DOT__memory__v0 
	    = (0x3fffU & (IData)((vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10 
				  >> 2U)));
    }
    if (((IData)(vlTOPp->Top__DOT__cpu__DOT__control_io_memwrite) 
	 & (2U == (3U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			 >> 0xcU))))) {
	__Vdlyvval__Top__DOT__mem__DOT__memory__v1 
	    = vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata2;
	__Vdlyvset__Top__DOT__mem__DOT__memory__v1 = 1U;
	__Vdlyvdim0__Top__DOT__mem__DOT__memory__v1 
	    = (0x3fffU & (IData)((vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10 
				  >> 2U)));
    }
    // ALWAYSPOST at Top.v:1336
    if (__Vdlyvset__Top__DOT__mem__DOT__memory__v0) {
	vlTOPp->Top__DOT__mem__DOT__memory[__Vdlyvdim0__Top__DOT__mem__DOT__memory__v0] 
	    = __Vdlyvval__Top__DOT__mem__DOT__memory__v0;
    }
    if (__Vdlyvset__Top__DOT__mem__DOT__memory__v1) {
	vlTOPp->Top__DOT__mem__DOT__memory[__Vdlyvdim0__Top__DOT__mem__DOT__memory__v1] 
	    = __Vdlyvval__Top__DOT__mem__DOT__memory__v1;
    }
    vlTOPp->Top__DOT__cpu__DOT__pcPlusFour_io_inputx 
	= vlTOPp->Top__DOT__cpu__DOT__pc;
    vlTOPp->Top__DOT__mem_io_imem_instruction = ((0x10000U 
						  <= vlTOPp->Top__DOT__cpu__DOT__pc)
						  ? 0U
						  : 
						 vlTOPp->Top__DOT__mem__DOT__memory
						 [(0x3fffU 
						   & (vlTOPp->Top__DOT__cpu__DOT__pc 
						      >> 2U))]);
    vlTOPp->Top__DOT__cpu__DOT__registers_io_wen = 
	(((0x33U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
	  | ((0x13U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
	     | ((3U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
		| ((0x23U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
		   & ((0x63U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
		      & ((0x37U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
			 | ((0x17U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
			    | ((0x6fU == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
			       | (0x67U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)))))))))) 
	 & (0U != (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			    >> 7U))));
    vlTOPp->Top__DOT__cpu__DOT__control_io_memwrite 
	= ((0x33U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
	   & ((0x13U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
	      & ((3U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
		 & (0x23U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)))));
    vlTOPp->Top__DOT__cpu__DOT__control_io_jump = (
						   (0x33U 
						    == 
						    (0x7fU 
						     & vlTOPp->Top__DOT__mem_io_imem_instruction))
						    ? 0U
						    : 
						   ((0x13U 
						     == 
						     (0x7fU 
						      & vlTOPp->Top__DOT__mem_io_imem_instruction))
						     ? 0U
						     : 
						    ((3U 
						      == 
						      (0x7fU 
						       & vlTOPp->Top__DOT__mem_io_imem_instruction))
						      ? 0U
						      : 
						     ((0x23U 
						       == 
						       (0x7fU 
							& vlTOPp->Top__DOT__mem_io_imem_instruction))
						       ? 0U
						       : 
						      ((0x63U 
							== 
							(0x7fU 
							 & vlTOPp->Top__DOT__mem_io_imem_instruction))
						        ? 0U
						        : 
						       ((0x37U 
							 == 
							 (0x7fU 
							  & vlTOPp->Top__DOT__mem_io_imem_instruction))
							 ? 0U
							 : 
							((0x17U 
							  == 
							  (0x7fU 
							   & vlTOPp->Top__DOT__mem_io_imem_instruction))
							  ? 0U
							  : 
							 ((0x6fU 
							   == 
							   (0x7fU 
							    & vlTOPp->Top__DOT__mem_io_imem_instruction))
							   ? 2U
							   : 
							  ((0x67U 
							    == 
							    (0x7fU 
							     & vlTOPp->Top__DOT__mem_io_imem_instruction))
							    ? 3U
							    : 0U)))))))));
    vlTOPp->Top__DOT__cpu__DOT__control_io_toreg = 
	((0x33U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
	  ? 0U : ((0x13U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
		   ? 0U : ((3U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
			    ? 1U : ((0x23U == (0x7fU 
					       & vlTOPp->Top__DOT__mem_io_imem_instruction))
				     ? 0U : ((0x63U 
					      == (0x7fU 
						  & vlTOPp->Top__DOT__mem_io_imem_instruction))
					      ? 0U : 
					     ((0x37U 
					       == (0x7fU 
						   & vlTOPp->Top__DOT__mem_io_imem_instruction))
					       ? 0U
					       : ((0x17U 
						   == 
						   (0x7fU 
						    & vlTOPp->Top__DOT__mem_io_imem_instruction))
						   ? 0U
						   : 
						  ((0x6fU 
						    == 
						    (0x7fU 
						     & vlTOPp->Top__DOT__mem_io_imem_instruction))
						    ? 2U
						    : 
						   ((0x67U 
						     == 
						     (0x7fU 
						      & vlTOPp->Top__DOT__mem_io_imem_instruction))
						     ? 2U
						     : 3U)))))))));
    vlTOPp->Top__DOT__cpu__DOT__control_io_alusrc1 
	= ((0x33U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
	    ? 0U : ((0x13U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
		     ? 0U : ((3U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
			      ? 0U : ((0x23U == (0x7fU 
						 & vlTOPp->Top__DOT__mem_io_imem_instruction))
				       ? 0U : ((0x63U 
						== 
						(0x7fU 
						 & vlTOPp->Top__DOT__mem_io_imem_instruction))
					        ? 0U
					        : (
						   (0x37U 
						    == 
						    (0x7fU 
						     & vlTOPp->Top__DOT__mem_io_imem_instruction))
						    ? 1U
						    : 
						   ((0x17U 
						     == 
						     (0x7fU 
						      & vlTOPp->Top__DOT__mem_io_imem_instruction))
						     ? 2U
						     : 
						    (0x6fU 
						     == 
						     (0x7fU 
						      & vlTOPp->Top__DOT__mem_io_imem_instruction)))))))));
    vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata1 
	= ((0x1fU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			       >> 0xfU))) ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_31
	    : ((0x1eU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				   >> 0xfU))) ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_30
	        : ((0x1dU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				       >> 0xfU))) ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_29
		    : ((0x1cU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
					   >> 0xfU)))
		        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_28
		        : ((0x1bU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
					       >> 0xfU)))
			    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_27
			    : ((0x1aU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						   >> 0xfU)))
			        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_26
			        : ((0x19U == (0x1fU 
					      & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						 >> 0xfU)))
				    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_25
				    : ((0x18U == (0x1fU 
						  & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						     >> 0xfU)))
				        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_24
				        : ((0x17U == 
					    (0x1fU 
					     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						>> 0xfU)))
					    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_23
					    : ((0x16U 
						== 
						(0x1fU 
						 & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						    >> 0xfU)))
					        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_22
					        : (
						   (0x15U 
						    == 
						    (0x1fU 
						     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							>> 0xfU)))
						    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_21
						    : 
						   ((0x14U 
						     == 
						     (0x1fU 
						      & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							 >> 0xfU)))
						     ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_20
						     : 
						    ((0x13U 
						      == 
						      (0x1fU 
						       & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							  >> 0xfU)))
						      ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_19
						      : 
						     ((0x12U 
						       == 
						       (0x1fU 
							& (vlTOPp->Top__DOT__mem_io_imem_instruction 
							   >> 0xfU)))
						       ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_18
						       : 
						      ((0x11U 
							== 
							(0x1fU 
							 & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							    >> 0xfU)))
						        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_17
						        : 
						       ((0x10U 
							 == 
							 (0x1fU 
							  & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							     >> 0xfU)))
							 ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_16
							 : 
							((0xfU 
							  == 
							  (0x1fU 
							   & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							      >> 0xfU)))
							  ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_15
							  : 
							 ((0xeU 
							   == 
							   (0x1fU 
							    & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							       >> 0xfU)))
							   ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_14
							   : 
							  ((0xdU 
							    == 
							    (0x1fU 
							     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								>> 0xfU)))
							    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_13
							    : 
							   ((0xcU 
							     == 
							     (0x1fU 
							      & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								 >> 0xfU)))
							     ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_12
							     : 
							    ((0xbU 
							      == 
							      (0x1fU 
							       & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								  >> 0xfU)))
							      ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_11
							      : 
							     ((0xaU 
							       == 
							       (0x1fU 
								& (vlTOPp->Top__DOT__mem_io_imem_instruction 
								   >> 0xfU)))
							       ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_10
							       : 
							      ((9U 
								== 
								(0x1fU 
								 & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								    >> 0xfU)))
							        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_9
							        : 
							       ((8U 
								 == 
								 (0x1fU 
								  & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								     >> 0xfU)))
								 ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_8
								 : 
								((7U 
								  == 
								  (0x1fU 
								   & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								      >> 0xfU)))
								  ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_7
								  : 
								 ((6U 
								   == 
								   (0x1fU 
								    & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								       >> 0xfU)))
								   ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_6
								   : 
								  ((5U 
								    == 
								    (0x1fU 
								     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
									>> 0xfU)))
								    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_5
								    : 
								   ((4U 
								     == 
								     (0x1fU 
								      & (vlTOPp->Top__DOT__mem_io_imem_instruction 
									 >> 0xfU)))
								     ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_4
								     : 
								    ((3U 
								      == 
								      (0x1fU 
								       & (vlTOPp->Top__DOT__mem_io_imem_instruction 
									  >> 0xfU)))
								      ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_3
								      : 
								     ((2U 
								       == 
								       (0x1fU 
									& (vlTOPp->Top__DOT__mem_io_imem_instruction 
									   >> 0xfU)))
								       ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_2
								       : 
								      ((1U 
									== 
									(0x1fU 
									 & (vlTOPp->Top__DOT__mem_io_imem_instruction 
									    >> 0xfU)))
								        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_1
								        : vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_0)))))))))))))))))))))))))))))));
    vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata2 
	= ((0x1fU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			       >> 0x14U))) ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_31
	    : ((0x1eU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				   >> 0x14U))) ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_30
	        : ((0x1dU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				       >> 0x14U))) ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_29
		    : ((0x1cU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
					   >> 0x14U)))
		        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_28
		        : ((0x1bU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
					       >> 0x14U)))
			    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_27
			    : ((0x1aU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						   >> 0x14U)))
			        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_26
			        : ((0x19U == (0x1fU 
					      & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						 >> 0x14U)))
				    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_25
				    : ((0x18U == (0x1fU 
						  & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						     >> 0x14U)))
				        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_24
				        : ((0x17U == 
					    (0x1fU 
					     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						>> 0x14U)))
					    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_23
					    : ((0x16U 
						== 
						(0x1fU 
						 & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						    >> 0x14U)))
					        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_22
					        : (
						   (0x15U 
						    == 
						    (0x1fU 
						     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							>> 0x14U)))
						    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_21
						    : 
						   ((0x14U 
						     == 
						     (0x1fU 
						      & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							 >> 0x14U)))
						     ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_20
						     : 
						    ((0x13U 
						      == 
						      (0x1fU 
						       & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							  >> 0x14U)))
						      ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_19
						      : 
						     ((0x12U 
						       == 
						       (0x1fU 
							& (vlTOPp->Top__DOT__mem_io_imem_instruction 
							   >> 0x14U)))
						       ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_18
						       : 
						      ((0x11U 
							== 
							(0x1fU 
							 & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							    >> 0x14U)))
						        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_17
						        : 
						       ((0x10U 
							 == 
							 (0x1fU 
							  & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							     >> 0x14U)))
							 ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_16
							 : 
							((0xfU 
							  == 
							  (0x1fU 
							   & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							      >> 0x14U)))
							  ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_15
							  : 
							 ((0xeU 
							   == 
							   (0x1fU 
							    & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							       >> 0x14U)))
							   ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_14
							   : 
							  ((0xdU 
							    == 
							    (0x1fU 
							     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								>> 0x14U)))
							    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_13
							    : 
							   ((0xcU 
							     == 
							     (0x1fU 
							      & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								 >> 0x14U)))
							     ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_12
							     : 
							    ((0xbU 
							      == 
							      (0x1fU 
							       & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								  >> 0x14U)))
							      ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_11
							      : 
							     ((0xaU 
							       == 
							       (0x1fU 
								& (vlTOPp->Top__DOT__mem_io_imem_instruction 
								   >> 0x14U)))
							       ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_10
							       : 
							      ((9U 
								== 
								(0x1fU 
								 & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								    >> 0x14U)))
							        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_9
							        : 
							       ((8U 
								 == 
								 (0x1fU 
								  & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								     >> 0x14U)))
								 ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_8
								 : 
								((7U 
								  == 
								  (0x1fU 
								   & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								      >> 0x14U)))
								  ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_7
								  : 
								 ((6U 
								   == 
								   (0x1fU 
								    & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								       >> 0x14U)))
								   ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_6
								   : 
								  ((5U 
								    == 
								    (0x1fU 
								     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
									>> 0x14U)))
								    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_5
								    : 
								   ((4U 
								     == 
								     (0x1fU 
								      & (vlTOPp->Top__DOT__mem_io_imem_instruction 
									 >> 0x14U)))
								     ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_4
								     : 
								    ((3U 
								      == 
								      (0x1fU 
								       & (vlTOPp->Top__DOT__mem_io_imem_instruction 
									  >> 0x14U)))
								      ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_3
								      : 
								     ((2U 
								       == 
								       (0x1fU 
									& (vlTOPp->Top__DOT__mem_io_imem_instruction 
									   >> 0x14U)))
								       ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_2
								       : 
								      ((1U 
									== 
									(0x1fU 
									 & (vlTOPp->Top__DOT__mem_io_imem_instruction 
									    >> 0x14U)))
								        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_1
								        : vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_0)))))))))))))))))))))))))))))));
    vlTOPp->Top__DOT__cpu__DOT__control_io_immediate 
	= ((0x33U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
	   & ((0x13U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
	      | ((3U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
		 | ((0x23U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
		    | ((0x63U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
		       & ((0x37U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
			  | ((0x17U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
			     | ((0x6fU != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
				& (0x67U == (0x7fU 
					     & vlTOPp->Top__DOT__mem_io_imem_instruction))))))))));
    vlTOPp->Top__DOT__cpu__DOT__immGen__DOT___T_26 
	= ((((0x80000000U & vlTOPp->Top__DOT__mem_io_imem_instruction)
	      ? 0xfffffU : 0U) << 0xcU) | (0xfffU & 
					   (vlTOPp->Top__DOT__mem_io_imem_instruction 
					    >> 0x14U)));
    vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx = ((0U 
						  == (IData)(vlTOPp->Top__DOT__cpu__DOT__control_io_alusrc1))
						  ? vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata1
						  : 
						 ((1U 
						   == (IData)(vlTOPp->Top__DOT__cpu__DOT__control_io_alusrc1))
						   ? 0U
						   : vlTOPp->Top__DOT__cpu__DOT__pc));
    vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation 
	= (((0x33U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
	    & ((0x13U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
	       & ((3U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
		  | ((0x23U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
		     | ((0x63U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
			& ((0x37U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
			   | (0x17U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))))))))
	    ? 2U : ((0U == (7U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				  >> 0xcU))) ? (((IData)(vlTOPp->Top__DOT__cpu__DOT__control_io_immediate) 
						 | (0U 
						    == 
						    (0x7fU 
						     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							>> 0x19U))))
						 ? 2U
						 : 3U)
		     : ((1U == (7U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				      >> 0xcU))) ? 6U
			 : ((2U == (7U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
					  >> 0xcU)))
			     ? 4U : ((3U == (7U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						   >> 0xcU)))
				      ? 5U : ((4U == 
					       (7U 
						& (vlTOPp->Top__DOT__mem_io_imem_instruction 
						   >> 0xcU)))
					       ? 9U
					       : ((5U 
						   == 
						   (7U 
						    & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						       >> 0xcU)))
						   ? 
						  ((0U 
						    == 
						    (0x7fU 
						     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							>> 0x19U)))
						    ? 7U
						    : 8U)
						   : 
						  ((6U 
						    == 
						    (7U 
						     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							>> 0xcU)))
						    ? 1U
						    : 
						   ((7U 
						     == 
						     (7U 
						      & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							 >> 0xcU)))
						     ? 0U
						     : 0xfU)))))))));
    vlTOPp->Top__DOT__cpu__DOT__immGen_io_sextImm = 
	((0x37U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
	  ? (0xfffff000U & vlTOPp->Top__DOT__mem_io_imem_instruction)
	  : ((0x17U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
	      ? (0xfffff000U & vlTOPp->Top__DOT__mem_io_imem_instruction)
	      : ((0x6fU == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
		  ? ((((0x80000000U & vlTOPp->Top__DOT__mem_io_imem_instruction)
		        ? 0x7ffU : 0U) << 0x15U) | 
		     ((0x100000U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				    >> 0xbU)) | ((0xff000U 
						  & vlTOPp->Top__DOT__mem_io_imem_instruction) 
						 | ((0x800U 
						     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							>> 9U)) 
						    | (0x7feU 
						       & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							  >> 0x14U))))))
		  : ((0x67U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
		      ? vlTOPp->Top__DOT__cpu__DOT__immGen__DOT___T_26
		      : ((0x63U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
			  ? ((((0x80000000U & vlTOPp->Top__DOT__mem_io_imem_instruction)
			        ? 0x7ffffU : 0U) << 0xdU) 
			     | ((0x1000U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
					    >> 0x13U)) 
				| ((0x800U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
					      << 4U)) 
				   | ((0x7e0U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						 >> 0x14U)) 
				      | (0x1eU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						  >> 7U))))))
			  : ((3U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
			      ? vlTOPp->Top__DOT__cpu__DOT__immGen__DOT___T_26
			      : ((0x23U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
				  ? ((((0x80000000U 
					& vlTOPp->Top__DOT__mem_io_imem_instruction)
				        ? 0xfffffU : 0U) 
				      << 0xcU) | ((0xfe0U 
						   & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						      >> 0x14U)) 
						  | (0x1fU 
						     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							>> 7U))))
				  : ((0x13U == (0x7fU 
						& vlTOPp->Top__DOT__mem_io_imem_instruction))
				      ? vlTOPp->Top__DOT__cpu__DOT__immGen__DOT___T_26
				      : ((0x73U == 
					  (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
					  ? (0x1fU 
					     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						>> 0xfU))
					  : 0U)))))))));
    vlTOPp->Top__DOT__cpu__DOT__branchAdd_io_result 
	= (vlTOPp->Top__DOT__cpu__DOT__pc + vlTOPp->Top__DOT__cpu__DOT__immGen_io_sextImm);
    vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy = ((IData)(vlTOPp->Top__DOT__cpu__DOT__control_io_immediate)
						  ? vlTOPp->Top__DOT__cpu__DOT__immGen_io_sextImm
						  : vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata2);
    vlTOPp->Top__DOT__cpu__DOT__alu__DOT___T_3 = (vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx 
						  | vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy);
    vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10 = 
	(VL_ULL(0x7fffffffffffffff) & ((0U == (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
				        ? (QData)((IData)(
							  (vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx 
							   & vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy)))
				        : ((1U == (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
					    ? (QData)((IData)(vlTOPp->Top__DOT__cpu__DOT__alu__DOT___T_3))
					    : ((2U 
						== (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
					        ? (QData)((IData)(
								  (vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx 
								   + vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy)))
					        : (
						   (3U 
						    == (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
						    ? (QData)((IData)(
								      (vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx 
								       - vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy)))
						    : 
						   ((4U 
						     == (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
						     ? (QData)((IData)(
								       VL_LTS_III(1,32,32, vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx, vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy)))
						     : 
						    ((5U 
						      == (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
						      ? (QData)((IData)(
									(vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx 
									 < vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy)))
						      : 
						     ((6U 
						       == (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
						       ? 
						      ((QData)((IData)(vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx)) 
						       << 
						       (0x1fU 
							& vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy))
						       : (QData)((IData)(
									 ((7U 
									   == (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
									   ? 
									  (vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx 
									   >> 
									   (0x1fU 
									    & vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy))
									   : 
									  ((8U 
									    == (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
									    ? 
									   VL_SHIFTRS_III(32,32,5, vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx, 
										(0x1fU 
										& vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy))
									    : 
									   ((9U 
									     == (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
									     ? 
									    (vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx 
									     ^ vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy)
									     : 
									    ((0xaU 
									      == (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
									      ? 
									     (~ vlTOPp->Top__DOT__cpu__DOT__alu__DOT___T_3)
									      : 0U))))))))))))));
    vlTOPp->Top__DOT__mem__DOT__memory___05FT_49_data 
	= vlTOPp->Top__DOT__mem__DOT__memory[(0x3fffU 
					      & (IData)(
							(vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10 
							 >> 2U)))];
    vlTOPp->Top__DOT__mem__DOT___GEN_14 = ((2U != (3U 
						   & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						      >> 0xcU)))
					    ? ((0U 
						== 
						(3U 
						 & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						    >> 0xcU)))
					        ? (0xffU 
						   & vlTOPp->Top__DOT__mem__DOT__memory
						   [
						   (0x3fffU 
						    & (IData)(
							      (vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10 
							       >> 2U)))])
					        : (0xffffU 
						   & vlTOPp->Top__DOT__mem__DOT__memory
						   [
						   (0x3fffU 
						    & (IData)(
							      (vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10 
							       >> 2U)))]))
					    : vlTOPp->Top__DOT__mem__DOT__memory
					   [(0x3fffU 
					     & (IData)(
						       (vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10 
							>> 2U)))]);
    vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata 
	= ((1U == (IData)(vlTOPp->Top__DOT__cpu__DOT__control_io_toreg))
	    ? ((0x4000U & vlTOPp->Top__DOT__mem_io_imem_instruction)
	        ? vlTOPp->Top__DOT__mem__DOT___GEN_14
	        : ((0U == (3U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				 >> 0xcU))) ? ((((0x80U 
						  & vlTOPp->Top__DOT__mem__DOT___GEN_14)
						  ? 0xffffffU
						  : 0U) 
						<< 8U) 
					       | (0xffU 
						  & vlTOPp->Top__DOT__mem__DOT___GEN_14))
		    : ((1U == (3U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				     >> 0xcU))) ? (
						   (((0x8000U 
						      & vlTOPp->Top__DOT__mem__DOT___GEN_14)
						      ? 0xffffU
						      : 0U) 
						    << 0x10U) 
						   | (0xffffU 
						      & vlTOPp->Top__DOT__mem__DOT___GEN_14))
		        : vlTOPp->Top__DOT__mem__DOT___GEN_14)))
	    : ((2U == (IData)(vlTOPp->Top__DOT__cpu__DOT__control_io_toreg))
	        ? ((IData)(4U) + vlTOPp->Top__DOT__cpu__DOT__pc)
	        : (IData)(vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10)));
}

void VTop::_settle__TOP__3(VTop__Syms* __restrict vlSymsp) {
    VL_DEBUG_IF(VL_DBG_MSGF("+    VTop::_settle__TOP__3\n"); );
    VTop* __restrict vlTOPp VL_ATTR_UNUSED = vlSymsp->TOPp;
    // Body
    vlTOPp->Top__DOT__cpu__DOT__pcPlusFour_io_inputx 
	= vlTOPp->Top__DOT__cpu__DOT__pc;
    vlTOPp->Top__DOT__mem_io_imem_instruction = ((0x10000U 
						  <= vlTOPp->Top__DOT__cpu__DOT__pc)
						  ? 0U
						  : 
						 vlTOPp->Top__DOT__mem__DOT__memory
						 [(0x3fffU 
						   & (vlTOPp->Top__DOT__cpu__DOT__pc 
						      >> 2U))]);
    vlTOPp->Top__DOT__cpu__DOT__registers_io_wen = 
	(((0x33U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
	  | ((0x13U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
	     | ((3U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
		| ((0x23U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
		   & ((0x63U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
		      & ((0x37U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
			 | ((0x17U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
			    | ((0x6fU == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
			       | (0x67U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)))))))))) 
	 & (0U != (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			    >> 7U))));
    vlTOPp->Top__DOT__cpu__DOT__control_io_memwrite 
	= ((0x33U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
	   & ((0x13U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
	      & ((3U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
		 & (0x23U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)))));
    vlTOPp->Top__DOT__cpu__DOT__control_io_jump = (
						   (0x33U 
						    == 
						    (0x7fU 
						     & vlTOPp->Top__DOT__mem_io_imem_instruction))
						    ? 0U
						    : 
						   ((0x13U 
						     == 
						     (0x7fU 
						      & vlTOPp->Top__DOT__mem_io_imem_instruction))
						     ? 0U
						     : 
						    ((3U 
						      == 
						      (0x7fU 
						       & vlTOPp->Top__DOT__mem_io_imem_instruction))
						      ? 0U
						      : 
						     ((0x23U 
						       == 
						       (0x7fU 
							& vlTOPp->Top__DOT__mem_io_imem_instruction))
						       ? 0U
						       : 
						      ((0x63U 
							== 
							(0x7fU 
							 & vlTOPp->Top__DOT__mem_io_imem_instruction))
						        ? 0U
						        : 
						       ((0x37U 
							 == 
							 (0x7fU 
							  & vlTOPp->Top__DOT__mem_io_imem_instruction))
							 ? 0U
							 : 
							((0x17U 
							  == 
							  (0x7fU 
							   & vlTOPp->Top__DOT__mem_io_imem_instruction))
							  ? 0U
							  : 
							 ((0x6fU 
							   == 
							   (0x7fU 
							    & vlTOPp->Top__DOT__mem_io_imem_instruction))
							   ? 2U
							   : 
							  ((0x67U 
							    == 
							    (0x7fU 
							     & vlTOPp->Top__DOT__mem_io_imem_instruction))
							    ? 3U
							    : 0U)))))))));
    vlTOPp->Top__DOT__cpu__DOT__control_io_toreg = 
	((0x33U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
	  ? 0U : ((0x13U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
		   ? 0U : ((3U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
			    ? 1U : ((0x23U == (0x7fU 
					       & vlTOPp->Top__DOT__mem_io_imem_instruction))
				     ? 0U : ((0x63U 
					      == (0x7fU 
						  & vlTOPp->Top__DOT__mem_io_imem_instruction))
					      ? 0U : 
					     ((0x37U 
					       == (0x7fU 
						   & vlTOPp->Top__DOT__mem_io_imem_instruction))
					       ? 0U
					       : ((0x17U 
						   == 
						   (0x7fU 
						    & vlTOPp->Top__DOT__mem_io_imem_instruction))
						   ? 0U
						   : 
						  ((0x6fU 
						    == 
						    (0x7fU 
						     & vlTOPp->Top__DOT__mem_io_imem_instruction))
						    ? 2U
						    : 
						   ((0x67U 
						     == 
						     (0x7fU 
						      & vlTOPp->Top__DOT__mem_io_imem_instruction))
						     ? 2U
						     : 3U)))))))));
    vlTOPp->Top__DOT__cpu__DOT__control_io_alusrc1 
	= ((0x33U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
	    ? 0U : ((0x13U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
		     ? 0U : ((3U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
			      ? 0U : ((0x23U == (0x7fU 
						 & vlTOPp->Top__DOT__mem_io_imem_instruction))
				       ? 0U : ((0x63U 
						== 
						(0x7fU 
						 & vlTOPp->Top__DOT__mem_io_imem_instruction))
					        ? 0U
					        : (
						   (0x37U 
						    == 
						    (0x7fU 
						     & vlTOPp->Top__DOT__mem_io_imem_instruction))
						    ? 1U
						    : 
						   ((0x17U 
						     == 
						     (0x7fU 
						      & vlTOPp->Top__DOT__mem_io_imem_instruction))
						     ? 2U
						     : 
						    (0x6fU 
						     == 
						     (0x7fU 
						      & vlTOPp->Top__DOT__mem_io_imem_instruction)))))))));
    vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata1 
	= ((0x1fU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			       >> 0xfU))) ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_31
	    : ((0x1eU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				   >> 0xfU))) ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_30
	        : ((0x1dU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				       >> 0xfU))) ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_29
		    : ((0x1cU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
					   >> 0xfU)))
		        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_28
		        : ((0x1bU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
					       >> 0xfU)))
			    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_27
			    : ((0x1aU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						   >> 0xfU)))
			        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_26
			        : ((0x19U == (0x1fU 
					      & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						 >> 0xfU)))
				    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_25
				    : ((0x18U == (0x1fU 
						  & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						     >> 0xfU)))
				        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_24
				        : ((0x17U == 
					    (0x1fU 
					     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						>> 0xfU)))
					    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_23
					    : ((0x16U 
						== 
						(0x1fU 
						 & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						    >> 0xfU)))
					        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_22
					        : (
						   (0x15U 
						    == 
						    (0x1fU 
						     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							>> 0xfU)))
						    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_21
						    : 
						   ((0x14U 
						     == 
						     (0x1fU 
						      & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							 >> 0xfU)))
						     ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_20
						     : 
						    ((0x13U 
						      == 
						      (0x1fU 
						       & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							  >> 0xfU)))
						      ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_19
						      : 
						     ((0x12U 
						       == 
						       (0x1fU 
							& (vlTOPp->Top__DOT__mem_io_imem_instruction 
							   >> 0xfU)))
						       ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_18
						       : 
						      ((0x11U 
							== 
							(0x1fU 
							 & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							    >> 0xfU)))
						        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_17
						        : 
						       ((0x10U 
							 == 
							 (0x1fU 
							  & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							     >> 0xfU)))
							 ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_16
							 : 
							((0xfU 
							  == 
							  (0x1fU 
							   & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							      >> 0xfU)))
							  ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_15
							  : 
							 ((0xeU 
							   == 
							   (0x1fU 
							    & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							       >> 0xfU)))
							   ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_14
							   : 
							  ((0xdU 
							    == 
							    (0x1fU 
							     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								>> 0xfU)))
							    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_13
							    : 
							   ((0xcU 
							     == 
							     (0x1fU 
							      & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								 >> 0xfU)))
							     ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_12
							     : 
							    ((0xbU 
							      == 
							      (0x1fU 
							       & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								  >> 0xfU)))
							      ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_11
							      : 
							     ((0xaU 
							       == 
							       (0x1fU 
								& (vlTOPp->Top__DOT__mem_io_imem_instruction 
								   >> 0xfU)))
							       ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_10
							       : 
							      ((9U 
								== 
								(0x1fU 
								 & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								    >> 0xfU)))
							        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_9
							        : 
							       ((8U 
								 == 
								 (0x1fU 
								  & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								     >> 0xfU)))
								 ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_8
								 : 
								((7U 
								  == 
								  (0x1fU 
								   & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								      >> 0xfU)))
								  ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_7
								  : 
								 ((6U 
								   == 
								   (0x1fU 
								    & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								       >> 0xfU)))
								   ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_6
								   : 
								  ((5U 
								    == 
								    (0x1fU 
								     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
									>> 0xfU)))
								    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_5
								    : 
								   ((4U 
								     == 
								     (0x1fU 
								      & (vlTOPp->Top__DOT__mem_io_imem_instruction 
									 >> 0xfU)))
								     ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_4
								     : 
								    ((3U 
								      == 
								      (0x1fU 
								       & (vlTOPp->Top__DOT__mem_io_imem_instruction 
									  >> 0xfU)))
								      ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_3
								      : 
								     ((2U 
								       == 
								       (0x1fU 
									& (vlTOPp->Top__DOT__mem_io_imem_instruction 
									   >> 0xfU)))
								       ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_2
								       : 
								      ((1U 
									== 
									(0x1fU 
									 & (vlTOPp->Top__DOT__mem_io_imem_instruction 
									    >> 0xfU)))
								        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_1
								        : vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_0)))))))))))))))))))))))))))))));
    vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata2 
	= ((0x1fU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
			       >> 0x14U))) ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_31
	    : ((0x1eU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				   >> 0x14U))) ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_30
	        : ((0x1dU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				       >> 0x14U))) ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_29
		    : ((0x1cU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
					   >> 0x14U)))
		        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_28
		        : ((0x1bU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
					       >> 0x14U)))
			    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_27
			    : ((0x1aU == (0x1fU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						   >> 0x14U)))
			        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_26
			        : ((0x19U == (0x1fU 
					      & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						 >> 0x14U)))
				    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_25
				    : ((0x18U == (0x1fU 
						  & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						     >> 0x14U)))
				        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_24
				        : ((0x17U == 
					    (0x1fU 
					     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						>> 0x14U)))
					    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_23
					    : ((0x16U 
						== 
						(0x1fU 
						 & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						    >> 0x14U)))
					        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_22
					        : (
						   (0x15U 
						    == 
						    (0x1fU 
						     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							>> 0x14U)))
						    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_21
						    : 
						   ((0x14U 
						     == 
						     (0x1fU 
						      & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							 >> 0x14U)))
						     ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_20
						     : 
						    ((0x13U 
						      == 
						      (0x1fU 
						       & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							  >> 0x14U)))
						      ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_19
						      : 
						     ((0x12U 
						       == 
						       (0x1fU 
							& (vlTOPp->Top__DOT__mem_io_imem_instruction 
							   >> 0x14U)))
						       ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_18
						       : 
						      ((0x11U 
							== 
							(0x1fU 
							 & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							    >> 0x14U)))
						        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_17
						        : 
						       ((0x10U 
							 == 
							 (0x1fU 
							  & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							     >> 0x14U)))
							 ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_16
							 : 
							((0xfU 
							  == 
							  (0x1fU 
							   & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							      >> 0x14U)))
							  ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_15
							  : 
							 ((0xeU 
							   == 
							   (0x1fU 
							    & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							       >> 0x14U)))
							   ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_14
							   : 
							  ((0xdU 
							    == 
							    (0x1fU 
							     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								>> 0x14U)))
							    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_13
							    : 
							   ((0xcU 
							     == 
							     (0x1fU 
							      & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								 >> 0x14U)))
							     ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_12
							     : 
							    ((0xbU 
							      == 
							      (0x1fU 
							       & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								  >> 0x14U)))
							      ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_11
							      : 
							     ((0xaU 
							       == 
							       (0x1fU 
								& (vlTOPp->Top__DOT__mem_io_imem_instruction 
								   >> 0x14U)))
							       ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_10
							       : 
							      ((9U 
								== 
								(0x1fU 
								 & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								    >> 0x14U)))
							        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_9
							        : 
							       ((8U 
								 == 
								 (0x1fU 
								  & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								     >> 0x14U)))
								 ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_8
								 : 
								((7U 
								  == 
								  (0x1fU 
								   & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								      >> 0x14U)))
								  ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_7
								  : 
								 ((6U 
								   == 
								   (0x1fU 
								    & (vlTOPp->Top__DOT__mem_io_imem_instruction 
								       >> 0x14U)))
								   ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_6
								   : 
								  ((5U 
								    == 
								    (0x1fU 
								     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
									>> 0x14U)))
								    ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_5
								    : 
								   ((4U 
								     == 
								     (0x1fU 
								      & (vlTOPp->Top__DOT__mem_io_imem_instruction 
									 >> 0x14U)))
								     ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_4
								     : 
								    ((3U 
								      == 
								      (0x1fU 
								       & (vlTOPp->Top__DOT__mem_io_imem_instruction 
									  >> 0x14U)))
								      ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_3
								      : 
								     ((2U 
								       == 
								       (0x1fU 
									& (vlTOPp->Top__DOT__mem_io_imem_instruction 
									   >> 0x14U)))
								       ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_2
								       : 
								      ((1U 
									== 
									(0x1fU 
									 & (vlTOPp->Top__DOT__mem_io_imem_instruction 
									    >> 0x14U)))
								        ? vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_1
								        : vlTOPp->Top__DOT__cpu__DOT__registers__DOT__regs_0)))))))))))))))))))))))))))))));
    vlTOPp->Top__DOT__cpu__DOT__control_io_immediate 
	= ((0x33U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
	   & ((0x13U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
	      | ((3U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
		 | ((0x23U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
		    | ((0x63U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
		       & ((0x37U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
			  | ((0x17U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
			     | ((0x6fU != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
				& (0x67U == (0x7fU 
					     & vlTOPp->Top__DOT__mem_io_imem_instruction))))))))));
    vlTOPp->Top__DOT__cpu__DOT__immGen__DOT___T_26 
	= ((((0x80000000U & vlTOPp->Top__DOT__mem_io_imem_instruction)
	      ? 0xfffffU : 0U) << 0xcU) | (0xfffU & 
					   (vlTOPp->Top__DOT__mem_io_imem_instruction 
					    >> 0x14U)));
    vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx = ((0U 
						  == (IData)(vlTOPp->Top__DOT__cpu__DOT__control_io_alusrc1))
						  ? vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata1
						  : 
						 ((1U 
						   == (IData)(vlTOPp->Top__DOT__cpu__DOT__control_io_alusrc1))
						   ? 0U
						   : vlTOPp->Top__DOT__cpu__DOT__pc));
    vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation 
	= (((0x33U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
	    & ((0x13U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
	       & ((3U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
		  | ((0x23U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
		     | ((0x63U != (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
			& ((0x37U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction)) 
			   | (0x17U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))))))))
	    ? 2U : ((0U == (7U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				  >> 0xcU))) ? (((IData)(vlTOPp->Top__DOT__cpu__DOT__control_io_immediate) 
						 | (0U 
						    == 
						    (0x7fU 
						     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							>> 0x19U))))
						 ? 2U
						 : 3U)
		     : ((1U == (7U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				      >> 0xcU))) ? 6U
			 : ((2U == (7U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
					  >> 0xcU)))
			     ? 4U : ((3U == (7U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						   >> 0xcU)))
				      ? 5U : ((4U == 
					       (7U 
						& (vlTOPp->Top__DOT__mem_io_imem_instruction 
						   >> 0xcU)))
					       ? 9U
					       : ((5U 
						   == 
						   (7U 
						    & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						       >> 0xcU)))
						   ? 
						  ((0U 
						    == 
						    (0x7fU 
						     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							>> 0x19U)))
						    ? 7U
						    : 8U)
						   : 
						  ((6U 
						    == 
						    (7U 
						     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							>> 0xcU)))
						    ? 1U
						    : 
						   ((7U 
						     == 
						     (7U 
						      & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							 >> 0xcU)))
						     ? 0U
						     : 0xfU)))))))));
    vlTOPp->Top__DOT__cpu__DOT__immGen_io_sextImm = 
	((0x37U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
	  ? (0xfffff000U & vlTOPp->Top__DOT__mem_io_imem_instruction)
	  : ((0x17U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
	      ? (0xfffff000U & vlTOPp->Top__DOT__mem_io_imem_instruction)
	      : ((0x6fU == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
		  ? ((((0x80000000U & vlTOPp->Top__DOT__mem_io_imem_instruction)
		        ? 0x7ffU : 0U) << 0x15U) | 
		     ((0x100000U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				    >> 0xbU)) | ((0xff000U 
						  & vlTOPp->Top__DOT__mem_io_imem_instruction) 
						 | ((0x800U 
						     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							>> 9U)) 
						    | (0x7feU 
						       & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							  >> 0x14U))))))
		  : ((0x67U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
		      ? vlTOPp->Top__DOT__cpu__DOT__immGen__DOT___T_26
		      : ((0x63U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
			  ? ((((0x80000000U & vlTOPp->Top__DOT__mem_io_imem_instruction)
			        ? 0x7ffffU : 0U) << 0xdU) 
			     | ((0x1000U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
					    >> 0x13U)) 
				| ((0x800U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
					      << 4U)) 
				   | ((0x7e0U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						 >> 0x14U)) 
				      | (0x1eU & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						  >> 7U))))))
			  : ((3U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
			      ? vlTOPp->Top__DOT__cpu__DOT__immGen__DOT___T_26
			      : ((0x23U == (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
				  ? ((((0x80000000U 
					& vlTOPp->Top__DOT__mem_io_imem_instruction)
				        ? 0xfffffU : 0U) 
				      << 0xcU) | ((0xfe0U 
						   & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						      >> 0x14U)) 
						  | (0x1fU 
						     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
							>> 7U))))
				  : ((0x13U == (0x7fU 
						& vlTOPp->Top__DOT__mem_io_imem_instruction))
				      ? vlTOPp->Top__DOT__cpu__DOT__immGen__DOT___T_26
				      : ((0x73U == 
					  (0x7fU & vlTOPp->Top__DOT__mem_io_imem_instruction))
					  ? (0x1fU 
					     & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						>> 0xfU))
					  : 0U)))))))));
    vlTOPp->Top__DOT__cpu__DOT__branchAdd_io_result 
	= (vlTOPp->Top__DOT__cpu__DOT__pc + vlTOPp->Top__DOT__cpu__DOT__immGen_io_sextImm);
    vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy = ((IData)(vlTOPp->Top__DOT__cpu__DOT__control_io_immediate)
						  ? vlTOPp->Top__DOT__cpu__DOT__immGen_io_sextImm
						  : vlTOPp->Top__DOT__cpu__DOT__registers_io_readdata2);
    vlTOPp->Top__DOT__cpu__DOT__alu__DOT___T_3 = (vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx 
						  | vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy);
    vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10 = 
	(VL_ULL(0x7fffffffffffffff) & ((0U == (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
				        ? (QData)((IData)(
							  (vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx 
							   & vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy)))
				        : ((1U == (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
					    ? (QData)((IData)(vlTOPp->Top__DOT__cpu__DOT__alu__DOT___T_3))
					    : ((2U 
						== (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
					        ? (QData)((IData)(
								  (vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx 
								   + vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy)))
					        : (
						   (3U 
						    == (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
						    ? (QData)((IData)(
								      (vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx 
								       - vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy)))
						    : 
						   ((4U 
						     == (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
						     ? (QData)((IData)(
								       VL_LTS_III(1,32,32, vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx, vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy)))
						     : 
						    ((5U 
						      == (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
						      ? (QData)((IData)(
									(vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx 
									 < vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy)))
						      : 
						     ((6U 
						       == (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
						       ? 
						      ((QData)((IData)(vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx)) 
						       << 
						       (0x1fU 
							& vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy))
						       : (QData)((IData)(
									 ((7U 
									   == (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
									   ? 
									  (vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx 
									   >> 
									   (0x1fU 
									    & vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy))
									   : 
									  ((8U 
									    == (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
									    ? 
									   VL_SHIFTRS_III(32,32,5, vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx, 
										(0x1fU 
										& vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy))
									    : 
									   ((9U 
									     == (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
									     ? 
									    (vlTOPp->Top__DOT__cpu__DOT__alu_io_inputx 
									     ^ vlTOPp->Top__DOT__cpu__DOT__alu_io_inputy)
									     : 
									    ((0xaU 
									      == (IData)(vlTOPp->Top__DOT__cpu__DOT__aluControl_io_operation))
									      ? 
									     (~ vlTOPp->Top__DOT__cpu__DOT__alu__DOT___T_3)
									      : 0U))))))))))))));
    vlTOPp->Top__DOT__mem__DOT__memory___05FT_49_data 
	= vlTOPp->Top__DOT__mem__DOT__memory[(0x3fffU 
					      & (IData)(
							(vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10 
							 >> 2U)))];
    vlTOPp->Top__DOT__mem__DOT___GEN_14 = ((2U != (3U 
						   & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						      >> 0xcU)))
					    ? ((0U 
						== 
						(3U 
						 & (vlTOPp->Top__DOT__mem_io_imem_instruction 
						    >> 0xcU)))
					        ? (0xffU 
						   & vlTOPp->Top__DOT__mem__DOT__memory
						   [
						   (0x3fffU 
						    & (IData)(
							      (vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10 
							       >> 2U)))])
					        : (0xffffU 
						   & vlTOPp->Top__DOT__mem__DOT__memory
						   [
						   (0x3fffU 
						    & (IData)(
							      (vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10 
							       >> 2U)))]))
					    : vlTOPp->Top__DOT__mem__DOT__memory
					   [(0x3fffU 
					     & (IData)(
						       (vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10 
							>> 2U)))]);
    vlTOPp->Top__DOT__cpu__DOT__registers_io_writedata 
	= ((1U == (IData)(vlTOPp->Top__DOT__cpu__DOT__control_io_toreg))
	    ? ((0x4000U & vlTOPp->Top__DOT__mem_io_imem_instruction)
	        ? vlTOPp->Top__DOT__mem__DOT___GEN_14
	        : ((0U == (3U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				 >> 0xcU))) ? ((((0x80U 
						  & vlTOPp->Top__DOT__mem__DOT___GEN_14)
						  ? 0xffffffU
						  : 0U) 
						<< 8U) 
					       | (0xffU 
						  & vlTOPp->Top__DOT__mem__DOT___GEN_14))
		    : ((1U == (3U & (vlTOPp->Top__DOT__mem_io_imem_instruction 
				     >> 0xcU))) ? (
						   (((0x8000U 
						      & vlTOPp->Top__DOT__mem__DOT___GEN_14)
						      ? 0xffffU
						      : 0U) 
						    << 0x10U) 
						   | (0xffffU 
						      & vlTOPp->Top__DOT__mem__DOT___GEN_14))
		        : vlTOPp->Top__DOT__mem__DOT___GEN_14)))
	    : ((2U == (IData)(vlTOPp->Top__DOT__cpu__DOT__control_io_toreg))
	        ? ((IData)(4U) + vlTOPp->Top__DOT__cpu__DOT__pc)
	        : (IData)(vlTOPp->Top__DOT__cpu__DOT__alu__DOT___GEN_10)));
}

void VTop::_eval(VTop__Syms* __restrict vlSymsp) {
    VL_DEBUG_IF(VL_DBG_MSGF("+    VTop::_eval\n"); );
    VTop* __restrict vlTOPp VL_ATTR_UNUSED = vlSymsp->TOPp;
    // Body
    if (((IData)(vlTOPp->clock) & (~ (IData)(vlTOPp->__Vclklast__TOP__clock)))) {
	vlTOPp->_sequent__TOP__2(vlSymsp);
    }
    // Final
    vlTOPp->__Vclklast__TOP__clock = vlTOPp->clock;
}

void VTop::_eval_initial(VTop__Syms* __restrict vlSymsp) {
    VL_DEBUG_IF(VL_DBG_MSGF("+    VTop::_eval_initial\n"); );
    VTop* __restrict vlTOPp VL_ATTR_UNUSED = vlSymsp->TOPp;
    // Body
    vlTOPp->_initial__TOP__1(vlSymsp);
}

void VTop::final() {
    VL_DEBUG_IF(VL_DBG_MSGF("+    VTop::final\n"); );
    // Variables
    VTop__Syms* __restrict vlSymsp = this->__VlSymsp;
    VTop* __restrict vlTOPp VL_ATTR_UNUSED = vlSymsp->TOPp;
}

void VTop::_eval_settle(VTop__Syms* __restrict vlSymsp) {
    VL_DEBUG_IF(VL_DBG_MSGF("+    VTop::_eval_settle\n"); );
    VTop* __restrict vlTOPp VL_ATTR_UNUSED = vlSymsp->TOPp;
    // Body
    vlTOPp->_settle__TOP__3(vlSymsp);
}

VL_INLINE_OPT QData VTop::_change_request(VTop__Syms* __restrict vlSymsp) {
    VL_DEBUG_IF(VL_DBG_MSGF("+    VTop::_change_request\n"); );
    VTop* __restrict vlTOPp VL_ATTR_UNUSED = vlSymsp->TOPp;
    // Body
    // Change detection
    QData __req = false;  // Logically a bool
    return __req;
}

#ifdef VL_DEBUG
void VTop::_eval_debug_assertions() {
    VL_DEBUG_IF(VL_DBG_MSGF("+    VTop::_eval_debug_assertions\n"); );
    // Body
    if (VL_UNLIKELY((clock & 0xfeU))) {
	Verilated::overWidthError("clock");}
    if (VL_UNLIKELY((reset & 0xfeU))) {
	Verilated::overWidthError("reset");}
}
#endif // VL_DEBUG

void VTop::_ctor_var_reset() {
    VL_DEBUG_IF(VL_DBG_MSGF("+    VTop::_ctor_var_reset\n"); );
    // Body
    clock = VL_RAND_RESET_I(1);
    reset = VL_RAND_RESET_I(1);
    io_success = VL_RAND_RESET_I(1);
    Top__DOT__mem_io_imem_instruction = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__control_io_toreg = VL_RAND_RESET_I(2);
    Top__DOT__cpu__DOT__control_io_memwrite = VL_RAND_RESET_I(1);
    Top__DOT__cpu__DOT__control_io_immediate = VL_RAND_RESET_I(1);
    Top__DOT__cpu__DOT__control_io_alusrc1 = VL_RAND_RESET_I(2);
    Top__DOT__cpu__DOT__control_io_jump = VL_RAND_RESET_I(2);
    Top__DOT__cpu__DOT__registers_io_writedata = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers_io_wen = VL_RAND_RESET_I(1);
    Top__DOT__cpu__DOT__registers_io_readdata1 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers_io_readdata2 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__aluControl_io_operation = VL_RAND_RESET_I(4);
    Top__DOT__cpu__DOT__alu_io_inputx = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__alu_io_inputy = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__immGen_io_sextImm = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__pcPlusFour_io_inputx = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__branchAdd_io_result = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__pc = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_0 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_1 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_2 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_3 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_4 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_5 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_6 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_7 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_8 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_9 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_10 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_11 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_12 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_13 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_14 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_15 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_16 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_17 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_18 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_19 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_20 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_21 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_22 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_23 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_24 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_25 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_26 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_27 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_28 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_29 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_30 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__registers__DOT__regs_31 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__alu__DOT___T_3 = VL_RAND_RESET_I(32);
    Top__DOT__cpu__DOT__alu__DOT___GEN_10 = VL_RAND_RESET_Q(63);
    Top__DOT__cpu__DOT__immGen__DOT___T_26 = VL_RAND_RESET_I(32);
    { int __Vi0=0; for (; __Vi0<16384; ++__Vi0) {
	    Top__DOT__mem__DOT__memory[__Vi0] = VL_RAND_RESET_I(32);
    }}
    Top__DOT__mem__DOT__memory___05FT_49_data = VL_RAND_RESET_I(32);
    Top__DOT__mem__DOT___GEN_14 = VL_RAND_RESET_I(32);
    __Vclklast__TOP__clock = VL_RAND_RESET_I(1);
}
