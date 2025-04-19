package net.thenextlvl.economist.configuration;

import com.google.gson.annotations.SerializedName;
import org.jspecify.annotations.NullMarked;

import java.util.Set;

@NullMarked
public class PluginConfig {
    public @SerializedName("storage-type") StorageType storageType = StorageType.SQLite;

    public @SerializedName("abbreviate-balance") boolean abbreviateBalance = true;
    public @SerializedName("scientific-numbers") boolean scientificNumbers = false;

    public @SerializedName("minimum-payment-amount") double minimumPayment = 0.01;
    public @SerializedName("max-loan-amount") double maxLoanAmount = 250;
    public @SerializedName("start-balance") double startBalance = 0;

    public @SerializedName("minimum-prune-days") int minimumPruneDays = 30;

    public @SerializedName("balance-aliases") Set<String> balanceAliases = Set.of("bal", "money");

    public @SerializedName("accounts") AccountConfig accounts = new AccountConfig();
    public @SerializedName("balance-top") BalanceTopConfig balanceTop = new BalanceTopConfig();
    public @SerializedName("banks") BankConfig banks = new BankConfig();
    public @SerializedName("currency") Currency currency = new Currency();

    public static class AccountConfig {
        public @SerializedName("auto-create") boolean autoCreate = true;
        public @SerializedName("per-world") boolean perWorld = false;
    }

    public static class BalanceTopConfig {
        public @SerializedName("entries-per-page") int entriesPerPage = 10;
        public @SerializedName("show-empty-accounts") boolean showEmptyAccounts = false;
    }

    public static class BankConfig {
        public @SerializedName("enabled") boolean enabled = true;
    }

    public static class Currency {
        public @SerializedName("currency-symbol") String symbol = "$";
        public @SerializedName("minimum-fractional-digits") int minFractionalDigits = 0;
        public @SerializedName("maximum-fractional-digits") int maxFractionalDigits = 2;
    }
}
