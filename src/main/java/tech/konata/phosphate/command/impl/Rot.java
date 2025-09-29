package tech.konata.phosphate.command.impl;

import tech.konata.phosphate.command.Command;

/**
 * @author IzumiiKonata
 * @since 2023/12/17
 */
public class Rot extends Command {

    public Rot() {
        super("Rot", "Rot", "Rot <yaw> <pitch>", "rot");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 2) {
            float yaw = Float.parseFloat(args[0]);
            float pitch = Float.parseFloat(args[1]);

            mc.thePlayer.rotationYaw = yaw;
            mc.thePlayer.rotationPitch = pitch;
        }
    }
}
