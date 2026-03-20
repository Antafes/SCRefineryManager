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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = RefinementServiceTest.TestConfiguration.class)
class RefinementServiceTest
{
    @MockitoBean
    private RefinementRepository refinementRepository;

    @Autowired
    private RefinementService refinementService;

    @Test
    void createNewFormProvidesSingleBlankRowWithDefaultRevenue()
    {
        RefinementForm form = this.refinementService.createNewForm();

        assertThat(form.cost()).isEmpty();
        assertThat(form.materials()).singleElement().satisfies(row -> {
            assertThat(row.material()).isNull();
            assertThat(row.amount()).isEmpty();
            assertThat(row.quality()).isEmpty();
            assertThat(row.revenue()).isEqualTo("0");
        });
    }

    @Test
    void addBuildsAndSavesRefinementFromForm()
    {
        Material material = material("quantanium", "Quantanium");

        SaveRefinementResult result = this.refinementService.add(new RefinementForm(
            "300",
            List.of(new RefinementMaterialForm(material, "125", "95", "700"))
        ));

        assertThat(result.successful()).isTrue();

        ArgumentCaptor<Refinement> refinementCaptor = ArgumentCaptor.forClass(Refinement.class);
        verify(this.refinementRepository).add(refinementCaptor.capture());
        Refinement savedRefinement = refinementCaptor.getValue();

        assertThat(savedRefinement.getCost()).isEqualTo(300);
        assertThat(savedRefinement.getCreatedAt()).isNotNull();
        assertThat(savedRefinement.getMaterials()).hasSize(1);

        RefinedMaterial refinedMaterial = savedRefinement.getMaterials().values().iterator().next();
        assertThat(refinedMaterial.getBaseMaterial()).isSameAs(material);
        assertThat(refinedMaterial.getAmount()).isEqualTo(125);
        assertThat(refinedMaterial.getQuality()).isEqualTo(95);
        assertThat(refinedMaterial.getSellingPrice()).isEqualTo(700);
    }

    @Test
    void addReturnsValidationErrorsInsteadOfSavingInvalidForm()
    {
        SaveRefinementResult result = this.refinementService.add(new RefinementForm(
            "",
            List.of(new RefinementMaterialForm(null, "", "7", ""))
        ));

        assertThat(result.successful()).isFalse();
        assertThat(result.validation().costErrorKey()).isEqualTo("costRequired");
        assertThat(result.validation().firstInvalidField())
            .isEqualTo(new InvalidField(RefinementFieldType.COST, null));
        assertThat(result.validation().materialRows()).singleElement().satisfies(row -> {
            assertThat(row.materialErrorKey()).isEqualTo("materialRequired");
            assertThat(row.amountErrorKey()).isEqualTo("amountRequired");
            assertThat(row.revenueErrorKey()).isEqualTo("revenueRequired");
        });

        verifyNoInteractions(this.refinementRepository);
    }

    @Test
    void fetchTableRowsBuildsAggregatedDisplayRows()
    {
        Material aluminium = material("aluminium", "Aluminium");
        Material quartzReference = material("quartz", "Quartz");
        Material quartzOreA = materialWithReference("quartz-ore-a", quartzReference);
        Material quartzOreB = materialWithReference("quartz-ore-b", quartzReference);

        Map<UUID, RefinedMaterial> materials = new LinkedHashMap<>();
        materials.put(UUID.randomUUID(), refinedMaterial(quartzOreA, 101, 0, 50));
        materials.put(UUID.randomUUID(), refinedMaterial(quartzOreB, 50, 0, 150));
        materials.put(UUID.randomUUID(), refinedMaterial(aluminium, 100, 85, 200));

        Map<Integer, Refinement> refinements = new LinkedHashMap<>();
        refinements.put(2, new Refinement()
            .setKey(2)
            .setCost(180)
            .setCreatedAt(ZonedDateTime.of(2026, 3, 17, 10, 0, 0, 0, ZoneOffset.UTC))
            .setMaterials(materials));
        when(this.refinementRepository.findAll()).thenReturn(refinements);

        List<RefinementTableRowData> rows = this.refinementService.fetchTableRows();

        assertThat(rows).singleElement().satisfies(row -> {
            assertThat(row.key()).isEqualTo(2);
            assertThat(row.cost()).isEqualTo(180);
            assertThat(row.revenue()).isEqualTo(400);
            assertThat(row.profit()).isEqualTo(220);
            assertThat(row.materials()).isEqualTo("Aluminium (1 SCU, 85), Quartz (2 SCU)");
        });
    }

