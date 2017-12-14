package com.scalamc.utils;

public class Utils {
    public static byte writeVarInt(int value) {
        do {
            byte temp = (byte)(value & 0b01111111);
            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            return temp;
        } while (value != 0);
    }
}
