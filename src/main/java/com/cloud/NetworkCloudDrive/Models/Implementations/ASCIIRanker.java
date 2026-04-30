package com.cloud.NetworkCloudDrive.Models.Implementations;

import java.util.ArrayList;
import java.util.List;

public class ASCIIRanker {

    public int compareByGroups(String firstItem, String secondItem) {
        boolean secondIsBigger = false;
        int length;
        if (firstItem.length() > secondItem.length()) {
            length = secondItem.length();
        } else {
            secondIsBigger = true;
            length = firstItem.length();
        }

        List<Integer> digits = new ArrayList<>();
        List<String> characters = new ArrayList<>();

        for (int i = 0; i < length; i++) {
        /*
        file1.txt !^
        fil2.txt <-- *** ^^^

        fil2.txt
        file1.txt

        file1.txt
        file2.txt
        file12.txt
         */
            char firstStringChar = firstItem.charAt(i);
            char secondStringChar = secondItem.charAt(i);
            if (Character.isDigit(firstStringChar)) {

            }
        }
        return 1;
    }

    /*
    should only be used when all characters in comparing strings are same
    then can be used to rank them between each other using ASCII scores
     */
    public int calculateRank(String word) {
        int rank = 0;
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            rank += ch;
        }
        return rank;
    }
}
