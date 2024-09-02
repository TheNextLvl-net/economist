package net.thenextlvl.economist.model.configuration;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Range;

import java.math.BigDecimal;

public record PluginConfig(
        @SerializedName("fractional-digits") @Range(from = 0, to = 30) int fractionalDigits,
        @SerializedName("currency-symbol") String currencySymbol,
        @SerializedName("start-balance") BigDecimal startBalance,
        @SerializedName("storage-type") StorageType storageType
) {
}
