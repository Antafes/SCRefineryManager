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

package antafes.sc.refinery.manager;

import antafes.eventDispatcher.Application;
import antafes.sc.base.repository.MaterialRepository;
import antafes.sc.refinery.manager.gui.BaseWindow;
import antafes.sc.refinery.manager.gui.Splash;
import antafes.sc.refinery.manager.repository.RefinementRepository;
import antafes.utilities.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import javax.swing.*;
import java.awt.*;

@SpringBootApplication
@ComponentScan({
    "antafes.sc.refinery.manager",
    "antafes.sc.refinery.manager.service",
    "antafes.sc.refinery.manager.repository",
    "antafes.sc.base.repository"
})
public class SCRefineryManager extends Application implements CommandLineRunner
{
    @Autowired
    private ApplicationContext applicationContext;

    static void main(String[] args)
    {
        new SpringApplicationBuilder(SCRefineryManager.class).headless(false).run(args);
    }

    @Override
    public void run(String ... args) throws Exception
    {
        this.openBaseWindow();
    }

    private void openBaseWindow()
    {
        SwingUtilities.invokeLater(() -> {
            BaseWindow baseWindow = this.applicationContext.getBean(BaseWindow.class);
            new Splash(Utilities.getResourceInJar("images/splash.png"), 2000, baseWindow);
            this.warmUp();
            Toolkit kit = Toolkit.getDefaultToolkit();
            Image img = kit.createImage(Utilities.getResourceInJar("images/logo.png"));
            baseWindow.setIconImage(img);
        });
    }

    private void warmUp()
    {
        MaterialRepository materialRepository = this.applicationContext.getBean(MaterialRepository.class);
        materialRepository.init();
        RefinementRepository refinementRepository = this.applicationContext.getBean(RefinementRepository.class);
        refinementRepository.init();
    }
}
