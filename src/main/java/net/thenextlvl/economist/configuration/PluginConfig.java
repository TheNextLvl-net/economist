package net.thenextlvl.economist.configuration;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Set;

@Getter
@Accessors(fluent = true)
public class PluginConfig {
    private @SerializedName("storage-type") StorageType storageType = StorageType.SQLite;

    private @SerializedName("abbreviate-balance") boolean abbreviateBalance = true;
    private @SerializedName("scientific-numbers") boolean scientificNumbers = false;

    private @SerializedName("minimum-payment-amount") double minimumPayment = 0.01;
    private @SerializedName("max-loan-amount") double maxLoanAmount = 250;
    private @SerializedName("start-balance") double startBalance = 0;

    private @SerializedName("minimum-prune-days") int minimumPruneDays = 30;

    private @SerializedName("balance-aliases") Set<String> balanceAliases = Set.of("bal", "money");

    private @SerializedName("accounts") AccountConfig accounts = new AccountConfig();
    private @SerializedName("balance-top") BalanceTopConfig balanceTop = new BalanceTopConfig();
    private @SerializedName("banks") BankConfig banks = new BankConfig();
    private @SerializedName("currency") Currency currency = new Currency();

    @Getter
    @Accessors(fluent = true)
    public static class AccountConfig {
        private @SerializedName("auto-create") boolean autoCreate = true;
        private @SerializedName("per-world") boolean perWorld = false;
    }

    @Getter
    @Accessors(fluent = true)
    public static class BalanceTopConfig {
        private @SerializedName("entries-per-page") int entriesPerPage = 10;
        private @SerializedName("load-unknown-player-names") boolean loadUnknownPlayerNames = false;
    }

    @Getter
    @Accessors(fluent = true)
    public static class BankConfig {
        private @SerializedName("enabled") boolean enabled = true;
    }

    @Getter
    @Accessors(fluent = true)
    public static class Currency {
        private @SerializedName("currency-symbol") String symbol = "$";
        private @SerializedName("minimum-fractional-digits") int minFractionalDigits = 0;
        private @SerializedName("maximum-fractional-digits") int maxFractionalDigits = 2;
    }
}
