package Util;

import java.util.HashMap;
import java.util.Map;

public class CurrencyConverter {

    private static final Map<String, Double> exchangeRates = new HashMap<>();

    static {
        exchangeRates.put("RON", 1.0);
        exchangeRates.put("EUR", 4.95);
        exchangeRates.put("USD", 4.60);
    }

    public static double convert(String fromCurrency, String toCurrency, double amount) {
        if (!exchangeRates.containsKey(fromCurrency) || !exchangeRates.containsKey(toCurrency)) {
            throw new IllegalArgumentException("Unsupported currency.");
        }

        double amountInRon = amount * exchangeRates.get(fromCurrency); // convertim mai întâi în RON
        return amountInRon / exchangeRates.get(toCurrency);            // apoi din RON în moneda dorita
    }
}
