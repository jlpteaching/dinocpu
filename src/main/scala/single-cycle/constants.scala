// Where we put constant definitions

package edu.darchr.codcpu

import chisel3._

trait ALUConstants
{
  val AND_OP = 0.U
  val OR_OP  = 1.U
  val ADD_OP = 2.U
  val SUB_OP = 6.U
  val SLT_OP = 7.U
  val NOR_OP = 12.U
}

object Constants extends
    ALUConstants
{
}