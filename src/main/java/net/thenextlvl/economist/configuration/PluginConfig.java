package net.thenextlvl.economist.configuration;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Range;

import java.math.BigDecimal;
import java.util.Set;

public record PluginConfig(
        @SerializedName("minimum-payment-amount") double minimumPayment,
        @SerializedName("fractional-digits") @Range(from = 0, to = 30) int fractionalDigits,
        @SerializedName("currency-symbol") String currencySymbol,
        @SerializedName("start-balance") BigDecimal startBalance,
        @SerializedName("storage-type") StorageType storageType,
        @SerializedName("balance-aliases") Set<String> balanceAliases,
        @SerializedName("abbreviate-balance") boolean abbreviateBalance,
        @SerializedName("scientific-numbers") boolean scientificNumbers
) {
}