    @Test
    void fetchTableRowsListsSameMaterialWithDifferentQualitiesSeparately()
    {
        Material aluminium = material("aluminium", "Aluminium");

        Map<UUID, RefinedMaterial> materials = new LinkedHashMap<>();
        materials.put(UUID.randomUUID(), refinedMaterial(aluminium, 100, 80, 200));
        materials.put(UUID.randomUUID(), refinedMaterial(aluminium, 50, 90, 150));

        Map<Integer, Refinement> refinements = new LinkedHashMap<>();
        refinements.put(3, new Refinement()
            .setKey(3)
            .setCost(100)
            .setCreatedAt(ZonedDateTime.of(2026, 3, 17, 10, 0, 0, 0, ZoneOffset.UTC))
            .setMaterials(materials));
        when(this.refinementRepository.findAll()).thenReturn(refinements);

        List<RefinementTableRowData> rows = this.refinementService.fetchTableRows();

        assertThat(rows).singleElement().satisfies(row ->
            assertThat(row.materials()).isEqualTo("Aluminium (1 SCU, 80), Aluminium (1 SCU, 90)")
        );
    }

    @Test
    void findFormMapsExistingRefinementForEditing()
    {
        Material material = material("borase", "Borase");
        Map<UUID, RefinedMaterial> materials = new LinkedHashMap<>();
        materials.put(UUID.randomUUID(), refinedMaterial(material, 400, 0, 900));
        when(this.refinementRepository.findOne(7)).thenReturn(new Refinement()
            .setKey(7)
            .setCost(450)
            .setMaterials(materials));

        RefinementForm form = this.refinementService.findForm(7);

        assertThat(form.cost()).isEqualTo("450");
        assertThat(form.materials()).singleElement().satisfies(entry -> {
            assertThat(entry.material()).isSameAs(material);
            assertThat(entry.amount()).isEqualTo("400");
            assertThat(entry.quality()).isEqualTo("0");
            assertThat(entry.revenue()).isEqualTo("900");
        });
    }

    @Test
    void findFormProvidesBlankEditRowWhenRefinementHasNoMaterials()
    {
        when(this.refinementRepository.findOne(11)).thenReturn(new Refinement()
            .setKey(11)
            .setCost(275)
            .setMaterials(new LinkedHashMap<>()));

        RefinementForm form = this.refinementService.findForm(11);

        assertThat(form.cost()).isEqualTo("275");
        assertThat(form.materials()).singleElement().satisfies(entry -> {
            assertThat(entry.material()).isNull();
            assertThat(entry.amount()).isEmpty();
            assertThat(entry.quality()).isEmpty();
            assertThat(entry.revenue()).isEmpty();
        });
    }

    @Test
    void updateBuildsRefinementAndPreservesExistingCreatedAt()
    {
        Material material = material("bexalite", "Bexalite");
        ZonedDateTime createdAt = ZonedDateTime.of(2026, 3, 16, 11, 45, 0, 0, ZoneOffset.UTC);
        when(this.refinementRepository.findOne(3)).thenReturn(new Refinement()
            .setKey(3)
            .setCreatedAt(createdAt)
            .setCost(100));

        SaveRefinementResult result = this.refinementService.update(3, new RefinementForm(
            "200",
            List.of(new RefinementMaterialForm(material, "600", "", "1200"))
        ));

        assertThat(result.successful()).isTrue();

        ArgumentCaptor<Refinement> refinementCaptor = ArgumentCaptor.forClass(Refinement.class);
        verify(this.refinementRepository).update(eq(3), refinementCaptor.capture());
        Refinement updated = refinementCaptor.getValue();

        assertThat(updated.getKey()).isEqualTo(3);
        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updated.getCost()).isEqualTo(200);
        assertThat(updated.getMaterials()).hasSize(1);
        assertThat(updated.getMaterials().values().iterator().next().getSellingPrice()).isEqualTo(1200);
        assertThat(updated.getMaterials().values().iterator().next().getQuality()).isZero();
    }

    @Test
    void findFormFailsForUnknownRefinement()
    {
        when(this.refinementRepository.findOne(99)).thenReturn(null);

        assertThatThrownBy(() -> this.refinementService.findForm(99))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unknown refinement key: 99");
    }

    @Test
    void removeDelegatesToRepository()
    {
        this.refinementService.remove(5);

        verify(this.refinementRepository).remove(5);
    }

    private static RefinedMaterial refinedMaterial(Material material, int amount, int quality, int sellingPrice)
    {
        return new RefinedMaterial()
            .setKey(UUID.randomUUID())
            .setBaseMaterial(material)
            .setAmount(amount)
            .setQuality(quality)
            .setSellingPrice(sellingPrice);
    }

    private static Material material(String key, String englishName)
    {
        Material material = mock(Material.class, RETURNS_DEEP_STUBS);
        when(material.getKey()).thenReturn(key);
        when(material.getName().getEnglish()).thenReturn(englishName);
        when(material.getReferences()).thenReturn(null);
        return material;
    }

    private static Material materialWithReference(String key, Material reference)
    {
        Material material = mock(Material.class, RETURNS_DEEP_STUBS);
        when(material.getKey()).thenReturn(key);
        when(material.getReferences()).thenReturn(reference);
        return material;
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(RefinementService.class)
    static class TestConfiguration
    {
    }
}
