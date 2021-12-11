package de.maxhenkel.persistentchat.mixin;

import de.maxhenkel.persistentchat.PersistentChat;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {

    @Shadow
    @Final
    private List<String> recentChat;
    @Shadow
    @Final
    private List<GuiMessage<Component>> allMessages;
    @Shadow
    @Final
    private List<GuiMessage<FormattedCharSequence>> trimmedMessages;

    @Inject(method = "<init>", at = @At("RETURN"))
    protected void init(Minecraft minecraft, CallbackInfo ci) {
        load();
    }

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;I)V", at = @At("RETURN"))
    protected void addMessage(Component message, int i, CallbackInfo ci) {
        save();
    }

    @Inject(method = "addRecentChat", at = @At("RETURN"))
    protected void addMessage(String msg, CallbackInfo ci) {
        save();
    }

    @Inject(method = "removeById", at = @At("RETURN"))
    protected void removeById(int i, CallbackInfo ci) {
        save();
    }

    private void save() {
        //TODO only save chat every second
        PersistentChat.LOGGER.info("Saving chat history");

        CompoundTag data = new CompoundTag();
        data.putInt("version", 0);

        ListTag history = new ListTag();
        for (String s : recentChat) {
            history.add(StringTag.valueOf(s));
        }
        data.put("recentChat", history);

        ListTag messages = new ListTag();
        for (GuiMessage<Component> c : allMessages) {
            CompoundTag message = new CompoundTag();
            message.putInt("addedTime", c.getAddedTime());
            message.putString("message", Component.Serializer.toJson(c.getMessage()));
            message.putInt("id", c.getId());
            messages.add(message);
        }
        data.put("allMessages", messages);


        try {
            NbtIo.writeCompressed(data, getSaveLocation());
        } catch (IOException e) {
            PersistentChat.LOGGER.warn("Failed to save chat history: {}", e.getMessage());
        }
    }

    private void load() {
        PersistentChat.LOGGER.info("Loading chat history");
        File file = getSaveLocation();
        if (!file.exists()) {
            return;
        }
        CompoundTag data;
        try {
            data = NbtIo.readCompressed(file);
        } catch (IOException e) {
            PersistentChat.LOGGER.warn("Couldn't load chat history: {}", e.getMessage());
            return;
        }
        if (data == null) {
            PersistentChat.LOGGER.warn("Couldn't load chat history");
            return;
        }

        if (data.getInt("version") != 0) {
            PersistentChat.LOGGER.warn("Incompatible chat history data");
            return;
        }

        ListTag history = data.getList("recentChat", Tag.TAG_STRING);
        recentChat.clear();
        for (int i = 0; i < history.size(); i++) {
            recentChat.add(history.getString(i));
        }

        ListTag messages = data.getList("allMessages", Tag.TAG_COMPOUND);
        allMessages.clear();
        trimmedMessages.clear();
        for (int i = 0; i < messages.size(); i++) {
            CompoundTag msg = messages.getCompound(i);
            int addedTime = -1000; // msg.getInt("addedTime");
            int id = msg.getInt("id");
            MutableComponent component = Component.Serializer.fromJson(msg.getString("message"));
            if (component != null) {
                allMessages.add(new GuiMessage<>(addedTime, component, id));
                trimmedMessages.add(new GuiMessage<>(addedTime, component.getVisualOrderText(), id));
            }
        }
    }

    private File getSaveLocation() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(PersistentChat.MODID).resolve("chat_data.nbt").toFile();
    }

}
