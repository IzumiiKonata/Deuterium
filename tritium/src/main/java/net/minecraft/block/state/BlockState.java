package net.minecraft.block.state;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.*;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;

import java.util.*;

public class BlockState {
    private static final Function<IProperty, String> GET_NAME_FUNC = p_apply_1_ -> p_apply_1_ == null ? "<NULL>" : p_apply_1_.getName();
    @Getter
    private final Block block;
    private final ImmutableList<IProperty> properties;
    @Getter
    private final ImmutableList<IBlockState> validStates;

    public BlockState(Block blockIn, IProperty... properties) {
        this.block = blockIn;
        Arrays.sort(properties, Comparator.comparing(IProperty::getName));
        this.properties = ImmutableList.copyOf(properties);

        int totalStates = 1;
        for (IProperty prop : this.properties) {
            totalStates *= prop.getAllowedValues().size();
        }

        Map<Map<IProperty, Comparable>, BlockState.StateImplementation> map =
                new LinkedHashMap<>((int)(totalStates / 0.75f) + 1);
        List<BlockState.StateImplementation> list = new ArrayList<>(totalStates);

        List<Comparable>[] allowedValuesArray = new List[this.properties.size()];
        for (int i = 0; i < this.properties.size(); i++) {
            Collection<? extends Comparable> values = this.properties.get(i).getAllowedValues();
            allowedValuesArray[i] = values instanceof List ?
                    (List<Comparable>) values : new ArrayList<>(values);
        }

        generateStates(allowedValuesArray, 0, new Comparable[this.properties.size()],
                map, list, blockIn);

        for (BlockState.StateImplementation state : list) {
            state.buildPropertyValueTable(map);
        }

        this.validStates = ImmutableList.copyOf(list);
    }

    private void generateStates(List<Comparable>[] allowedValues, int propertyIndex, Comparable[] currentValues, Map<Map<IProperty, Comparable>, BlockState.StateImplementation> map, List<BlockState.StateImplementation> list, Block blockIn) {
        if (propertyIndex == this.properties.size()) {
            ImmutableMap.Builder<IProperty, Comparable> builder =
                    ImmutableMap.builderWithExpectedSize(this.properties.size());

            for (int i = 0; i < this.properties.size(); i++) {
                builder.put(this.properties.get(i), currentValues[i]);
            }

            ImmutableMap<IProperty, Comparable> propertyMap = builder.build();
            BlockState.StateImplementation state =
                    new BlockState.StateImplementation(blockIn, propertyMap);

            map.put(propertyMap, state);
            list.add(state);
            return;
        }

        List<Comparable> values = allowedValues[propertyIndex];
        for (Comparable value : values) {
            currentValues[propertyIndex] = value;
            generateStates(allowedValues, propertyIndex + 1, currentValues, map, list, blockIn);
        }
    }
    public IBlockState getBaseState() {
        return this.validStates.getFirst();
    }

    public Collection<IProperty> getProperties() {
        return this.properties;
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("block", Block.blockRegistry.getNameForObject(this.block)).add("properties", Iterables.transform(this.properties, GET_NAME_FUNC)).toString();
    }

    static class StateImplementation extends BlockStateBase {
        @Getter
        private final Block block;
        @Getter
        private final ImmutableMap<IProperty, Comparable> properties;
        private Table<IProperty, Comparable, IBlockState> propertyValueTable;

        private StateImplementation(Block blockIn, ImmutableMap<IProperty, Comparable> propertiesIn) {
            this.block = blockIn;
            this.properties = propertiesIn;
        }

        public Collection<IProperty> getPropertyNames() {
            return Collections.unmodifiableCollection(this.properties.keySet());
        }

        public <T extends Comparable<T>> T getValue(IProperty<T> property) {
            if (!this.properties.containsKey(property)) {
                throw new IllegalArgumentException("Cannot get property " + property + " as it does not exist in " + this.block.getBlockState());
            } else {
                return property.getValueClass().cast(this.properties.get(property));
            }
        }

        public <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value) {
            if (!this.properties.containsKey(property)) {
                throw new IllegalArgumentException("Cannot set property " + property + " as it does not exist in " + this.block.getBlockState());
            } else if (!property.getAllowedValues().contains(value)) {
                throw new IllegalArgumentException("Cannot set property " + property + " to " + value + " on block " + Block.blockRegistry.getNameForObject(this.block) + ", it is not an allowed value");
            } else {
                return this.properties.get(property) == value ? this : this.propertyValueTable.get(property, value);
            }
        }

        public int hashCode() {
            return this.properties.hashCode();
        }

        public void buildPropertyValueTable(Map<Map<IProperty, Comparable>, BlockState.StateImplementation> map) {
            if (this.propertyValueTable != null) {
                throw new IllegalStateException();
            } else {
                int expectedSize = 0;
                for (IProperty<?> prop : this.properties.keySet()) {
                    expectedSize += prop.getAllowedValues().size() - 1;
                }

                Table<IProperty, Comparable, IBlockState> table =
                        HashBasedTable.create(this.properties.size(), expectedSize);

                Map<IProperty, Comparable> tempMap = new HashMap<>(this.properties);

                for (IProperty<? extends Comparable> iproperty : this.properties.keySet()) {
                    Comparable currentValue = this.properties.get(iproperty);

                    for (Comparable comparable : iproperty.getAllowedValues()) {
                        if (comparable != currentValue) {
                            tempMap.put(iproperty, comparable);
                            table.put(iproperty, comparable, map.get(tempMap));
                        }
                    }

                    tempMap.put(iproperty, currentValue);
                }

                this.propertyValueTable = table;
            }
        }

    }
}