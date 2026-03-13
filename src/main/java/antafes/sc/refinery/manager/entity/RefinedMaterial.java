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

import antafes.sc.base.entity.Material;
import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class RefinedMaterial
{
    @XmlAttribute
    private UUID key;
    @XmlElement
    private Material baseMaterial;
    /**
     * Amount of base material in cSCU (centi Standard Cargo Unit)
     */
    @XmlElement
    private int amount;
    @XmlElement
    private int quality;
    @XmlElement
    private int sellingPrice;
}
