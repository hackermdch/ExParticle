package net.hackermdch.exparticle.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.io.PrintStream;

public class ClientMessageUtil {
    private static final Minecraft CLIENT = Minecraft.getInstance();

    public static void addChatMessage(Throwable e) {
        e.printStackTrace(new PrintStream(System.out) {
            public void println(Object x) {
                CLIENT.gui.getChat().addMessage(Component.literal(x.toString()));
            }
        });
    }

    public static void addChatMessage(Component message) {
        CLIENT.gui.getChat().addMessage(message);
    }
}
