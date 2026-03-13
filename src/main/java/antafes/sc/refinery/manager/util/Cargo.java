package antafes.sc.refinery.manager.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Cargo
{
    public String format(int amount)
    {
        return String.format("%d SCU", amount);
    }

    public String formatFromCSCU(int amountInCSCU)
    {
        return format(amountInCSCU / 100);
    }
}
