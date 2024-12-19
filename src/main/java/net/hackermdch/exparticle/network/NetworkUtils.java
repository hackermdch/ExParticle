package net.hackermdch.exparticle.network;

import net.minecraft.network.FriendlyByteBuf;

public class NetworkUtils {
    static boolean validString(String str) {
        return str != null && !str.isEmpty() && !str.equals("null");
    }

    static int readInt(FriendlyByteBuf buf, boolean read, int defValue) {
        return read ? buf.readInt() : defValue;
    }

    static float readFloat(FriendlyByteBuf buf, boolean read, float defValue) {
        return read ? buf.readFloat() : defValue;
    }

    static double readDouble(FriendlyByteBuf buf, boolean read, double defValue) {
        return read ? buf.readDouble() : defValue;
    }

    static String readString(FriendlyByteBuf buf, boolean read, String defValue) {
        return read ? buf.readUtf() : defValue;
    }
}
