/*
 * Copyright (C) 2018 ItzSomebody
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package tech.konata.obfuscator.transformers.shrinkers;

/**
 * Enables and runs the shrinking transformers.
 *
 * @author ItzSomebody
 */
public class ShrinkerDelegator extends Shrinker {
    private ShrinkerSetup setup;

    public ShrinkerDelegator(ShrinkerSetup setup) {
        this.setup = setup;
    }

    @Override
    public void transform() {
        if (this.setup.isRemoveVisibleAnnotations()) {
            VisibleAnnotationsRemover visibleAnnotationsRemover = new VisibleAnnotationsRemover();
            visibleAnnotationsRemover.init(this.obfuscator);
            visibleAnnotationsRemover.transform();
        }
        if (this.setup.isRemoveInvisibleAnnotations()) {
            InvisibleAnnotationsRemover invisibleAnnotationsRemover = new InvisibleAnnotationsRemover();
            invisibleAnnotationsRemover.init(this.obfuscator);
            invisibleAnnotationsRemover.transform();
        }
        if (this.setup.isRemoveAttributes()) {
            AttributesRemover attributesRemover = new AttributesRemover();
            attributesRemover.init(this.obfuscator);
            attributesRemover.transform();
        }
        if (this.setup.isRemoveDebug()) {
            DebugInfoRemover debugInfoRemover = new DebugInfoRemover();
            debugInfoRemover.init(this.obfuscator);
            debugInfoRemover.transform();
        }
    }

    @Override
    public String getName() {
        return "Shrinker";
    }

    public ShrinkerSetup getSetup() {
        return setup;
    }
}
