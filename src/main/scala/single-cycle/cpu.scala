// This file is where all of the CPU components are assembled into the whole CPU

package edu.darchr.codcpu

import chisel3._

/**
 * The main CPU definition that hooks up all of the other components.
 *
 * For more information, see section 4.4 of Patterson and Hennessy
 * This follows figure 4.21
 */
class CPU extends Module {

    // All of the structures required
    val pc         = Reg(UInt(32.W))
    val instMem    = Module(new InstructionMemory())
    val control    = Module(new Control())
    val registers  = Module(new RegisterFile())
    val aluControl = Module(new ALUControl())
    val alu        = Module(new ALU())
    val immGen     = Module(new ImmediateGenerator())
    val dataMem    = Module(new DataMemory())
    val pcPlusFour = Module(new Adder())
    val branchAdd  = Module(new Adder())

    instMem.io.address := pc

    pcPlusFour.io.inputx := pc
    pcPlusFour.io.inputy := 4.U

    val instruction = instMem.io.instruction
    val opcode = instruction(0,6)

    control.io.opcode := opcode

    registers.io.readreg1 := instruction(19,15)
    registers.io.readreg2 := instruction(24,20)

    registers.io.writereg := instruction(11,7)
    registers.io.wen      := control.io.regwrite

    aluControl.io.aluop  := control.io.aluop
    aluControl.io.funct7 := instruction(25,31)
    aluControl.io.funct3 := instruction(12,14)

    immGen.io.instruction := instruction
    val imm = immGen.io.sextImm

    val alu_inputy = Mux(control.io.alusrc, imm, registers.io.readdata2)
    alu.io.inputx := registers.io.readdata1
    alu.io.inputy := alu_inputy
    alu.io.operation := aluControl.io.operation

    dataMem.io.address   := alu.io.result
    dataMem.io.writedata := registers.io.readdata2
    dataMem.io.memread   := control.io.memread
    dataMem.io.memwrite  := control.io.memwrite

    val write_data = Mux(control.io.memtoreg, dataMem.io.readdata, alu.io.result)
    registers.io.writedata := write_data

    branchAdd.io.inputx := pc
    branchAdd.io.inputy := imm
    val next_pc = Mux(control.io.branch & alu.io.zero,
                      branchAdd.io.result,
                      pcPlusFour.io.result)

    pc := next_pc

}