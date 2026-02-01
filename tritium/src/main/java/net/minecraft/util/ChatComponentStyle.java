package net.minecraft.util;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

public abstract class ChatComponentStyle implements IChatComponent {
    protected List<IChatComponent> siblings = Lists.newArrayList();
    private ChatStyle style;

    /**
     * Appends the given component to the end of this one.
     */
    public IChatComponent appendSibling(IChatComponent component) {
        component.getChatStyle().setParentStyle(this.getChatStyle());
        this.siblings.add(component);
        return this;
    }

    public List<IChatComponent> getSiblings() {
        return this.siblings;
    }

    /**
     * Appends the given text to the end of this component.
     */
    public IChatComponent appendText(String text) {
        return this.appendSibling(new ChatComponentText(text));
    }

    public IChatComponent setChatStyle(ChatStyle style) {
        this.style = style;

        for (IChatComponent ichatcomponent : this.siblings) {
            ichatcomponent.getChatStyle().setParentStyle(this.getChatStyle());
        }

        return this;
    }

    public ChatStyle getChatStyle() {
        if (this.style == null) {
            this.style = new ChatStyle();

            for (IChatComponent ichatcomponent : this.siblings) {
                ichatcomponent.getChatStyle().setParentStyle(this.style);
            }
        }

        return this.style;
    }

    public Iterator<IChatComponent> iterator() {
        return Iterators.concat(Iterators.<IChatComponent>forArray(this), createDeepCopyIterator(this.siblings));
    }

    /**
     * Get the text of this component, <em>and all child components</em>, with all special formatting codes removed.
     */
    public final String getUnformattedText() {
        StringBuilder stringbuilder = new StringBuilder();

        for (IChatComponent ichatcomponent : this) {
            stringbuilder.append(ichatcomponent.getUnformattedTextForChat());
        }

        return stringbuilder.toString();
    }

    LazyLoadBase<String> formattedTextCache = LazyLoadBase.of(() -> {
        StringBuilder stringbuilder = new StringBuilder();

        for (IChatComponent ichatcomponent : this) {
            stringbuilder.append(ichatcomponent.getChatStyle().getFormattingCode());
            stringbuilder.append(ichatcomponent.getUnformattedTextForChat());
            stringbuilder.append(EnumChatFormatting.RESET);
        }

        return stringbuilder.toString();
    });

    /**
     * Gets the text of this component, with formatting codes added for rendering.
     */
    public final String getFormattedText() {
        return formattedTextCache.getValue();
    }

    public static Iterator<IChatComponent> createDeepCopyIterator(Iterable<IChatComponent> components) {
        return new DeepCopyIterator(components);
    }

    /**
     * Memory-optimized iterator that performs deep copying lazily
     */
    private static class DeepCopyIterator implements Iterator<IChatComponent> {
        private final Iterator<IChatComponent> componentsIterator;
        private Iterator<IChatComponent> currentComponentIterator;
        private IChatComponent nextComponent;

        public DeepCopyIterator(Iterable<IChatComponent> components) {
            this.componentsIterator = components.iterator();
            this.currentComponentIterator = null;
            this.nextComponent = null;
            advance();
        }

        private void advance() {
            while (true) {
                if (currentComponentIterator != null && currentComponentIterator.hasNext()) {
                    // Get next component from current iterator and create deep copy
                    IChatComponent original = currentComponentIterator.next();
                    IChatComponent copy = original.createCopy();
                    copy.setChatStyle(copy.getChatStyle().createDeepCopy());
                    nextComponent = copy;
                    return;
                }

                if (!componentsIterator.hasNext()) {
                    nextComponent = null;
                    return;
                }

                // Move to next component's iterator
                IChatComponent nextOriginal = componentsIterator.next();
                currentComponentIterator = nextOriginal.iterator();
            }
        }

        @Override
        public boolean hasNext() {
            return nextComponent != null;
        }

        @Override
        public IChatComponent next() {
            if (nextComponent == null) {
                throw new java.util.NoSuchElementException();
            }
            IChatComponent result = nextComponent;
            advance();
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (!(p_equals_1_ instanceof ChatComponentStyle chatcomponentstyle)) {
            return false;
        } else {
            return this.siblings.equals(chatcomponentstyle.siblings) && this.getChatStyle().equals(chatcomponentstyle.getChatStyle());
        }
    }

    public int hashCode() {
        return 31 * this.style.hashCode() + this.siblings.hashCode();
    }

    public String toString() {
        return "BaseComponent{style=" + this.style + ", siblings=" + this.siblings + '}';
    }
}
