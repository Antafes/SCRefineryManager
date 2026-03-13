package antafes.sc.refinery.manager.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Currency
{
    public String format(int amount) {
        return String.format("%d aUEC", amount);
    }
}
