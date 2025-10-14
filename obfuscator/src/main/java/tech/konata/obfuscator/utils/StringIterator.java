package tech.konata.obfuscator.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class StringIterator implements Iterator<String> {

        private static final String CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        private final int length;
        private final int charsetSize;
        private final long totalCombinations;
        private long current = 0;

        public StringIterator(int length) {
            this.length = length;
            this.charsetSize = CHARSET.length();
            this.totalCombinations = (long) Math.pow(charsetSize, length);
        }

        @Override
        public boolean hasNext() {
            return current < totalCombinations;
        }

        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            StringBuilder sb = new StringBuilder();
            long num = current++;

            for (int i = 0; i < length; i++) {
                sb.append(CHARSET.charAt((int)(num % charsetSize)));
                num /= charsetSize;
            }

            return sb.reverse().toString();
        }
    }