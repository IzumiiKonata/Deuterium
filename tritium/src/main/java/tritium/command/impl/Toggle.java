package tritium.command.impl;

import lombok.SneakyThrows;
import net.minecraft.util.EnumChatFormatting;
import tritium.command.Command;
import tritium.command.CommandValues;
import tritium.utils.other.Result;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @author IzumiiKonata
 * @since 6/16/2023 3:43 PM
 */
public class Toggle extends Command {

    public Toggle() {
        super("Toggle", "Toggles the setting with multiple values.", "toggle <setting> <value1> <value2> ...", "t");
    }

    @SneakyThrows
    public void execute(String[] args) {

        if (args.length < 2) {
            printUsage();
            return;
        }

        // arg 0 is the command name
        String settingName = args[0];

        Field field = CommandValues.get(settingName);

        if (field == null) {
            print(EnumChatFormatting.RED + "Setting {} does not exist!", settingName);
            return;
        }

        Object[] values = new Object[args.length - 1];

        for (int i = 1; i < args.length; i++) {
            Result<?> parse = CommandValues.parse(field.getType(), args[i]);

            if (parse.isError()) {
                print(EnumChatFormatting.RED + "Invalid value for setting {}: {}", settingName, args[i]);
                parse.getError().printStackTrace();
                return;
            }

            values[i - 1] = parse.get();
        }

        Object nowValue = field.get(CommandValues.getValues());

        for (int i = 0; i < values.length; i++) {

            if (Objects.equals(values[i], nowValue)) {
                field.set(CommandValues.getValues(), values[(i + 1) % values.length]);
                break;
            }

            if (i == values.length - 1) {
                field.set(CommandValues.getValues(), values[0]);
            }
        }

    }
}
