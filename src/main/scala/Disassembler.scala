package dinocpu
class Disassembler {
  val R_TYPE_OPCODE = Integer.parseInt("0110011",2)
  val I_TYPE_OPCODE = Integer.parseInt("0010011",2)
  val STORE_OPCODE = Integer.parseInt("0100011",2)
  val LOAD_OPCODE = Integer.parseInt("0000011",2)
  val BRANCH_OPCODE = Integer.parseInt("1100011",2)
  val JAR_OPCODE = Integer.parseInt("1101111",2)
  val JALR_OPCODE = Integer.parseInt("1100111",2)
  val AUIPC_OPCODE =  Integer.parseInt("0010111",2)
  val LUI_OPCODE =  Integer.parseInt("0110111",2)

  def sliceLong(num:Long, upper: Int,lower: Int): Long = {
   var mask: Long = scala.math.pow(2,upper - lower+1).toLong - 1
    mask = mask << lower
    //println(mask)
    (num & mask) >> lower
  }

  def parseRType(instruction:Long):String={
    //println("R-type")
    val rd = sliceLong(instruction,11,7)
    val funct3 = sliceLong(instruction,14,12)
    val rs1 = sliceLong(instruction,19,15)
    val rs2 = sliceLong(instruction,24,20)
    val funct7 = sliceLong(instruction,31,25)

    val instName = funct3 match {
      case 0 => funct7 match {
        case 0 => "add"
        case 32 => "sub"
      }
      case 1 => "sll"
      case 2 => "slt"
      case 3 => "sltu"
      case 4 => "xor"
      case 5 => funct7 match {
        case 0 => "srl"
        case 32 => "sra"
      }
      case 6 => "or"
      case 7 => "and"
    }
    instName + " x" + rd + ", x" + rs1 + ", x" + rs2
  }
  def parseIType(instruction:Long)={
    val rd = sliceLong(instruction,11,7)
    val funct3 = sliceLong(instruction,14,12)
    val rs1 = sliceLong(instruction,19,15)
    val funct7 = sliceLong(instruction,31,25)
    val imm = sliceLong(instruction,31,20)
    val shamt = sliceLong(instruction,24,20)
    funct3 match {
      case 0 => "addi" + " x" + rd + ", x" + rs1 + ", " + imm
      case 2 => "slti" +  " x" + rd + ", x" + rs1 + ", " + imm
      case 3 => "sltiu" +  " x" + rd + ", x" + rs1 + ", " + imm
      case 4 => "xori" +  " x" + rd + ", x" + rs1 + ", " + imm
      case 6 => "ori" +  " x" + rd + ", x" + rs1 + ", " + imm
      case 7 => "andi" + " x" + rd + ", x" + rs1 + ", " + imm
      case 1 => "slli" " x" + rd + ", x" + rs1 + ", " + shamt
      case 5 => funct7 match {
        case 0 => "srli" +  " x" + rd + ", x" + rs1 + ", " + shamt
        case 8 => "srai" +  " x" + rd + ", x" + rs1 + ", " + shamt
      }
    }
  }
  def parseStore(instruction:Long):String={
    val offset = sliceLong(instruction,31,25) << 5 + sliceLong(instruction,11,7)
    val rs1 = sliceLong(instruction,19,15)
    val rs2 = sliceLong(instruction,24,20)
    val funct3 = sliceLong(instruction,14,12)
    val instName = funct3 match {
      case 0 => "sb"
      case 1 => "sh"
      case 2 => "sw"
    }
    instName + " x" + rs2 + ", x" + rs1 +" (" + offset +")"
  }

  def parseLoad(instruction:Long)={
    val rd = sliceLong(instruction,11,7)
    val funct3 = sliceLong(instruction,14,12)
    val rs1 = sliceLong(instruction,19,15)
    val instName = funct3 match {
      case 0 => "lb"
      case 1 => "lh"
      case 2 => "lw"
      case 4 => "lbu"
      case 5 => "lhu"
    }
    val imm = sliceLong(instruction,31,20)
    instName + " x" + rd + ", x" + rs1 +" (" + imm +")"
  }

  def parseBranch(instruction:Long):String={
    val funct3 = sliceLong(instruction,14,12)
    val rs1 = sliceLong(instruction,19,15)
    val rs2 = sliceLong(instruction,24,20)
    val offset = sliceLong(instruction,7,7) << 10 + sliceLong(instruction,30,25) << 4 + sliceLong(instruction,11,8)
    val instName = funct3 match {
      case 0 => "beq"
      case 1 => "bne"
      case 4 => "blt"
      case 5 => "bge"
      case 6 => "bltu"
      case 7 => "bgeu"
    }
    instName + " x" + rs1 + ", x" + rs2 + "(" + offset + ")"
  }
  def parseJal(instruction:Long):String={
    val rd = sliceLong(instruction,11,7)
    val imm = sliceLong(instruction,19,12) << 12 +
      sliceLong(instruction,20,20) << 10 +
      sliceLong(instruction,30,21) << 1
    "jal x" + rd + "(" + imm + ")"
  }
  def parseJalr(instruction:Long):String={
    val rd = sliceLong(instruction,11,7)
    val rs1 = sliceLong(instruction,19,15)
    val imm = sliceLong(instruction,30,20)
    "jalr x" + rd +", x" + rs1 + "(" + imm + ")"
   }
  def parseAuipc(instruction:Long):String={
    val rd = sliceLong(instruction,11,7)
    val imm = sliceLong(instruction,30,20)
    "lui x" + rd + " " + imm
  }
  def parseLui(instruction:Long):String={
    val rd = sliceLong(instruction,11,7)
    val imm = sliceLong(instruction,30,20)
    "auipc x" + rd + " " + imm
  }

  def dissamble(instruction:Long):String  = {
    println(instruction)
    val opCode = sliceLong(instruction,6,0)
    println(opCode)
    val str = opCode match {
      case R_TYPE_OPCODE => parseRType(instruction)
      case I_TYPE_OPCODE => parseIType(instruction)
      case STORE_OPCODE => parseStore(instruction)
      case LOAD_OPCODE => parseLoad(instruction)
      case BRANCH_OPCODE => parseBranch(instruction)
      case JAR_OPCODE => parseJal(instruction)
      case JALR_OPCODE => parseJalr(instruction)
      case AUIPC_OPCODE => parseBranch(instruction)
      case LUI_OPCODE => parseBranch(instruction)
      case _ => "Unknown"
    }
    println(str)
    str
  }
  def main (args: Array[String] ): Unit = {
    dissamble(Integer.parseInt("00000000000000000000001010110011"))
  }
}

