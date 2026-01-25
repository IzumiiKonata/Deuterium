package tritium.command.impl;

import tritium.command.Command;
import tritium.command.CommandHandler;

/**
 * @author IzumiiKonata
 * @since 2023/12/17
 */
public class Rot extends Command {

    public Rot() {
        super("Rot", "Rot", "Rot <yaw> <pitch>", "rot");
    }

    @CommandHandler(paramNames = {"yaw", "pitch"})
    public void execute(float yaw, float pitch) {
        mc.thePlayer.rotationYaw = yaw;
        mc.thePlayer.rotationPitch = pitch;
    }
}
