package tritium.settings;

import com.google.common.primitives.Primitives;
import lombok.Getter;
import tritium.bridge.settings.NumberValueWrapper;

import java.text.DecimalFormat;
import java.util.function.Supplier;

public class NumberSetting<T extends Number> extends Setting<T> {

    @Getter
    public final T minimum, maximum, increment;
    private T lastValue;

    public double nowWidth = 0;

    public NumberSetting(String name, T value, T minimum, T maximum, T increment) {
        super(name, value);
        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = increment;
        this.lastValue = value;
    }

    public NumberSetting(String name, T value, T minimum, T maximum, T increment, Supplier<Boolean> show) {
        super(name, value, show);
        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = increment;
        this.lastValue = value;
    }

    @Override
    protected void createValueWrapper() {
        this.wrapper = new NumberValueWrapper(this);
    }

    public float getFloatValue() {
        return this.getValue().floatValue();
    }

    public int getIntValue() {
        return this.getValue().intValue();
    }

    public long getLongValue() {
        return this.getValue().longValue();
    }

    public T getValue() {
        return this.value;
    }

    public DecimalFormat df = new DecimalFormat("#.##");

    public String getStringForRender() {
        return df.format(this.getValue());
    }

    @Override
    public void setValue(T value) {
        double precision = 1 / increment.doubleValue();
        super.setValue(cast((Class<T>) this.value.getClass(), /*Math.round( * precision) / precision*/value.doubleValue()));
        if (!this.lastValue.equals(this.value)) {
            this.onValueChanged(lastValue, this.value);
        }
        this.lastValue = this.value;
    }

    public void onValueChanged(T last, T now) {
    }


    @Override
    public void loadValue(String value) {
        T parse = parse(value, (Class<T>) this.value.getClass());
        double precision = 1 / increment.doubleValue();
        this.setValue(cast((Class<T>) this.value.getClass(), Math.round(parse.doubleValue() * precision) / precision));
    }

    public static <A extends Number, V extends Number> A cast(Class<A> numberClass, final V value) {
        numberClass = Primitives.wrap(numberClass);
        Object casted;
        if (numberClass == Byte.class) {
            casted = value.byteValue();
        } else if (numberClass == Short.class) {
            casted = value.shortValue();
        } else if (numberClass == Integer.class) {
            casted = value.intValue();
        } else if (numberClass == Long.class) {
            casted = value.longValue();
        } else if (numberClass == Float.class) {
            casted = value.floatValue();
        } else {
            if (numberClass != Double.class) {
                throw new ClassCastException(String.format("%s cannot be casted to %s", value.getClass(), numberClass));
            }
            casted = value.doubleValue();
        }
        return (A) casted;
    }

    public <A extends Number> A parse(final String input, final Class<A> numberType) throws NumberFormatException {
        return cast(numberType, Double.parseDouble(input));
    }

    public T getStep() {
        return this.increment;
    }

}
