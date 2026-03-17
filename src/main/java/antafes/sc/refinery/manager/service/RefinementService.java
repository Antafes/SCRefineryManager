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

package antafes.sc.refinery.manager.service;

import antafes.sc.base.entity.Material;
import antafes.sc.refinery.manager.dto.*;
import antafes.sc.refinery.manager.entity.RefinedMaterial;
import antafes.sc.refinery.manager.entity.Refinement;
import antafes.sc.refinery.manager.repository.RefinementRepository;
import antafes.sc.refinery.manager.util.Cargo;
import antafes.sc.refinery.manager.util.Name;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RefinementService
{
    @Autowired
    private RefinementRepository refinementRepository;

    public List<RefinementTableRowData> fetchTableRows()
    {
        return this.refinementRepository.findAll().entrySet().stream()
            .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
            .map(entry -> {
                Refinement refinement = entry.getValue();
                int revenue = refinement.calculateTotalSellingPrice();
                return new RefinementTableRowData(
                    entry.getKey(),
                    refinement.getCost(),
                    revenue,
                    revenue - refinement.getCost(),
                    this.formatMaterials(refinement),
                    refinement.getCreatedAt()
                );
            })
            .toList();
    }

    public RefinementForm createNewForm()
    {
        return new RefinementForm("", List.of(this.createNewMaterialRow()));
    }

    public RefinementMaterialForm createNewMaterialRow()
    {
        return this.createEmptyMaterialRow("0");
    }

    public RefinementMaterialForm createEditMaterialRow()
    {
        return this.createEmptyMaterialRow("");
    }

    public RefinementForm findForm(Integer key)
    {
        if (key == null) {
            throw new IllegalArgumentException("Refinement key must not be null.");
        }

        Refinement refinement = this.refinementRepository.findOne(key);
        if (refinement == null) {
            throw new IllegalArgumentException("Unknown refinement key: " + key);
        }

        List<RefinementMaterialForm> materials = new ArrayList<>();
        if (refinement.getMaterials() != null) {
            refinement.getMaterials().values().forEach(refinedMaterial -> materials.add(
                new RefinementMaterialForm(
                    refinedMaterial.getBaseMaterial(),
                    String.valueOf(refinedMaterial.getAmount()),
                    String.valueOf(refinedMaterial.getQuality()),
                    String.valueOf(refinedMaterial.getSellingPrice())
                )
            ));
        }

        if (materials.isEmpty()) {
            materials.add(this.createEditMaterialRow());
        }

        return new RefinementForm(String.valueOf(refinement.getCost()), List.copyOf(materials));
    }

    public SaveRefinementResult add(RefinementForm form)
    {
        ValidatedRefinementForm validatedForm = this.validate(form);
        if (!validatedForm.validation().isValid()) {
            return SaveRefinementResult.failure(validatedForm.validation());
        }

        Refinement refinement = this.buildRefinement(validatedForm);
        refinement.setCreatedAt(ZonedDateTime.now());
        this.refinementRepository.add(refinement);
        return SaveRefinementResult.success();
    }

    public SaveRefinementResult update(Integer key, RefinementForm form)
    {
        if (key == null) {
            throw new IllegalArgumentException("Refinement key must not be null.");
        }

        Refinement existingRefinement = this.refinementRepository.findOne(key);
        if (existingRefinement == null) {
            throw new IllegalArgumentException("Unknown refinement key: " + key);
        }

        ValidatedRefinementForm validatedForm = this.validate(form);
        if (!validatedForm.validation().isValid()) {
            return SaveRefinementResult.failure(validatedForm.validation());
        }

        Refinement refinement = this.buildRefinement(validatedForm);
        refinement.setKey(key);
        refinement.setCreatedAt(existingRefinement.getCreatedAt());
        this.refinementRepository.update(key, refinement);
        return SaveRefinementResult.success();
    }

    public void remove(Integer key)
    {
        this.refinementRepository.remove(key);
    }

    private ValidatedRefinementForm validate(RefinementForm form)
    {
        if (form == null) {
            throw new IllegalArgumentException("Refinement form must not be null.");
        }

        Integer cost = this.parseInteger(form.cost());
        String costErrorKey = cost == null ? "costRequired" : null;
        InvalidField firstInvalidField = costErrorKey == null
            ? null
            : new InvalidField(RefinementFieldType.COST, null);

        List<RefinementMaterialForm> materialForms = form.materials() == null ? List.of() : form.materials();
        List<ValidatedRefinementMaterialForm> validatedMaterials = new ArrayList<>();
        List<MaterialRowValidation> materialRowValidations = new ArrayList<>(materialForms.size());

        for (int rowIndex = 0; rowIndex < materialForms.size(); rowIndex++) {
            RefinementMaterialForm materialForm = materialForms.get(rowIndex);
            Material material = materialForm == null ? null : materialForm.material();
            Integer amount = this.parseInteger(materialForm == null ? null : materialForm.amount());
            Integer quality = this.parseInteger(materialForm == null ? null : materialForm.quality());
            Integer revenue = this.parseInteger(materialForm == null ? null : materialForm.revenue());

            String materialErrorKey = material == null ? "materialRequired" : null;
            String amountErrorKey = amount == null ? "amountRequired" : null;
            String revenueErrorKey = revenue == null ? "revenueRequired" : null;

            materialRowValidations.add(new MaterialRowValidation(materialErrorKey, amountErrorKey, revenueErrorKey));

            if (firstInvalidField == null) {
                if (materialErrorKey != null) {
                    firstInvalidField = new InvalidField(RefinementFieldType.MATERIAL, rowIndex);
                } else if (amountErrorKey != null) {
                    firstInvalidField = new InvalidField(RefinementFieldType.AMOUNT, rowIndex);
                } else if (revenueErrorKey != null) {
                    firstInvalidField = new InvalidField(RefinementFieldType.REVENUE, rowIndex);
                }
            }

            if (materialErrorKey == null && amountErrorKey == null && revenueErrorKey == null) {
                validatedMaterials.add(new ValidatedRefinementMaterialForm(
                    material,
                    amount,
                    quality == null ? 0 : quality,
                    revenue
                ));
            }
        }

        return new ValidatedRefinementForm(
            cost == null ? 0 : cost,
            List.copyOf(validatedMaterials),
            new RefinementValidation(costErrorKey, List.copyOf(materialRowValidations), firstInvalidField)
        );
    }

    private Refinement buildRefinement(ValidatedRefinementForm validatedForm)
    {
        Refinement refinement = new Refinement();
        refinement.setCost(validatedForm.cost());

        Map<UUID, RefinedMaterial> materials = new LinkedHashMap<>();
        validatedForm.materials().forEach(materialForm -> {
            RefinedMaterial refinedMaterial = new RefinedMaterial();
            refinedMaterial.setKey(UUID.randomUUID());
            refinedMaterial.setBaseMaterial(materialForm.material());
            refinedMaterial.setAmount(materialForm.amount());
            refinedMaterial.setQuality(materialForm.quality());
            refinedMaterial.setSellingPrice(materialForm.revenue());
            materials.put(refinedMaterial.getKey(), refinedMaterial);
        });
        refinement.setMaterials(materials);

        return refinement;
    }

    private Integer parseInteger(String text)
    {
        if (text == null || text.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private RefinementMaterialForm createEmptyMaterialRow(String revenue)
    {
        return new RefinementMaterialForm(null, "", "", revenue);
    }

    private String formatMaterials(Refinement refinement)
    {
        if (refinement.getMaterials() == null || refinement.getMaterials().isEmpty()) {
            return "";
        }

        Map<Object, MaterialAggregate> combined = new LinkedHashMap<>();
        refinement.getMaterials().values().forEach(refinedMaterial -> {
            Material displayMaterial = this.getDisplayMaterial(refinedMaterial);
            Object materialKey = displayMaterial != null ? displayMaterial.getKey() : null;
            combined.compute(
                materialKey,
                (_, aggregate) -> aggregate == null
                    ? new MaterialAggregate(displayMaterial, refinedMaterial.getAmount())
                    : aggregate.addAmount(refinedMaterial.getAmount())
            );
        });

        return combined.values().stream()
            .sorted(Comparator.comparing(a -> this.getMaterialDisplayName(a.material()), String.CASE_INSENSITIVE_ORDER))
            .map(a -> "%s (%s)".formatted(
                this.getMaterialDisplayName(a.material()),
                Cargo.formatFromCSCU(a.amountCSCU())
            ))
            .collect(Collectors.joining(", "));
    }

    private Material getDisplayMaterial(RefinedMaterial refinedMaterial)
    {
        if (refinedMaterial == null) {
            return null;
        }

        Material displayMaterial = refinedMaterial.getBaseMaterial();
        if (displayMaterial != null && displayMaterial.getReferences() != null) {
            displayMaterial = displayMaterial.getReferences();
        }
        return displayMaterial;
    }

    private String getMaterialDisplayName(Material displayMaterial)
    {
        return displayMaterial == null ? "" : Name.fetchTranslatedName(displayMaterial);
    }

    private record MaterialAggregate(Material material, int amountCSCU)
    {
        private MaterialAggregate addAmount(int additionalAmountCSCU)
        {
            return new MaterialAggregate(this.material, this.amountCSCU + additionalAmountCSCU);
        }
    }
}
