package dev.upcraft.ht.discordbridge.plugin.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.upcraft.ht.discordbridge.discord.util.ChannelsConfig;
import dev.upcraft.ht.discordbridge.discord.util.RolesConfig;
import dev.upcraft.ht.discordbridge.discord.util.StartupConfig;

public class BotConfigCodecs {

    public static final BuilderCodec<RolesConfig> ROLES_CODEC = BuilderCodec.builder(RolesConfig.class, RolesConfig::empty)
            .append(new KeyedCodec<>("ServerOperators", Codec.STRING_ARRAY),
                    RolesConfig::setServerOperatorsArray,
                    RolesConfig::getServerOperatorsArray
            )
            .add()
            .build();

    public static final BuilderCodec<ChannelsConfig> CHANNELS_CODEC = BuilderCodec.builder(ChannelsConfig.class, ChannelsConfig::empty)
            .append(new KeyedCodec<>("DefaultChannelId", Codec.STRING, true),
                    ChannelsConfig::setDefaultChannelString,
                    ChannelsConfig::getDefaultChannelString
            )
            .add()
            .build();

    public static final BuilderCodec<StartupConfig> STARTUP_CODEC = BuilderCodec.builder(StartupConfig.class, StartupConfig::empty)
            .append(new KeyedCodec<>("DiscordToken", Codec.STRING, true),
                    StartupConfig::setToken,
                    StartupConfig::getToken
                    )
            .add()
            .append(new KeyedCodec<>("DevGuild", Codec.STRING),
                    StartupConfig::setDevGuildString,
                    StartupConfig::getDevGuildString
                    )
            .add()
            .append(new KeyedCodec<>("Roles", ROLES_CODEC, true),
                    StartupConfig::setRoles,
                    StartupConfig::getRoles
            )
            .add()
            .append(new KeyedCodec<>("Channels", CHANNELS_CODEC, true),
                    StartupConfig::setChannels,
                    StartupConfig::getChannels
            )
            .add()
            .build();
}
