package tritium.command;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.EnumChatFormatting;
import tritium.command.annotations.AllowedInts;
import tritium.command.annotations.CommandDesc;
import tritium.command.annotations.NumericRange;
import tritium.management.CommandManager;
import tritium.screens.ConsoleScreen;
import tritium.settings.NumberSetting;
import tritium.utils.other.Result;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * @author IzumiiKonata
 * Date: 2025/11/22 08:56
 */
public class CommandValues {

    @Getter
    @Setter
    private static Values values = new Values();

    public static class Values {

        @CommandDesc("Swap hand side")
        @SerializedName("cl_righthand")
        public boolean cl_righthand = true;

        @CommandDesc("Viewmodel offset x")
        @SerializedName("viewmodel_offset_x")
        public double viewmodel_offset_x = 0;

        @CommandDesc("Viewmodel offset y")
        @SerializedName("viewmodel_offset_y")
        public double viewmodel_offset_y = 0;

        @CommandDesc("Viewmodel offset z")
        @SerializedName("viewmodel_offset_z")
        public double viewmodel_offset_z = 0;

    }

    public static void registerCommands() {

        for (Field f : values.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            SerializedName serializedName = f.getAnnotation(SerializedName.class);

            CommandManager.CommandRegisteredCallback cb = CommandManager.registerCommand(serializedName.value(), arg -> {
                ConsoleScreen.log(set(f, arg));
            }, f.getType(), getArgumentDescForValue(f));

            CommandDesc desc = f.getAnnotation(CommandDesc.class);

            if (desc != null)
                cb.setDescription(desc.value());
        }

    }

    private static String getArgumentDescForValue(Field field) {
        Class<?> type = field.getType();

        if (type == boolean.class) {
            return "true/false";
        } else if (type == int.class || type == long.class || type == float.class || type == double.class) {

            AllowedInts allowedInts = field.getAnnotation(AllowedInts.class);
            if (allowedInts != null) {
                String[] array = Arrays.stream(allowedInts.value()).mapToObj(String::valueOf).toArray(String[]::new);
                return String.join("/", array);
            }

            return "Number";
        } else if (type == String.class) {
            return "String";
        }

        return "Unknown";
    }

    public static String set(Field field, Object value) {
        SerializedName serializedName = field.getAnnotation(SerializedName.class);
        String name = serializedName.value();

        if (value instanceof Number) {
            NumericRange range = field.getAnnotation(NumericRange.class);

            if (range != null) {
                if (((Number) value).doubleValue() < range.min() || ((Number) value).doubleValue() > range.max()) {
                    // clamp the value
                    value = range.min() > ((Number) value).doubleValue() ? range.min() : range.max();
                    // set to field type
                    value = NumberSetting.cast((Class<? extends Number>) field.getType(), ((Number) value));
                }
            }
        }

        try {
            field.set(values, value);
            return EnumChatFormatting.GREEN + "Set " + name + " to " + value;
        } catch (IllegalAccessException e) {
            return EnumChatFormatting.RED + "Failed to set " + name + " to " + value;
        }
    }

    public static String set(String name, String rawValue) {
        Field field = null;

        for (Field f : values.getClass().getDeclaredFields()) {

            SerializedName serializedName = f.getAnnotation(SerializedName.class);

            if (serializedName.value().equalsIgnoreCase(name)) {
                field = f;
                field.setAccessible(true);
                break;
            }
        }

        if (field == null)
            return EnumChatFormatting.RED + "Command \"" + name + "\" does not exist!";

        Result<?> parse = parse(field.getType(), rawValue);

        if (parse.isError())
            return EnumChatFormatting.RED + "Invalid value for " + name + ": " + parse.getError().getMessage();

        try {
            field.set(values, parse.get());
            return EnumChatFormatting.GREEN + "Set " + name + " to " + rawValue;
        } catch (IllegalAccessException e) {
            return EnumChatFormatting.RED + "Failed to set " + name + " to " + rawValue;
        }
    }

    private static <T> Result<T> parse(Class<T> type, String rawValue) {
        return Result.from(() -> {

            if (type == String.class)
                return (T) rawValue;
            else if (type == Integer.class)
                return (T) Integer.valueOf(rawValue);
            else if (type == Long.class)
                return (T) Long.valueOf(rawValue);
            else if (type == Float.class)
                return (T) Float.valueOf(rawValue);
            else if (type == Double.class)
                return (T) Double.valueOf(rawValue);
            else if (type == Boolean.class) {
                if (rawValue.equals("1") || rawValue.equals("true"))
                    return (T) Boolean.TRUE;
                else
                    return (T) Boolean.FALSE;
            }

            throw new IllegalArgumentException("Unknown type: " + type);
        });
    }

}
