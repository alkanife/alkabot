package dev.alkanife.alkabot.configuration.json;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WelcomeMessageConfig {

    private boolean enable;
    @SerializedName("ignore_bots")
    private boolean ignoreBots;
    @SerializedName("channel_id")
    private String channelId;
}
