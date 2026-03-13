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

import antafes.sc.refinery.manager.Configuration;
import antafes.sc.refinery.manager.entity.RefinedMaterial;
import antafes.sc.refinery.manager.entity.Refinement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RefinementRepositoryTest
{
    @TempDir
    Path tempDir;

    @Test
    void addPersistsAndLoadsRefinementData() throws Exception
    {
        Configuration configuration = mock(Configuration.class);
        when(configuration.getBasePath()).thenReturn(this.tempDir.toString() + File.separator);

        RefinementRepository repository = new RefinementRepository();
        ReflectionTestUtils.setField(repository, "configuration", configuration);

        UUID materialKey = UUID.randomUUID();
        Map<UUID, RefinedMaterial> materials = new HashMap<>();
        materials.put(materialKey, new RefinedMaterial()
            .setKey(materialKey)
            .setAmount(200)
            .setQuality(95)
            .setSellingPrice(700));

        repository.add(new Refinement()
            .setMaterials(materials)
            .setCost(300));

        Path refinementFile = this.tempDir.resolve("refinements.xml");
        assertThat(refinementFile).exists();
        assertThat(Files.readString(refinementFile))
            .contains("<refinements>")
            .contains("<refinement")
            .contains("<materials>")
            .doesNotContain("HashMap");

        RefinementRepository loadedRepository = new RefinementRepository();
        ReflectionTestUtils.setField(loadedRepository, "configuration", configuration);
        loadedRepository.loadData();

        assertThat(loadedRepository.findAll()).hasSize(1);

        Refinement loadedRefinement = loadedRepository.findOne(1);
        assertThat(loadedRefinement.getCost()).isEqualTo(300);
        assertThat(loadedRefinement.getMaterials()).containsKey(materialKey);
        assertThat(loadedRefinement.getMaterials().get(materialKey).getAmount()).isEqualTo(200);
        assertThat(loadedRefinement.getMaterials().get(materialKey).getQuality()).isEqualTo(95);
    }
}
