package github.kituin.chatimage.tool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.kituin.ChatImageCode.ChatImageCode;
import io.github.kituin.ChatImageCode.ChatImageCodeInstance;
import io.github.kituin.ChatImageCode.exception.InvalidChatImageCodeException;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.JsonHelper;


/**
 * @author kitUIN
 */
public class ChatImageStyle {
    public static final HoverEvent.Action<ChatImageCode> SHOW_IMAGE =
            new HoverEvent.Action<>("show_chatimage", true,
                    ChatImageCode::fromJson,
                    ChatImageCode::toJson,
                    ChatImageStyle::fromJson);

    /**
     * 文本 悬浮图片样式
     *
     * @param code {@link ChatImageCode}
     * @return 悬浮图片样式
     */
    public static Style getStyleFromCode(ChatImageCode code) {
        return getStyleFromCode(code, Formatting.GREEN);
    }

    /**
     * 文本 悬浮图片样式
     *
     * @param code {@link ChatImageCode}
     * @param color 颜色
     * @return 悬浮图片样式
     */
    public static Style getStyleFromCode(ChatImageCode code, Formatting color) {
        Style style = Style.EMPTY.withHoverEvent(new HoverEvent(SHOW_IMAGE, code));
        return style.withColor(color);
    }
    /**
     * 获取悬浮图片样式的Text消息
     *
     * @param code {@link ChatImageCode}
     * @return {@link MutableText}
     */
    public static MutableText messageFromCode(ChatImageCode code) {
        MutableText t = code.messageFromCode(
                LiteralText::new,
                TranslatableText::new,
                (obj, s)-> (BaseText) obj.append(s));
        Style style = ChatImageStyle.getStyleFromCode(code);
        return t.fillStyle(style);
    }



    public static ChatImageCode fromJson(Text text) {
        try {
            return ChatImageCodeInstance.createBuilder().fromCode(text.toString()).build();
        } catch (InvalidChatImageCodeException e) {
            return ChatImageCodeInstance.createBuilder().build();
        }
    }
}
