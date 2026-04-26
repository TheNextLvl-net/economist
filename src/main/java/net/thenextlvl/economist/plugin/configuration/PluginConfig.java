package net.thenextlvl.economist.plugin.configuration;

import com.google.gson.annotations.SerializedName;

import java.util.Set;

public class PluginConfig {
    public @SerializedName("abbreviate-balance") boolean abbreviateBalance = true;
    public @SerializedName("scientific-numbers") boolean scientificNumbers = false;

    public @SerializedName("minimum-payment-amount") double minimumPayment = 0.01;

    public @SerializedName("minimum-prune-days") int minimumPruneDays = 30;

    public @SerializedName("balance-aliases") Set<String> balanceAliases = Set.of("bal", "money");

    public @SerializedName("accounts") AccountConfig accounts = new AccountConfig();
    public @SerializedName("pagination") Pagination pagination = new Pagination();
    public @SerializedName("banks") BankConfig banks = new BankConfig();
    public @SerializedName("database") DatabaseConfig database = new DatabaseConfig();

    public static class AccountConfig {
        public @SerializedName("auto-create") boolean autoCreate = true;
        public @SerializedName("per-world") boolean perWorld = false;
    }

    public static class Pagination {
        public @SerializedName("entries-per-page") int entriesPerPage = 10;
        public @SerializedName("show-empty-accounts") boolean showEmptyAccounts = false;
    }

    public static class BankConfig {
        public @SerializedName("enabled") boolean enabled = true;
    }

    public static class DatabaseConfig {
        public @SerializedName("storage-type") StorageType storageType = StorageType.SQLite;
        public @SerializedName("jdbc-url") String jdbcUrl = "";
        public @SerializedName("host") String host = "localhost";
        public @SerializedName("port") int port = 0;
        public @SerializedName("database") String database = "economist";
        public @SerializedName("username") String username = "";
        public @SerializedName("password") String password = "";
    }
}
