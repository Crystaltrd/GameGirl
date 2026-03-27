
void main(String[] args) {
    try {
        if (args.length > 0) {
            Cartridge card = new Cartridge(new File(args[0]));
            IO.println(card.header.humanReadable());
            IO.println(card.header);
        }
        InstructionSet instructionSet = InstructionSet.fromFile(new File("assets/JSON/Opcodes.json"));
        IO.println(instructionSet.getUnprefixed().get("0x05"));
    } catch (IOException e) {
        throw new RuntimeException(e);
    }

}
