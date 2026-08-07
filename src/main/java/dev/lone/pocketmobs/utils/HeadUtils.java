package dev.lone.pocketmobs.utils;

import dev.lone.pocketmobs.Main;
import dev.lone.pocketmobs.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HeadUtils
{
    private static final String TEXTURE_HOST = "https://textures.minecraft.net/texture/";
    private static final Map<String, String> TEXTURE_ID_CACHE = new ConcurrentHashMap<>();

    public static ItemStack setSkullOwner(ItemStack item, UUID uuid, String texture)
    {
        if (item.getType() != Material.PLAYER_HEAD)
        {
            item.setType(Material.PLAYER_HEAD);
        }
        
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null)
        {
            org.bukkit.profile.PlayerProfile profile = Bukkit.createPlayerProfile(uuid, "PocketMobs");
            applyTexture(profile.getTextures(), texture);
            meta.setOwnerProfile(profile);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static void applyTexture(PlayerTextures textures, String texture)
    {
        try
        {
            String textureId = extractTextureId(texture);
            if (textureId.isEmpty())
            {
                return;
            }
            textures.setSkin(new URL(TEXTURE_HOST + textureId));
        }
        catch (Exception e)
        {
            Main.inst.getLogger().warning("Failed to apply skull texture: " + e.getMessage());
        }
    }

    public static String normalizeTextureReference(String texture)
    {
        String textureId = extractTextureId(texture);
        return textureId.isEmpty() ? "" : textureId;
    }

    private static String extractTextureId(String texture)
    {
        if (texture == null || texture.isEmpty())
        {
            return "";
        }

        String cached = TEXTURE_ID_CACHE.get(texture);
        if (cached != null)
        {
            return cached;
        }

        String normalized = texture.trim();
        String textureId = normalized;

        if (normalized.startsWith("http"))
        {
            textureId = normalized.substring(normalized.lastIndexOf('/') + 1);
        }
        else if (normalized.contains("textures.minecraft.net"))
        {
            int lastSlash = normalized.lastIndexOf('/');
            if (lastSlash != -1)
            {
                textureId = normalized.substring(lastSlash + 1);
            }
        }
        else if (normalized.matches("^[0-9a-fA-F]{32,}$"))
        {
            // Raw texture hash; accept as-is without Base64 decoding.
            textureId = normalized;
        }
        else
        {
            try
            {
                byte[] decodedBytes = Base64.getDecoder().decode(normalized);
                String decoded = new String(decodedBytes);

                int urlStart = decoded.indexOf("\"url\":\"");
                if (urlStart != -1)
                {
                    urlStart += 7;
                    int urlEnd = decoded.indexOf("\"", urlStart);
                    if (urlEnd != -1)
                    {
                        String url = decoded.substring(urlStart, urlEnd);
                        textureId = url.substring(url.lastIndexOf('/') + 1);
                    }
                }
            }
            catch (IllegalArgumentException e)
            {
                if (Main.inst != null && Main.inst.getLogger() != null && Settings.lang != null)
                {
                    String truncatedTexture = normalized.length() > 50 ? normalized.substring(0, 50) + "..." : normalized;
                    Main.inst.getLogger().warning(Settings.lang.getColored("invalid-head-texture") + ": " + truncatedTexture);
                }
            }
        }

        TEXTURE_ID_CACHE.put(texture, textureId);
        return textureId;
    }
}

