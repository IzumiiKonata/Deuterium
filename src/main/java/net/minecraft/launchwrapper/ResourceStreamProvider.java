package net.minecraft.launchwrapper;

import java.io.InputStream;

/**
 * @author IzumiiKonata
 * Date: 2026/1/24 21:31
 */
@FunctionalInterface
public interface ResourceStreamProvider {

    InputStream makeNew();

}
