module BindsTo_0_DualPortedMemory(
  input         clock,
  input         reset,
  input  [31:0] io_imem_address,
  output [31:0] io_imem_instruction,
  input  [31:0] io_dmem_address,
  input  [31:0] io_dmem_writedata,
  input         io_dmem_memread,
  input         io_dmem_memwrite,
  input  [1:0]  io_dmem_maskmode,
  input         io_dmem_sext,
  output [31:0] io_dmem_readdata
);

initial begin
  $readmemh("test", DualPortedMemory.memory);
end
                      endmodule

bind DualPortedMemory BindsTo_0_DualPortedMemory BindsTo_0_DualPortedMemory_Inst(.*);