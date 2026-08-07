package dev.lone.pocketmobs;

import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

final class CaughtMobDisplaySupport
{
    private CaughtMobDisplaySupport()
    {
    }

    static String getDisplayName(CaughtMob mob)
    {
        DisplayDetails details = buildDisplayDetails(mob);

        if (details.status.isEmpty() && details.variant.isEmpty() && details.extra.isEmpty())
        {
            return details.name;
        }

        List<String> summary = new ArrayList<>();
        summary.addAll(details.status);
        summary.addAll(details.variant);
        summary.addAll(details.extra);
        return details.name + " [" + String.join(", ", summary) + "]";
    }

    static List<String> getDisplayLoreLines(CaughtMob mob)
    {
        DisplayDetails details = buildDisplayDetails(mob);
        List<String> lines = new ArrayList<>();
        if (!details.status.isEmpty())
        {
            lines.add(Settings.lang.getColored("mob-status-line").replace("{value}", String.join(", ", details.status)));
        }
        if (!details.variant.isEmpty())
        {
            lines.add(Settings.lang.getColored("mob-variant-line").replace("{value}", String.join(", ", details.variant)));
        }
        if (!details.extra.isEmpty())
        {
            lines.add(Settings.lang.getColored("mob-extra-line").replace("{value}", String.join(", ", details.extra)));
        }
        return lines;
    }

    private static DisplayDetails buildDisplayDetails(CaughtMob mob)
    {
        DisplayDetails details = new DisplayDetails(formatDisplayToken(mob.type));
        appendVillagerLoreDetails(mob, details.variant, details.extra);
        appendHorseLoreDetails(mob, details.status, details.variant, details.extra);
        appendVariantLoreDetails(mob, details.status, details.variant, details.extra);
        return details;
    }

    private static void appendVillagerLoreDetails(CaughtMob mob, List<String> variant, List<String> extra)
    {
        if (mob.villagerData == null || mob.villagerData.isEmpty())
        {
            return;
        }

        for (String part : mob.villagerData.split(";"))
        {
            if (part.startsWith("profession:"))
            {
                variant.add(localizeDetailValue(part.substring("profession:".length())));
            }
            else if (part.startsWith("type:"))
            {
                extra.add(localizeDetailValue(part.substring("type:".length())));
            }
            else if (part.startsWith("level:"))
            {
                extra.add("Lv." + part.substring("level:".length()));
            }
        }
    }

    private static void appendHorseLoreDetails(CaughtMob mob, List<String> status, List<String> variant, List<String> extra)
    {
        if (mob.horseData == null || mob.horseData.isEmpty())
        {
            return;
        }

        for (String part : mob.horseData.split(";"))
        {
            if (part.startsWith("tamed:") && Boolean.parseBoolean(part.substring("tamed:".length())))
            {
                status.add(localizeDetailKey("status", "tamed"));
            }
            else if (part.startsWith("saddled:") && Boolean.parseBoolean(part.substring("saddled:".length())))
            {
                status.add(localizeDetailKey("status", "saddled"));
            }
            else if (part.startsWith("color:"))
            {
                variant.add(localizeDetailValue(part.substring("color:".length())));
            }
            else if (part.startsWith("style:"))
            {
                variant.add(localizeDetailValue(part.substring("style:".length())));
            }
            else if (part.startsWith("armor:"))
            {
                extra.add(localizeDetailValue(part.substring("armor:".length())));
            }
            else if (part.startsWith("jump:"))
            {
                extra.add(localizeTemplate("mob-detail.jump", trimDecimal(part.substring("jump:".length()))));
            }
        }
    }

