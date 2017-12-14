package com.scalamc.utils;
import scala.collection.mutable.ArrayBuffer;
import scala.collection.mutable.Buffer;

import java.util.ArrayList;
import java.util.List;

public class VarIntJ {
    public static void writeVarInt(int value) {
        List<Byte> lst = new ArrayList<>();
        do {
            byte temp = (byte)(value & 0b01111111);
            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            System.out.println("res "+temp);
        } while (value != 0);
    }
}
