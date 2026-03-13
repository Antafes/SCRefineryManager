package antafes.sc.refinery.manager.entity;

import antafes.sc.refinery.manager.repository.adapter.RefinedMaterialsAdapter;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;
import lombok.experimental.Accessors;

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
    @XmlJavaTypeAdapter(type = HashMap.class, value = RefinedMaterialsAdapter.class)
    private Map<UUID, RefinedMaterial> materials;
    @XmlElement
    private int cost;

    public int calculateTotalCargo()
    {
        return this.materials.values().stream().mapToInt(refinedMaterial -> refinedMaterial.getAmount() / 100).sum();
    }

    public int calculateTotalSellingPrice()
    {
        return this.materials.values().stream().mapToInt(RefinedMaterial::getSellingPrice).sum();
    }

    public int calculateTotalProfit()
    {
        return this.calculateTotalSellingPrice() - this.cost;
    }
}
