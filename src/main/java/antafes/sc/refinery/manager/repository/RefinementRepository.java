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

import antafes.sc.base.repository.BaseRepository;
import antafes.sc.base.repository.MaterialRepository;
import antafes.sc.refinery.manager.Configuration;
import antafes.sc.refinery.manager.entity.Refinement;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Repository
public class RefinementRepository extends BaseRepository<Integer, Refinement>
{
    private final Map<Integer, Refinement> refinements = new HashMap<>();
    @Autowired
    private Configuration configuration;
    @Autowired
    private MaterialRepository materialRepository;

    @Override
    public Map<Integer, Refinement> findAll()
    {
        return this.refinements;
    }

    @Override
    public Refinement findOne(Integer key)
    {
        return this.refinements.get(key);
    }

    public void saveData()
    {
        try {
            File productionsFile = new File(this.configuration.getBasePath() + "refinements.xml");
            this.checkFileExists(productionsFile);

            Marshaller marshaller = JAXBContext.newInstance(RefinementListWrapper.class).createMarshaller();
            RefinementListWrapper list = new RefinementListWrapper();
            this.refinements.forEach((_, production) -> list.refinements.add(production));
            marshaller.marshal(list, productionsFile);
        } catch (IOException | JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add a new refinement to the repository and persist the change.
     * If the refinement has no key set, a new unique key will be assigned.
     */
    public synchronized void add(Refinement refinement)
    {
        if (refinement == null) return;
        if (refinement.getKey() == null) {
            int key = this.refinements.keySet().stream().mapToInt(i -> i).max().orElse(0) + 1;
            refinement.setKey(key);
        }
        this.refinements.put(refinement.getKey(), refinement);
        saveData();
    }

    /**
     * Update an existing refinement and persist the change.
     */
    public synchronized void update(Integer key, Refinement refinement)
    {
        if (key == null || refinement == null) return;
        if (!this.refinements.containsKey(key)) {
            throw new IllegalArgumentException("Unknown refinement key: " + key);
        }

        refinement.setKey(key);
        this.refinements.put(key, refinement);
        saveData();
    }

    /**
     * Remove the refinement identified by the given key and persist the updated repository state.
     * The method fails fast when the key does not exist so callers do not silently delete nothing.
     */
    public synchronized void remove(Integer key)
    {
        if (!this.refinements.containsKey(key)) {
            throw new IllegalArgumentException("Unknown refinement key: " + key);
        }

        this.refinements.remove(key);
        saveData();
    }

    @Override
    protected void loadData()
    {
        try {
            File refinementsFile = new File(this.configuration.getBasePath() + "refinements.xml");
            this.checkFileExists(refinementsFile);
            Unmarshaller unmarshaller = JAXBContext.newInstance(RefinementListWrapper.class).createUnmarshaller();
            RefinementListWrapper wrapper = (RefinementListWrapper) unmarshaller.unmarshal(new FileInputStream(refinementsFile));
            wrapper.refinements.forEach(refinement -> {
                refinement.getMaterials()
                    .forEach((_, refinedMaterial) -> refinedMaterial.setBaseMaterial(this.materialRepository.findOne(refinedMaterial.getBaseMaterial().getKey())));
                this.refinements.put(refinement.getKey(), refinement);
            });
        } catch (JAXBException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkFileExists(File refinementsFile) throws IOException
    {
        if (!refinementsFile.exists()) {
            if (!refinementsFile.getParentFile().exists()) {
                //noinspection ResultOfMethodCallIgnored
                refinementsFile.getParentFile().mkdirs();
            }

            //noinspection ResultOfMethodCallIgnored
            refinementsFile.createNewFile();
            //noinspection ResultOfMethodCallIgnored
            refinementsFile.setWritable(true);
            List<String> lines = Arrays.asList(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                "<refinements>",
                "</refinements>"
            );
            Files.write(refinementsFile.toPath(), lines, java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    @XmlRootElement(name = "refinements")
    private static class RefinementListWrapper
    {
        @XmlElement(name = "refinement")
        private final List<Refinement> refinements = new ArrayList<>();
    }
}
