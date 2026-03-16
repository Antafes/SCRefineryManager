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

package antafes.sc.refinery.manager.entity;

import antafes.sc.refinery.manager.repository.adapter.RefinedMaterialsAdapter;
import antafes.sc.refinery.manager.repository.adapter.ZonedDateTimeAdapter;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Refinement
{
    @XmlAttribute
    private Integer key;

    @XmlElement
    @XmlJavaTypeAdapter(ZonedDateTimeAdapter.class)
    private ZonedDateTime createdAt;

    @XmlElement
    @XmlJavaTypeAdapter(RefinedMaterialsAdapter.class)
    private Map<UUID, RefinedMaterial> materials = new HashMap<>();

    @XmlElement
    private int cost;

    public int calculateTotalCargo()
    {
        if (this.materials == null) return 0;
        return this.materials.values().stream().mapToInt(refinedMaterial -> refinedMaterial.getAmount() / 100).sum();
    }

    public int calculateTotalSellingPrice()
    {
        if (this.materials == null) return 0;
        return this.materials.values().stream().mapToInt(RefinedMaterial::getSellingPrice).sum();
    }

    public int calculateTotalProfit()
    {
        return this.calculateTotalSellingPrice() - this.cost;
    }
}
