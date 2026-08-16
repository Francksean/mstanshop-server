package com.mstan.shop.common.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/** Formats amounts as FCFA (no decimal subunit in everyday use), e.g. "13 500 FCFA". */
public final class CurrencyFormatter {

    private CurrencyFormatter() {
    }

    public static String format(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.FRANCE);
        return numberFormat.format(amount) + " FCFA";
    }
}
