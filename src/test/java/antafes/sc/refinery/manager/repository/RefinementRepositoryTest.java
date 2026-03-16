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

package antafes.sc.refinery.manager.repository;

import antafes.sc.base.entity.Material;
import antafes.sc.base.repository.MaterialRepository;
import antafes.sc.refinery.manager.Configuration;
import antafes.sc.refinery.manager.entity.RefinedMaterial;
import antafes.sc.refinery.manager.entity.Refinement;
import antafes.sc.refinery.manager.util.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = RefinementRepositoryTest.TestConfiguration.class)
class RefinementRepositoryTest
{
    @MockitoBean
    private Configuration configuration;

    @MockitoBean
    private MaterialRepository materialRepository;

    @Autowired
    private TestRefinementRepository refinementRepository;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp()
    {
        when(configuration.getBasePath()).thenReturn(this.tempDir.toString() + File.separator);
        this.refinementRepository.findAll().clear();
    }

    @Test
    void currencyFormattingDependsOnAppLanguageNotUserLocale()
    {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);

            assertThat(Currency.format(9_999_999, Configuration.Language.ENGLISH))
                .isEqualTo("9,999,999 aUEC");

            assertThat(Currency.format(9_999_999))
                .isEqualTo("9,999,999 aUEC");
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    void addPersistsAndLoadsRefinementData() throws Exception
    {
        String baseMaterialKey = UUID.randomUUID().toString();
        Material baseMaterial = materialWithKey(baseMaterialKey);
        when(this.materialRepository.findOne(baseMaterialKey)).thenReturn(baseMaterial);

        UUID refinedMaterialKey = UUID.randomUUID();
        Map<UUID, RefinedMaterial> materials = new HashMap<>();
        materials.put(refinedMaterialKey, new RefinedMaterial()
            .setKey(refinedMaterialKey)
            .setBaseMaterial(baseMaterial)
            .setAmount(200)
            .setQuality(95)
            .setSellingPrice(700));

        ZonedDateTime createdAt = ZonedDateTime.of(2026, 3, 16, 15, 12, 0, 0, ZoneOffset.ofHours(2));
        ZonedDateTime expectedUtc = createdAt.withZoneSameInstant(ZoneOffset.UTC);

        this.refinementRepository.add(new Refinement()
            .setCreatedAt(createdAt)
            .setMaterials(materials)
            .setCost(300));

        Path refinementFile = this.tempDir.resolve("refinements.xml");
        assertThat(refinementFile).exists();
        assertThat(Files.readString(refinementFile))
            .contains("<refinements")
            .containsPattern("<refinement(\\s|/|>)")
            .contains("<materials")
            .contains("2026-03-16T13:12Z")
            .doesNotContain("HashMap");

        // Simulate a reload from disk.
        this.refinementRepository.findAll().clear();
        this.refinementRepository.loadDataFromDisk();

        assertThat(this.refinementRepository.findAll()).hasSize(1);

        Refinement loadedRefinement = this.refinementRepository.findOne(1);
        assertThat(loadedRefinement.getCost()).isEqualTo(300);
        assertThat(loadedRefinement.getCreatedAt()).isEqualTo(expectedUtc);
        assertThat(loadedRefinement.getCreatedAt().toInstant()).isEqualTo(createdAt.toInstant());
        assertThat(loadedRefinement.getMaterials()).containsKey(refinedMaterialKey);
        assertThat(loadedRefinement.getMaterials().get(refinedMaterialKey).getAmount()).isEqualTo(200);
        assertThat(loadedRefinement.getMaterials().get(refinedMaterialKey).getQuality()).isEqualTo(95);
        assertThat(loadedRefinement.getMaterials().get(refinedMaterialKey).getBaseMaterial().getKey()).isEqualTo(baseMaterialKey);
    }

    @Test
    void removePersistsDeletion() throws Exception
    {
        String baseMaterialKey = UUID.randomUUID().toString();
        Material baseMaterial = materialWithKey(baseMaterialKey);
        when(this.materialRepository.findOne(baseMaterialKey)).thenReturn(baseMaterial);

        UUID refinedMaterialKey = UUID.randomUUID();
        Map<UUID, RefinedMaterial> materials = new HashMap<>();
        materials.put(refinedMaterialKey, new RefinedMaterial()
            .setKey(refinedMaterialKey)
            .setBaseMaterial(baseMaterial)
            .setAmount(200)
            .setQuality(95)
            .setSellingPrice(700));

        this.refinementRepository.add(new Refinement()
            .setMaterials(materials)
            .setCost(150));

        this.refinementRepository.remove(1);

        // Reload from disk.
        this.refinementRepository.findAll().clear();
        this.refinementRepository.loadDataFromDisk();

        assertThat(this.refinementRepository.findAll()).isEmpty();
        assertThat(Files.readString(this.tempDir.resolve("refinements.xml")))
            .doesNotContainPattern("<refinement(\\s|/|>)");
    }

    private static Material materialWithKey(String key) throws Exception
    {
        Material material = Material.class.getDeclaredConstructor().newInstance();

        try {
            ReflectionTestUtils.setField(material, "key", key);
        } catch (IllegalArgumentException ignored) {
            ReflectionTestUtils.setField(material, "id", key);
        }

        return material;
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestConfiguration {
        @Bean
        TestRefinementRepository refinementRepository()
        {
            return new TestRefinementRepository();
        }
    }

    static class TestRefinementRepository extends RefinementRepository
    {
        @Override
        protected void loadData()
        {
        }

        void loadDataFromDisk()
        {
            super.loadData();
        }
    }
}
