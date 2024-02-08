package github.kituin.chatimage.integration;

import io.github.kituin.ChatImageCode.ChatImageFrame;
import io.github.kituin.ChatImageCode.IClientAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static github.kituin.chatimage.ChatImage.CONFIG;
import static github.kituin.chatimage.ChatImage.MOD_ID;
import static github.kituin.chatimage.network.ChatImagePacket.loadFromServer;
import static github.kituin.chatimage.network.ChatImagePacket.sendFilePackets;
import static io.github.kituin.ChatImageCode.NetworkHelper.createFilePacket;

public class ChatImageClientAdapter implements IClientAdapter {
    @Override
    public int getTimeOut() {
        return CONFIG.timeout;
    }

    @Override
    public ChatImageFrame.TextureReader<ResourceLocation> loadTexture(InputStream image) throws IOException {
        NativeImage nativeImage = NativeImage.read(image);
        return new ChatImageFrame.TextureReader<>(
                Minecraft.getInstance().getTextureManager()
                        .register(MOD_ID + "/chatimage",new DynamicTexture(nativeImage)),
                nativeImage.getWidth(),
                nativeImage.getHeight()
        );
    }

    @Override
    public void sendToServer(String url, File file, boolean isToServer) {
        if (isToServer) {
            List<String> bufs = createFilePacket(url, file);
            sendFilePackets(bufs);
        } else {
            loadFromServer(url);
        }
    }

    @Override
    public void checkCachePath() {
        File folder = new File(CONFIG.cachePath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    @Override
    public int getMaxFileSize() {
        return CONFIG.MaxFileSize;
    }

    @Override
    public ITextComponent getProcessMessage(int i) {
        return new TranslationTextComponent("process.chatimage.message", i);
    }


}
