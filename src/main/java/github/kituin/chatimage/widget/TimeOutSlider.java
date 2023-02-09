package github.kituin.chatimage.widget;

import github.kituin.chatimage.config.ChatImageConfig;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import static github.kituin.chatimage.client.ChatImageClient.CONFIG;

public class TimeOutSlider extends SettingSliderWidget {


    public TimeOutSlider(int x, int y, int width, int height, TooltipSupplier tooltipSupplier) {
        super(x, y, width, height, CONFIG.timeout, 3, 60, tooltipSupplier);
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(composeGenericOptionText(new TranslatableText("timeout.chatimage.gui"), new LiteralText(String.valueOf(this.position))).append(" ").append(new TranslatableText("seconds.chatimage.gui")));
        CONFIG.timeout = this.position;
        ChatImageConfig.saveConfig(CONFIG);
    }
}
