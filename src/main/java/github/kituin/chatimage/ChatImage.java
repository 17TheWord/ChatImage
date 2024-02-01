package github.kituin.chatimage;


import github.kituin.chatimage.integration.ChatImageLogger;
import github.kituin.chatimage.network.ChatImagePacket;
import io.github.kituin.ChatImageCode.ChatImageCodeInstance;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;


/**
 * @author kitUIN
 */
public class ChatImage implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    static{
        ChatImageCodeInstance.LOGGER = new ChatImageLogger();
    }
    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(ChatImagePacket.FILE_CHANNEL, (server, player, handler, buf, responseSender) -> ChatImagePacket.serverFileChannelReceived(server, buf));
        ServerPlayNetworking.registerGlobalReceiver(ChatImagePacket.GET_FILE_CHANNEL, (server, player, handler, buf, responseSender) -> ChatImagePacket.serverGetFileChannelReceived(player, buf));
    }
}
