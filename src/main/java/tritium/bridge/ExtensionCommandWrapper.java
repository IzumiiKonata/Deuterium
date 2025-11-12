package tritium.bridge;

import today.opai.api.features.ExtensionCommand;
import tritium.command.Command;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 19:11
 */
public class ExtensionCommandWrapper extends Command {

    private final ExtensionCommand command;

    public ExtensionCommandWrapper(ExtensionCommand command) {
        super(command.getNames()[0], command.getDescription(), command.getUsage(), command.getNames());
        this.command = command;
    }

    public void execute(String[] args) {
        // dirty fix
        String[] params = new String[args.length + 1];
        params[0] = getName();
        System.arraycopy(args, 0, params, 1, args.length);
        command.onExecute(params);
    }
}
