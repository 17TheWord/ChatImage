package github.kituin.chatimage.mixin;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import github.kituin.chatimage.exception.InvalidChatImageCodeException;
import github.kituin.chatimage.tool.ChatImageCode;
import github.kituin.chatimage.tool.ChatImageStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 注入修改文本显示,自动将CICode转换为可鼠标悬浮格式文字
 *
 * @author kitUIN
 */
@Mixin(ChatComponent.class)
public class ChatComponentMixin extends GuiComponent {
    @Shadow
    @Final
    private Minecraft minecraft;
    private static Pattern pattern = Pattern.compile("(\\[\\[CICode,(.*?)\\]\\])");

    @ModifyVariable(at = @At("HEAD"),
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V",
            argsOnly = true)
    public Component addMessage(Component p_241484_) {
        return replaceMessage(p_241484_);
    }


    private Component replaceCode(Component text) {
        String checkedText = "";
        String key = "";
        MutableComponent player = null;
        boolean isSelf = false;
        boolean isIncoming = false;
        System.out.println(text);
        if (text.getContents() instanceof LiteralContents lc) {
            checkedText = lc.toString();
        } else if (text.getContents() instanceof TranslatableContents ttc) {
            key = ttc.getKey();
            if ("chat.type.text".equals(key) || "chat.type.announcement".equals(key) || "commands.message.display.incoming".equals(key) || "commands.message.display.outgoing".equals(key)) {
                Object[] args = ttc.getArgs();
                player = (MutableComponent) args[0];
                isSelf = player.getContents().toString().equals(this.minecraft.player.getName().getContents().toString());
                MutableComponent contents = (MutableComponent) args[1];
                if (contents.getContents() instanceof LiteralContents lc) {
                    checkedText = lc.text();
                } else {
                    checkedText = contents.getContents().toString();
                }
                if ("commands.message.display.incoming".equals(key) || "commands.message.display.outgoing".equals(key)) {
                    isIncoming = true;
                }
            }
        } else {
            checkedText = text.getContents().toString();
        }
        System.out.println(checkedText);
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
                LogUtils.getLogger().error(e.getMessage());
            }
        }
        if (flag) {
            return text;
        }
        int lastPosition = 0;
        int j = 0;
        MutableComponent res;
        res = Component.literal(checkedText.substring(lastPosition, nums.get(0))).setStyle(style);
        if (nums.get(0) == 0) {
            res.append(ChatImageStyle.messageFromCode(chatImageCodeList.get(0)));
            j = 2;
        }
        for (int i = j; i < nums.size(); i += 2) {
            if (i == j && j == 2) {
                res.append(Component.literal(checkedText.substring(nums.get(1), nums.get(2))));
            }
            res.append(ChatImageStyle.messageFromCode(chatImageCodeList.get(i / 2)));
            lastPosition = nums.get(i + 1);
            if (i + 2 < nums.size() && lastPosition + 1 != nums.get(i + 2)) {
                String s = checkedText.substring(lastPosition, nums.get(i + 2));
                res.append(Component.literal(s).setStyle(style));
            } else if (lastPosition == nums.get(nums.size() - 1)) {
                res.append(Component.literal(checkedText.substring(lastPosition)));
            }
        }
        if (player == null) {
            return res;
        } else {
            MutableComponent resp = MutableComponent.create(new TranslatableContents(key, player, res));
            if (isIncoming) {
                return resp.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true));
            } else {
                return resp;
            }
        }
    }

    private Component replaceMessage(Component message) {
        MutableComponent res = (MutableComponent) replaceCode(message);
        for (Component t : message.getSiblings()) {
            res.append(replaceMessage(t));
        }
        return res;
    }
}

