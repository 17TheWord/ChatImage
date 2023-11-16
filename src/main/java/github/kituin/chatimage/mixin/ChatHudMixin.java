package github.kituin.chatimage.mixin;


import com.google.common.collect.Lists;
import io.github.kituin.ChatImageCode.exception.InvalidChatImageCodeException;
import io.github.kituin.ChatImageCode.ChatImageCode;
import github.kituin.chatimage.tool.ChatImageStyle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static github.kituin.chatimage.client.ChatImageClient.CONFIG;


/**
 * 注入修改文本显示,自动将CICode转换为可鼠标悬浮格式文字
 *
 * @author kitUIN
 */
@Mixin(ChatHud.class)
public class ChatHudMixin extends DrawableHelper {
    @Shadow
    @Final
    private static Logger LOGGER;
    private static Pattern pattern = Pattern.compile("(\\[\\[CICode,(.*?)\\]\\])");
    private static final Pattern cqPattern = Pattern.compile("\\[CQ:image,(.*?)\\]");

    @ModifyVariable(at = @At("HEAD"),
            method = "addMessage(Lnet/minecraft/text/Text;IIZ)V",
            argsOnly = true)
    public Text addMessage(Text message) {
        return replaceMessage(message);
    }


    private static Text replaceCode(Text text) {
        String checkedText;
        String key = "";
        MutableText player = null;
        boolean isSelf = false;
        boolean isIncoming = false;
        if (text instanceof TranslatableText ttc) {
            key = ttc.getKey();
            Object[] args = ttc.getArgs();
            if ("chat.type.text".equals(key) || "chat.type.announcement".equals(key) ||
                    "commands.message.display.incoming".equals(key) || "commands.message.display.outgoing".equals(key)) {
                player = (LiteralText) args[0];
                isSelf = player.asString().equals(MinecraftClient.getInstance().player.getName().asString());
                if ("commands.message.display.incoming".equals(key) || "commands.message.display.outgoing".equals(key)) {
                    isIncoming = true;
                }
            }
            if (args[1] instanceof String content) {
                checkedText = content;
            } else {
                MutableText contents = (MutableText) args[1];
                checkedText = contents.asString();
            }
        } else {
            checkedText = text.asString();
        }

        if(CONFIG.cqCode){
            Matcher cqm = cqPattern.matcher(checkedText);
            while (cqm.find()) {
                String[] cqArgs = cqm.group(1).split(",");
                String cq_Url = "";
                for(int i=0;i<cqArgs.length;i++){
                    String[] cqParams = cqArgs[i].split("=");
                    if("url".equals(cqParams[0])){
                        cq_Url = cqParams[1];
                        break;
                    }
                }
                if(!cq_Url.isEmpty()){
                    checkedText = checkedText.replace(cqm.group(0), String.format("[[CICode,url=%s]]", cq_Url));
                }
            }
        }
        Style style = text.getStyle();
        List<ChatImageCode> chatImageCodeList = Lists.newArrayList();
        Matcher m = pattern.matcher(checkedText);
        List<Integer> nums = Lists.newArrayList();
        boolean flag = true;
        while (m.find()) {
            try {
                ChatImageCode image = ChatImageCode.of(m.group(), isSelf);
                flag = false;
                nums.add(m.start());
                nums.add(m.end());
                chatImageCodeList.add(image);
            } catch (InvalidChatImageCodeException e) {
                LOGGER.error(e.getMessage());
            }
        }
        if (flag) {
            return text;
        }
        int lastPosition = 0;
        int j = 0;
        MutableText res;
        if (nums.get(0) != 0) {
            res = ((MutableText) Text.of(checkedText.substring(lastPosition, nums.get(0)))).setStyle(style);
        } else {
            res = ((MutableText) Text.of(checkedText.substring(lastPosition, nums.get(0)))).setStyle(style);
            res.append(ChatImageStyle.messageFromCode(chatImageCodeList.get(0)));
            j = 2;
        }
        for (int i = j; i < nums.size(); i += 2) {
            if (i == j && j == 2) {
                res.append(Text.of(checkedText.substring(nums.get(1), nums.get(2))));
            }
            res.append(ChatImageStyle.messageFromCode(chatImageCodeList.get(i / 2)));
            lastPosition = nums.get(i + 1);
            if (i + 2 < nums.size() && lastPosition + 1 != nums.get(i + 2)) {
                String s = checkedText.substring(lastPosition, nums.get(i + 2));
                res.append(((MutableText) Text.of(s)).setStyle(style));
            } else if (lastPosition == nums.get(nums.size() - 1)) {
                res.append(Text.of(checkedText.substring(lastPosition)));
            }
        }
        if (player != null) {
            TranslatableText resp = new TranslatableText(key, player, res);
            if (isIncoming) {
                return resp.setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(true));
            }
            return resp;
        } else {
            return res;
        }
    }

    private static Text replaceMessage(Text message) {
        try{
            MutableText res = (MutableText) replaceCode(message);
            for (Text t : message.getSiblings()) {
                res.append(replaceMessage(t));
            }
            return res;
        }
        catch (Exception e){
            return message;
        }
    }
}

