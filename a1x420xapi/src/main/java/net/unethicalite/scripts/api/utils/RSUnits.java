package net.unethicalite.scripts.api.utils;

import java.text.NumberFormat;

public class RSUnits {
    public static String convertToRSUnits(int number) {
        String postfix = "";
        int divisor = 1;
        if (number >= 1_000_000_000) {
            divisor = 1_000_000_000;
            postfix = "B";
        } else if (number >= 10_000_000) {
            divisor = 1_000_000;
            postfix = "M";
        } else if (number >= 10_000) {
            divisor = 1_000;
            postfix = "K";
        }

        int formattedNumber = (int) Math.floor((double) number / divisor);

        // Using the NumberFormat class to add commas
        NumberFormat nf = NumberFormat.getInstance();
        return nf.format(formattedNumber) + postfix;
    }
}
