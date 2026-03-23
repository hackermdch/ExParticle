package net.hackermdch.exparticle.util;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class TextUtil {
    private static final ByteBufferBuilder buffer = new ByteBufferBuilder(0x200000);

    public static NativeImage toImage(Component text, float scale) {
        var font = Minecraft.getInstance().font;
        var width = (int) (font.width(text) * scale);
        var height = (int) (font.lineHeight * scale);
        if (scale == 0) return new NativeImage(NativeImage.Format.RGBA, 1, 1, false);
        var rt = new TextureTarget(width, height, false, false);
        var img = new NativeImage(NativeImage.Format.RGBA, width, height, false);
        var buff = ByteBuffer.allocateDirect(width * height * 4);
        var bufferSource = MultiBufferSource.immediate(buffer);
        var pm = RenderSystem.getProjectionMatrix();
        var vs = RenderSystem.getVertexSorting();
        rt.setClearColor(0, 0, 0, 0);
        rt.clear(false);
        rt.bindWrite(true);
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0, width, height, 0, 0, 1000), VertexSorting.ORTHOGRAPHIC_Z);
        font.drawInBatch(text, 0, 0, -1, false, new Matrix4f().scale(scale, scale, 1), bufferSource, Font.DisplayMode.NORMAL, 0, 0xf000f0);
        bufferSource.endBatch();
        RenderSystem.setProjectionMatrix(pm, vs);
        GL32.glPixelStorei(GL32.GL_PACK_ALIGNMENT, 1);
        GL32.glReadPixels(0, 0, width, height, GL32.GL_RGBA, GL32.GL_UNSIGNED_BYTE, buff);
        MemoryUtil.memCopy(MemoryUtil.memAddress(buff), img.pixels, buff.capacity());
        rt.destroyBuffers();
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
        return img;
    }
}
