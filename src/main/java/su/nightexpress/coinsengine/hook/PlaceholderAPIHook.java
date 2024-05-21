package su.nightexpress.coinsengine.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.coinsengine.CoinsEnginePlugin;
import su.nightexpress.coinsengine.api.currency.Currency;
import su.nightexpress.coinsengine.data.impl.CoinsUser;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.Pair;
import su.nightexpress.nightcore.util.text.NightMessage;

import java.text.DecimalFormat;
import java.util.List;

public class PlaceholderAPIHook {

    private static PointsExpansion expansion;

    public static void setup(@NotNull CoinsEnginePlugin plugin) {
        if (expansion == null) {
            expansion = new PointsExpansion(plugin);
            expansion.register();
        }
    }

    public static void shutdown() {
        if (expansion != null) {
            expansion.unregister();
            expansion = null;
        }
    }

    private static class PointsExpansion extends PlaceholderExpansion {

        private final CoinsEnginePlugin plugin;

        public PointsExpansion(@NotNull CoinsEnginePlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        @NotNull
        public String getAuthor() {
            return this.plugin.getDescription().getAuthors().get(0);
        }

        @Override
        @NotNull
        public String getIdentifier() {
            return this.plugin.getDescription().getName().toLowerCase();
        }

        @Override
        @NotNull
        public String getVersion() {
            return this.plugin.getDescription().getVersion();
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public String onPlaceholderRequest(Player player, String holder) {
            if (holder.startsWith("server_balance_raw_")) {
                String curId = holder.substring("server_balance_raw_".length());
                Currency currency = plugin.getCurrencyManager().getCurrency(curId);
                if (currency == null) return null;

                DecimalFormat format = new DecimalFormat("#");
                format.setMaximumFractionDigits(8);

                return format.format(plugin.getCurrencyManager().getTotalBalance(currency));
            }
            if (holder.startsWith("server_balance_short_")) {
                String curId = holder.substring("server_balance_short_".length());
                Currency currency = plugin.getCurrencyManager().getCurrency(curId);
                if (currency == null) return null;

                return currency.formatCompact(plugin.getCurrencyManager().getTotalBalance(currency)); // line format_short:
            }
            if (holder.startsWith("server_balance_")) {
                String curId = holder.substring("server_balance_".length());
                Currency currency = plugin.getCurrencyManager().getCurrency(curId);
                if (currency == null) return null;

                return currency.format(plugin.getCurrencyManager().getTotalBalance(currency)); // line format:
            }
            
            // top_balance_coins_1
            // top_player_coins_1
            if (holder.startsWith("top_")) {
                String cut = holder.substring("top_".length()); // balance_coins_1
                String[] split = cut.split("_");
                if (split.length < 3) return null;

                String type = split[0];
                String currencyId = split[1];
                Currency currency = plugin.getCurrencyManager().getCurrency(currencyId);
                if (currency == null) return null;

                int pos = NumberUtil.getInteger(split[2]);
                if (pos <= 0) return null;

                List<Pair<String, Double>> baltop = plugin.getCurrencyManager().getBalanceList(currency);
                if (pos > baltop.size()) return "-";

                Pair<String, Double> pair = baltop.get(pos - 1);
                if (type.equalsIgnoreCase("balance")) return NightMessage.asLegacy(currency.format(pair.getSecond()));
                if (type.equalsIgnoreCase("player")) return NightMessage.asLegacy(pair.getFirst());

                return null;
            }

            if (player == null) return null;

            CoinsUser user = plugin.getUserManager().getUserData(player);

            if (holder.startsWith("balance_raw_")) {
                String currencyId = holder.substring("balance_raw_".length()); // coins
                Currency currency = plugin.getCurrencyManager().getCurrency(currencyId);
                if (currency == null) return null;

                DecimalFormat format = new DecimalFormat("#");
                format.setMaximumFractionDigits(8);

                return format.format(currency.fine(user.getBalance(currency)));
            }
            if (holder.startsWith("balance_rounded_")) {
                String currencyId = holder.substring("balance_rounded_".length()); // coins
                Currency currency = plugin.getCurrencyManager().getCurrency(currencyId);
                if (currency == null) return null;

                return NumberUtil.format(currency.fine(user.getBalance(currency)));
            }
            if (holder.startsWith("balance_short_")) {
                String currencyId = holder.substring("balance_short_".length()); // coins
                Currency currency = plugin.getCurrencyManager().getCurrency(currencyId);
                if (currency == null) return null;

                return currency.formatCompact(user.getBalance(currency)); 
            }
            if (holder.startsWith("balance_")) {
                String currencyId = holder.substring("balance_".length());
                Currency currency = plugin.getCurrencyManager().getCurrency(currencyId);
                if (currency == null) return null;

                return currency.format(user.getBalance(currency));
            }

            return null;
        }
    }
}
