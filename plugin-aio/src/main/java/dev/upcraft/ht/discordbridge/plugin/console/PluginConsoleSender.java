package dev.upcraft.ht.discordbridge.plugin.console;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.util.MessageUtil;
import dev.upcraft.ht.discordbridge.util.Services;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class PluginConsoleSender implements CommandSender {

    private final CommandSender delegate;
    @Nullable
    private final String messageId;

    public PluginConsoleSender(CommandSender delegate, @Nullable String messageId) {
        this.delegate = delegate;
        this.messageId = messageId;
    }

    @Override
    public void sendMessage(@NonNull Message message) {
        // log to console
        delegate.sendMessage(message);

        // log to discord
        // TODO strip formatting codes?
        Services.BOT.sendCommandFeedback(MessageUtil.toAnsiString(message).toAnsi(), messageId);
    }

    @Override
    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    @Override
    public UUID getUuid() {
        return delegate.getUuid();
    }

    @Override
    public boolean hasPermission(@NonNull String s) {
        return delegate.hasPermission(s);
    }

    @Override
    public boolean hasPermission(@NonNull String s, boolean b) {
        return delegate.hasPermission(s, b);
    }
}
