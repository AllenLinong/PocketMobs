package dev.lone.pocketmobs.commands;

import dev.lone.pocketmobs.Constants;
import dev.lone.pocketmobs.Main;
import dev.lone.pocketmobs.Settings;
import dev.lone.pocketmobs.Utils;
import dev.lone.pocketmobs.data.Ball;
import dev.lone.pocketmobs.utils.InvUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Shared helpers for the ball-dispensing commands ({@code get} / {@code give}),
 * which otherwise duplicated ball-name validation, amount clamping and the
 * stack-batched give loop.
 */
final class CommandUtils
{
    static final int MAX_AMOUNT = Constants.MAX_COMMAND_AMOUNT;

    private CommandUtils()
    {
    }

    /**
     * Validates a ball-name argument. On failure sends the localized
     * "item-not-found" message to {@code sender} and returns {@code false}.
     */
    static boolean validateBallName(CommandSender sender, String ballArg)
    {
        String ballName = ballArg.trim();
        if (ballName.isEmpty()
                || ballName.length() > Constants.MAX_BALL_NAME_LENGTH
                || !Main.inst.ballsManager.exists(ballArg))
        {
            sender.sendMessage(Settings.lang.getColored("item-not-found").replace("{item}", ballArg));
            return false;
        }
        return true;
    }

    /**
     * Parses an optional amount argument, clamping it to [1, {@link #MAX_AMOUNT}]
     * and warning {@code sender} when the value was clamped down.
     */
    static int parseAmount(CommandSender sender, String amountArg)
    {
        int amount = Utils.parseInt(amountArg, 1);
        if (amount > MAX_AMOUNT)
        {
            sender.sendMessage(Settings.lang.getColored("amount-clamped").replace("{value}", String.valueOf(MAX_AMOUNT)));
            amount = MAX_AMOUNT;
        }
        return Math.max(1, amount);
    }

    /**
     * Gives {@code amount} balls of type {@code ballKey} to {@code target},
     * batched by the item's max stack size. Returns the ball's display name
     * (falling back to {@code ballKey} when it has none).
     */
    static String giveBalls(Player target, String ballKey, int amount)
    {
        Ball ball = Main.inst.ballsManager.byKey(ballKey);
        if (ball == null)
        {
            return ballKey;
        }

        ItemStack template = ball.getItemStack().clone();
        template.setAmount(1);

        int maxStackSize = Math.max(1, template.getMaxStackSize());
        int remaining = amount;
        while (remaining > 0)
        {
            ItemStack batch = template.clone();
            batch.setAmount(Math.min(remaining, maxStackSize));
            InvUtil.giveItem(target, batch);
            remaining -= batch.getAmount();
        }

        // Read the name from the Ball config directly. The deprecated
        // ItemMeta#getDisplayName() returns an empty string on Paper 1.21.4 when the
        // name was set via the Adventure displayName(Component) API, which made the
        // {item} placeholder render blank in the get/give messages.
        return ball.displayName != null && !ball.displayName.isEmpty() ? ball.displayName : ballKey;
    }
}
