package net.thenextlvl.economist.configuration;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Set;

@Getter
@Accessors(fluent = true)
public final class PluginConfig {
    private final @SerializedName("storage-type") StorageType storageType = StorageType.SQLite;

    private final @SerializedName("abbreviate-balance") boolean abbreviateBalance = true;
    private final @SerializedName("scientific-numbers") boolean scientificNumbers = false;

    private final @SerializedName("minimum-payment-amount") double minimumPayment = 0.01;
    private final @SerializedName("max-loan-amount") double maxLoanAmount = 250;
    private final @SerializedName("start-balance") double startBalance = 0;

    private final @SerializedName("balance-aliases") Set<String> balanceAliases = Set.of("bal", "money");

    private final @SerializedName("accounts") AccountConfig accounts = new AccountConfig();
    private final @SerializedName("balance-top") BalanceTopConfig balanceTop = new BalanceTopConfig();
    private final @SerializedName("banks") BankConfig banks = new BankConfig();
    private final @SerializedName("currency") Currency currency = new Currency();

    @Getter
    @Accessors(fluent = true)
    public static final class AccountConfig {
        private final @SerializedName("auto-create") boolean autoCreate = true;
        private final @SerializedName("per-world") boolean perWorld = false;
    }

    @Getter
    @Accessors(fluent = true)
    public static final class BalanceTopConfig {
        private final @SerializedName("entries-per-page") int entriesPerPage = 10;
        private final @SerializedName("load-unknown-player-names") boolean loadUnknownPlayerNames = false;
    }

    @Getter
    @Accessors(fluent = true)
    public static final class BankConfig {
        private final @SerializedName("enabled") boolean enabled = true;
    }

    @Getter
    @Accessors(fluent = true)
    public static final class Currency {
        private final @SerializedName("currency-symbol") String symbol = "$";
        private final @SerializedName("minimum-fractional-digits") int minFractionalDigits = 0;
        private final @SerializedName("maximum-fractional-digits") int maxFractionalDigits = 2;
    }
}
