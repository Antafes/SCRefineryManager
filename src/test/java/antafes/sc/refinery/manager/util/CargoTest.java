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

package antafes.sc.refinery.manager.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CargoTest.TestConfiguration.class)
class CargoTest
{
    @Test
    void formatFromCSCURoundsUpToNextWholeSCU()
    {
        assertThat(Cargo.formatFromCSCU(1)).isEqualTo("1 SCU");
        assertThat(Cargo.formatFromCSCU(100)).isEqualTo("1 SCU");
        assertThat(Cargo.formatFromCSCU(101)).isEqualTo("2 SCU");
        assertThat(Cargo.formatFromCSCU(199)).isEqualTo("2 SCU");
        assertThat(Cargo.formatFromCSCU(200)).isEqualTo("2 SCU");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestConfiguration
    {
    }
}
