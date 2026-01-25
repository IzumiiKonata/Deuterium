package tritium.command.impl;

import tritium.command.Command;
import tritium.command.CommandHandler;
import tritium.management.ModuleManager;

public class NameSpoof extends Command {

    public NameSpoof() {
        super("Name Spoof", "Spoof ur name.", "ns <name>", "ns");
    }

    @CommandHandler(paramNames = {"name"})
    public void execute(String name) {
        ModuleManager.nameSpoof.name = name;
        this.print("OK! [" + ModuleManager.nameSpoof.name + "]");
    }

}
