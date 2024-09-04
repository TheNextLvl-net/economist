package net.thenextlvl.economist.controller;

import net.thenextlvl.economist.EconomistPlugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("ALL")
class Abbreviation {
    private static final EconomistPlugin plugin = JavaPlugin.getPlugin(EconomistPlugin.class);
    private static final Map<BigDecimal, String> abbreviations = new HashMap<>();

    static {
        abbreviations.put(new BigDecimal("1e3"), "thousand");
        abbreviations.put(new BigDecimal("1e6"), "million");
        abbreviations.put(new BigDecimal("1e9"), "billion");
        abbreviations.put(new BigDecimal("1e12"), "trillion");
        abbreviations.put(new BigDecimal("1e15"), "quadrillion");
        abbreviations.put(new BigDecimal("1e18"), "quintillion");
        abbreviations.put(new BigDecimal("1e21"), "sextillion");
        abbreviations.put(new BigDecimal("1e24"), "septillion");
        abbreviations.put(new BigDecimal("1e27"), "octillion");
        abbreviations.put(new BigDecimal("1e30"), "nonillion");
        abbreviations.put(new BigDecimal("1e33"), "decillion");
        abbreviations.put(new BigDecimal("1e36"), "undecillion");
        abbreviations.put(new BigDecimal("1e39"), "duodecillion");
        abbreviations.put(new BigDecimal("1e42"), "tredecillion");
        abbreviations.put(new BigDecimal("1e45"), "quattuordecillion");
        abbreviations.put(new BigDecimal("1e48"), "quindecillion");
        abbreviations.put(new BigDecimal("1e51"), "sexdecillion");
        abbreviations.put(new BigDecimal("1e54"), "septendecillion");
        abbreviations.put(new BigDecimal("1e57"), "octodecillion");
        abbreviations.put(new BigDecimal("1e60"), "novemdecillion");
        abbreviations.put(new BigDecimal("1e63"), "vigintillion");
        abbreviations.put(new BigDecimal("1e66"), "unvigintillion");
        abbreviations.put(new BigDecimal("1e69"), "duovigintillion");
        abbreviations.put(new BigDecimal("1e72"), "trevigintillion");
        abbreviations.put(new BigDecimal("1e75"), "quattuorvigintillion");
        abbreviations.put(new BigDecimal("1e78"), "quinvigintillion");
        abbreviations.put(new BigDecimal("1e81"), "sexvigintillion");
        abbreviations.put(new BigDecimal("1e84"), "septenvigintillion");
        abbreviations.put(new BigDecimal("1e87"), "octovigintillion");
        abbreviations.put(new BigDecimal("1e90"), "novemvigintillion");
        abbreviations.put(new BigDecimal("1e93"), "trigintillion");
        abbreviations.put(new BigDecimal("1e96"), "untrigintillion");
        abbreviations.put(new BigDecimal("1e99"), "duotrigintillion");
        abbreviations.put(new BigDecimal("1e102"), "trestrigintillion");
        abbreviations.put(new BigDecimal("1e105"), "quattuortrigintillion");
        abbreviations.put(new BigDecimal("1e108"), "quintrigintillion");
        abbreviations.put(new BigDecimal("1e111"), "sextrigintillion");
        abbreviations.put(new BigDecimal("1e114"), "septentrigintillion");
        abbreviations.put(new BigDecimal("1e117"), "octotrigintillion");
        abbreviations.put(new BigDecimal("1e120"), "noventrigintillion");
        abbreviations.put(new BigDecimal("1e123"), "quadragintillion");
        abbreviations.put(new BigDecimal("1e126"), "unquadragintillion");
        abbreviations.put(new BigDecimal("1e129"), "duoquadragintillion");
        abbreviations.put(new BigDecimal("1e132"), "trequadragintillion");
        abbreviations.put(new BigDecimal("1e135"), "quattuorquadragintillion");
        abbreviations.put(new BigDecimal("1e138"), "quinquadragintillion");
        abbreviations.put(new BigDecimal("1e141"), "sexquadragintillion");
        abbreviations.put(new BigDecimal("1e144"), "septenquadragintillion");
        abbreviations.put(new BigDecimal("1e147"), "octoquadragintillion");
        abbreviations.put(new BigDecimal("1e150"), "novemquadragintillion");
        abbreviations.put(new BigDecimal("1e153"), "quinquagintillion");
        abbreviations.put(new BigDecimal("1e156"), "unquinquagintillion");
        abbreviations.put(new BigDecimal("1e159"), "duoquinquagintillion");
        abbreviations.put(new BigDecimal("1e162"), "trequinquagintillion");
        abbreviations.put(new BigDecimal("1e165"), "quattuorquinquagintillion");
        abbreviations.put(new BigDecimal("1e168"), "quinquinquagintillion");
        abbreviations.put(new BigDecimal("1e171"), "sexquinquagintillion");
        abbreviations.put(new BigDecimal("1e174"), "septenquinquagintillion");
        abbreviations.put(new BigDecimal("1e177"), "octoquinquagintillion");
        abbreviations.put(new BigDecimal("1e180"), "novemquinquagintillion");
        abbreviations.put(new BigDecimal("1e183"), "sexagintillion");
        abbreviations.put(new BigDecimal("1e186"), "unsexagintillion");
        abbreviations.put(new BigDecimal("1e189"), "duosexagintillion");
        abbreviations.put(new BigDecimal("1e192"), "tresexagintillion");
        abbreviations.put(new BigDecimal("1e195"), "quattuorsexagintillion");
        abbreviations.put(new BigDecimal("1e198"), "quinsexagintillion");
        abbreviations.put(new BigDecimal("1e201"), "sexsexagintillion");
        abbreviations.put(new BigDecimal("1e204"), "septensexagintillion");
        abbreviations.put(new BigDecimal("1e207"), "octosexagintillion");
        abbreviations.put(new BigDecimal("1e210"), "novemsexagintillion");
        abbreviations.put(new BigDecimal("1e213"), "septuagintillion");
        abbreviations.put(new BigDecimal("1e216"), "unseptuagintillion");
        abbreviations.put(new BigDecimal("1e219"), "duoseptuagintillion");
        abbreviations.put(new BigDecimal("1e222"), "treseptuagintillion");
        abbreviations.put(new BigDecimal("1e225"), "quattuorseptuagintillion");
        abbreviations.put(new BigDecimal("1e228"), "quinseptuagintillion");
        abbreviations.put(new BigDecimal("1e231"), "sexseptuagintillion");
        abbreviations.put(new BigDecimal("1e234"), "septenseptuagintillion");
        abbreviations.put(new BigDecimal("1e237"), "octoseptuagintillion");
        abbreviations.put(new BigDecimal("1e240"), "novemseptuagintillion");
        abbreviations.put(new BigDecimal("1e243"), "octogintillion");
        abbreviations.put(new BigDecimal("1e246"), "unoctogintillion");
        abbreviations.put(new BigDecimal("1e249"), "duooctogintillion");
        abbreviations.put(new BigDecimal("1e252"), "treoctogintillion");
        abbreviations.put(new BigDecimal("1e255"), "quattuoroctogintillion");
        abbreviations.put(new BigDecimal("1e258"), "quinoctogintillion");
        abbreviations.put(new BigDecimal("1e261"), "sexoctogintillion");
        abbreviations.put(new BigDecimal("1e264"), "septenoctogintillion");
        abbreviations.put(new BigDecimal("1e267"), "octooctogintillion");
        abbreviations.put(new BigDecimal("1e270"), "novemoctogintillion");
        abbreviations.put(new BigDecimal("1e273"), "nonagintillion");
        abbreviations.put(new BigDecimal("1e276"), "unnonagintillion");
        abbreviations.put(new BigDecimal("1e279"), "duononagintillion");
        abbreviations.put(new BigDecimal("1e282"), "trenonagintillion");
        abbreviations.put(new BigDecimal("1e285"), "quattuornonagintillion");
        abbreviations.put(new BigDecimal("1e288"), "quinnonagintillion");
        abbreviations.put(new BigDecimal("1e291"), "sexnonagintillion");
        abbreviations.put(new BigDecimal("1e294"), "septennonagintillion");
        abbreviations.put(new BigDecimal("1e297"), "octononagintillion");
        abbreviations.put(new BigDecimal("1e300"), "novemnonagintillion");
        abbreviations.put(new BigDecimal("1e303"), "centillion");
        abbreviations.put(new BigDecimal("1e306"), "uncentillion");
        abbreviations.put(new BigDecimal("1e309"), "duocentillion");
        abbreviations.put(new BigDecimal("1e312"), "trescentillion");
        abbreviations.put(new BigDecimal("1e315"), "quattuorcentillion");
        abbreviations.put(new BigDecimal("1e318"), "quincentillion");
        abbreviations.put(new BigDecimal("1e321"), "sexcentillion");
        abbreviations.put(new BigDecimal("1e324"), "septencentillion");
        abbreviations.put(new BigDecimal("1e327"), "octocentillion");
        abbreviations.put(new BigDecimal("1e330"), "novemcentillion");
        abbreviations.put(new BigDecimal("1e333"), "unducentillion");
        abbreviations.put(new BigDecimal("1e336"), "duoducentillion");
        abbreviations.put(new BigDecimal("1e339"), "treducentillion");
        abbreviations.put(new BigDecimal("1e342"), "quattuorducentillion");
        abbreviations.put(new BigDecimal("1e345"), "quinducentillion");
        abbreviations.put(new BigDecimal("1e348"), "sexducentillion");
        abbreviations.put(new BigDecimal("1e351"), "septenducentillion");
        abbreviations.put(new BigDecimal("1e354"), "octoducentillion");
        abbreviations.put(new BigDecimal("1e357"), "novemducentillion");
        abbreviations.put(new BigDecimal("1e360"), "untrecentillion");
        abbreviations.put(new BigDecimal("1e363"), "duotrecentillion");
        abbreviations.put(new BigDecimal("1e366"), "trecentillion");
        abbreviations.put(new BigDecimal("1e369"), "quattuortrecentillion");
        abbreviations.put(new BigDecimal("1e372"), "quintrecentillion");
        abbreviations.put(new BigDecimal("1e375"), "sextrecentillion");
        abbreviations.put(new BigDecimal("1e378"), "septentrecentillion");
        abbreviations.put(new BigDecimal("1e381"), "octotrecentillion");
        abbreviations.put(new BigDecimal("1e384"), "novemtrecentillion");
    }

    static String format(double amount, NumberFormat format, Locale locale) {
        var negative = amount < 0;
        var positive = negative ? -amount : amount;
        return abbreviations.entrySet().stream()
                .filter(entry -> entry.getKey().doubleValue() <= positive)
                .max((entry, other) -> entry.getKey().compareTo(other.getKey()))
                .map(entry -> {
                    var prefix = negative ? "-" : "";
                    var abbreviation = plugin.abbreviations().format(locale, entry.getValue());
                    var formatted = format.format(positive / entry.getKey().doubleValue());
                    return prefix + formatted + abbreviation;
                }).orElseGet(() -> format.format(amount));
    }
}
