package de.maxhenkel.persistentchat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class PersistentChat implements ClientModInitializer {

    public static final String MODID = "persistentchat";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
//    public static ClientConfig CLIENT_CONFIG;

    @Override
    public void onInitializeClient() {
//        CLIENT_CONFIG = ConfigBuilder.build(Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(MODID).resolve(MODID + ".properties"), ClientConfig::new);
    }
}
