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
