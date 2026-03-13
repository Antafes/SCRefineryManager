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

import antafes.sc.refinery.manager.entity.RefinedMaterial;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RefinedMaterialsAdapter extends XmlAdapter<RefinedMaterialsAdapter.MaterialsWrapper, Map<UUID, RefinedMaterial>>
{
    @Override
    public MaterialsWrapper marshal(Map<UUID, RefinedMaterial> materials) throws Exception
    {
        MaterialsWrapper wrapper = new MaterialsWrapper();
        wrapper.materials.addAll(materials.values());

        return wrapper;
    }

    @Override
    public Map<UUID, RefinedMaterial> unmarshal(MaterialsWrapper wrapper) throws Exception
    {
        Map<UUID, RefinedMaterial> map = new java.util.HashMap<>();

        for (RefinedMaterial material : wrapper.materials) {
            map.put(material.getKey(), material);
        }

        return map;
    }

    @XmlRootElement(name = "materials")
    public static class MaterialsWrapper
    {
        @XmlElement(name = "material")
        public List<RefinedMaterial> materials = new ArrayList<>();
    }
}
