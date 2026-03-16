/*
 * This file is part of SCRefineryManager.
 *
 * SCRefineryManager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SCRefineryManager is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SCRefineryManager. If not, see <http://www.gnu.org/licenses/>.
 *
 * @package SCRefineryManager
 * @author Marian Pollzien <map@wafriv.de>
 * @copyright (c) 2026, Marian Pollzien
 * @license https://www.gnu.org/licenses/lgpl.html LGPLv3
 */

package antafes.sc.refinery.manager.repository.adapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * JAXB adapter for serializing {@link ZonedDateTime}.
 *
 * Uses ISO-8601 strings (e.g. 2026-03-16T15:04:22.222+01:00[Europe/Berlin]).
 * For backward compatibility it also accepts epoch-millis values written as numbers.
 */
public class ZonedDateTimeAdapter extends XmlAdapter<String, ZonedDateTime>
{
    @Override
    public ZonedDateTime unmarshal(String value)
    {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return ZonedDateTime.parse(value);
        } catch (RuntimeException ignored) {
            // Backward-compatibility: allow epoch millis.
            try {
                long epochMillis = Long.parseLong(value.trim());
                return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
            } catch (RuntimeException ignoredAgain) {
                return null;
            }
        }
    }

    @Override
    public String marshal(ZonedDateTime value)
    {
        return value == null ? null : value.toString();
    }
}
