package tech.konata.obfuscator.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author IzumiiKonata
 * Date: 2025/9/5 21:07
 */
public class SequencedNameGenerator {

    final String chars = "abcdefghijklmnopqrstuvwxyz";
    final char[] arrayedChars = chars.toCharArray();

    char[] charArray = {};

    public void clear() {
        this.charArray = new char[]{};
    }

    public String nextSequencedString() {

        int length = this.charArray.length;
        if (length == 0) {
            this.growArrayAndClearAll();
            return this.arrayToString();
        }

        for (int i = length - 1; i >= 0; i--) {

            this.charArray[i] ++;

            if (this.charArray[i] > this.arrayedChars[this.arrayedChars.length - 1]) {

                // set to the first char in the array
                this.charArray[i] = this.arrayedChars[0];

                // last
                if (i == 0) {
                    this.growArrayAndClearAll();
                }

            } else {
                break;
            }

        }

        return this.arrayToString();
    }

    private void growArrayAndClearAll() {
        this.charArray = Arrays.copyOf(this.charArray, this.charArray.length + 1);

        // set all to the first char in the chars array
        Arrays.fill(this.charArray, this.arrayedChars[0]);
    }

    private String arrayToString() {
        return new String(this.charArray);
    }

}