    private static void appendVariantLoreDetails(CaughtMob mob, List<String> status, List<String> variant, List<String> extra)
    {
        if (mob.variantData == null || mob.variantData.isEmpty())
        {
            return;
        }

        boolean tamed = false;
        for (String part : mob.variantData.split(";"))
        {
            if (part.startsWith("tamed:") && Boolean.parseBoolean(part.substring("tamed:".length())))
            {
                tamed = true;
                status.add(localizeDetailKey("status", "tamed"));
            }
            else if (part.startsWith("sitting:") && tamed && Boolean.parseBoolean(part.substring("sitting:".length())))
            {
                status.add(localizeDetailKey("status", "sitting"));
            }
            else if (part.startsWith("cat_type:"))
            {
                variant.add(localizeDetailValue(part.substring("cat_type:".length())));
            }
            else if (part.startsWith("collar:") && tamed)
            {
                extra.add(localizeTemplate("mob-detail.collar", localizeDetailValue(part.substring("collar:".length()))));
            }
            else if (part.startsWith("angry:") && tamed && Boolean.parseBoolean(part.substring("angry:".length())))
            {
                status.add(localizeDetailKey("status", "angry"));
            }
            else if (part.startsWith("parrot_variant:"))
            {
                variant.add(localizeDetailValue(part.substring("parrot_variant:".length())));
            }
            else if (part.startsWith("rabbit_type:"))
            {
                variant.add(localizeDetailValue(part.substring("rabbit_type:".length())));
            }
            else if (part.startsWith("fox_type:"))
            {
                variant.add(localizeDetailValue(part.substring("fox_type:".length())));
            }
            else if (part.startsWith("main_gene:"))
            {
                variant.add(localizeTemplate("mob-detail.main-gene", localizeDetailValue(part.substring("main_gene:".length()))));
            }
            else if (part.startsWith("hidden_gene:"))
            {
                extra.add(localizeTemplate("mob-detail.hidden-gene", localizeDetailValue(part.substring("hidden_gene:".length()))));
            }
            else if (part.startsWith("fish_pattern:"))
            {
                variant.add(localizeDetailValue(part.substring("fish_pattern:".length())));
            }
            else if (part.startsWith("body_color:"))
            {
                extra.add(localizeTemplate("mob-detail.body-color", localizeDetailValue(part.substring("body_color:".length()))));
            }
            else if (part.startsWith("pattern_color:"))
            {
                extra.add(localizeTemplate("mob-detail.pattern-color", localizeDetailValue(part.substring("pattern_color:".length()))));
            }
            else if (part.startsWith("axolotl_variant:"))
            {
                variant.add(localizeDetailValue(part.substring("axolotl_variant:".length())));
            }
            else if (part.startsWith("frog_variant:"))
            {
                variant.add(localizeDetailValue(part.substring("frog_variant:".length())));
            }
            else if (part.startsWith("llama_color:"))
            {
                variant.add(localizeDetailValue(part.substring("llama_color:".length())));
            }
        }
    }

    private static String localizeDetailValue(String raw)
    {
        String normalized = normalizeDetailKey(raw);
        String key = "mob-detail.value." + normalized;
        String localized = Settings.lang.getString(key);
        if (localized == null || localized.isEmpty() || localized.equals(key))
        {
            return formatDisplayToken(raw);
        }
        return Settings.lang.getColored(key);
    }

    private static String localizeDetailKey(String group, String keySuffix)
    {
        String key = "mob-detail." + group + "." + normalizeDetailKey(keySuffix);
        String localized = Settings.lang.getString(key);
        if (localized == null || localized.isEmpty() || localized.equals(key))
        {
            return formatDisplayToken(keySuffix);
        }
        return Settings.lang.getColored(key);
    }

    private static String localizeTemplate(String key, String value)
    {
        String localized = Settings.lang.getString(key);
        if (localized == null || localized.isEmpty() || localized.equals(key))
        {
            return value;
        }
        return Settings.lang.getColored(key).replace("{value}", value);
    }

    private static String normalizeDetailKey(String raw)
    {
        return raw == null ? "" : raw.trim().toLowerCase().replace(' ', '_').replace('-', '_');
    }

    private static String trimDecimal(String raw)
    {
        try
        {
            double value = Double.parseDouble(raw);
            if (value == Math.rint(value))
            {
                return String.valueOf((int) value);
            }
            return String.format(java.util.Locale.ROOT, "%.2f", value);
        }
        catch (NumberFormatException ignored)
        {
            return raw;
        }
    }

    private static String formatDisplayToken(String raw)
    {
        if (raw == null || raw.isEmpty())
        {
            return "";
        }

        String normalized = raw.replace(':', ' ').replace('_', ' ').replace('-', ' ').trim();
        if (normalized.isEmpty())
        {
            return raw;
        }

        String[] words = normalized.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String word : words)
        {
            if (word.isEmpty())
            {
                continue;
            }
            if (builder.length() > 0)
            {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1)
            {
                builder.append(word.substring(1).toLowerCase());
            }
        }
        return builder.toString();
    }

    private static String formatDisplayToken(EntityType type)
    {
        return formatDisplayToken(type.name());
    }

    private static final class DisplayDetails
    {
        private final String name;
        private final List<String> status = new ArrayList<>();
        private final List<String> variant = new ArrayList<>();
        private final List<String> extra = new ArrayList<>();

        private DisplayDetails(String name)
        {
            this.name = name;
        }
    }
}
