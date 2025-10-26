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

package tech.konata.obfuscator.utils;

import tech.konata.obfuscator.Main;
import tech.konata.obfuscator.asm.ClassWrapper;
import tech.konata.obfuscator.asm.MethodWrapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utilities for strings. Primarily used for string generation.
 *
 * @author ItzSomebody
 */
public class StringUtils {
    private final static char[] ALPHA_NUM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private final static char[] ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    public static String randomSpacesString(int length) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++)
            sb.append((char) (RandomUtils.getRandomIntNoOrigin(16) + '\u2000'));

        return sb.toString();
    }

    public static String randomUnrecognizedString(int length) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++)
            sb.append((char) (RandomUtils.getRandomIntNoOrigin(8) + 'ꚬ'));

        return sb.toString();
    }

    public static String randomAlphaString(int length) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++)
            sb.append(ALPHA[RandomUtils.getRandomIntNoOrigin(ALPHA.length)]);

        return sb.toString();
    }

    public static String randomAlphaNumericString(int length) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++)
            sb.append(ALPHA_NUM[RandomUtils.getRandomIntNoOrigin(ALPHA_NUM.length)]);

        return sb.toString();
    }

    public static String randomClassName(Map<String, ClassWrapper> classNames) {
        ArrayList<String> list = new ArrayList<>(classNames.keySet());
        String chosen = list.get(RandomUtils.getRandomIntNoOrigin(classNames.size())) + randomAlphaString(1);

        int depth = 1;
        int maxRetryTimes = ALPHA.length;
        int retriedTimes = 0;
        while (list.contains(chosen)) {
            chosen = list.get(RandomUtils.getRandomIntNoOrigin(classNames.size())) + randomAlphaString(depth);
            retriedTimes++;

            if (retriedTimes >= maxRetryTimes) {
                depth ++;
                retriedTimes = 0;
                maxRetryTimes = (int) Math.pow(ALPHA.length, depth);
            }
        }

        return chosen;
    }

    private static String reOrder(String input) {
        char[] arr = input.toCharArray();

        List<Integer> idx = new ArrayList<>();

        for (int i = 0; i < arr.length; i++) {
            idx.add(i);
        }

        Collections.shuffle(idx);

        StringBuilder sb = new StringBuilder();
        for (int i : idx) {
            sb.append(arr[i]);
        }

        return sb.toString();
    }

    public static String randomClassNameFromBase(Collection<String> classNames) {
        ArrayList<String> list = classNames.stream().filter(cp -> cp.startsWith(Main.REPACKAGE_NAME)).collect(Collectors.toCollection(ArrayList::new));

        String first = list.get(RandomUtils.getRandomIntNoOrigin(list.size()));
        first = first.substring(first.indexOf("/") + 1);

        String s = reOrder(first);
//        System.out.println(first + " => " + s);

        String packageName = "";

        for (String string : list) {
            if (string.startsWith(Main.REPACKAGE_NAME)) {
                packageName = string.substring(0, string.indexOf("/") + 1);
            }
        }

        while (list.contains(packageName + s)) {
//            System.out.println("!! CONTAINS !!");
            first = list.get(RandomUtils.getRandomIntNoOrigin(list.size()));
            first = first.substring(first.indexOf("/") + 1);
            s = reOrder(first);
//            System.out.println(first + " => " + s);
        }

        return packageName + s;
    }
}
