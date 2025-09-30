package tritium.command.impl;

import tritium.command.Command;
import tritium.management.ModuleManager;

public class NSpoof extends Command {

    public NSpoof() {
        super("Name Spoof", "Spoof ur name.", "ns <name>", "ns");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            this.printUsage();
            return;
        }

        ModuleManager.nameSpoof.name = args[0];
        this.print("OK! [" + ModuleManager.nameSpoof.name + "]");
    }

}
