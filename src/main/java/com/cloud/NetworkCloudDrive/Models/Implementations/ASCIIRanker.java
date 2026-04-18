package com.cloud.NetworkCloudDrive.Models.Implementations;

public class ASCIIRanker {

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
