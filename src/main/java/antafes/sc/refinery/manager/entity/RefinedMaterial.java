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
    private int sellingPrice;
}
