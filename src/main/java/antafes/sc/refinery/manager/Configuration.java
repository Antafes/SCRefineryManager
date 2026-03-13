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

import antafes.sc.refinery.manager.language.English;
import antafes.utilities.BaseConfiguration;
import antafes.utilities.Utilities;
import lombok.Getter;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.*;

@Service()
public class Configuration extends BaseConfiguration
{
    private static final String DATA_PATH = "data/";

    public Configuration()
    {
        this.loadProperties();
    }

    @Override
    public String getBasePath()
    {
        return System.getProperty("user.home") + "/.screfman/";
    }

    @Override
    public LanguageInterface getLanguage()
    {
        return Language.valueOf(this.getProperties().getProperty("language", Language.ENGLISH.toString()));
    }

    @Override
    public void loadProperties()
    {
        super.loadProperties();

        if (!this.getProperties().containsKey("language")) {
            this.setLanguage(Language.ENGLISH);
        }
    }

    public static String getDataPath()
    {
        return DATA_PATH;
    }

    @Getter
    public enum Language implements LanguageInterface
    {
        ENGLISH(English.class.getName(), "English", "images/english.png");

        private final String languageString;
        private final String name;
        private final ImageIcon icon;

        Language(String languageString, String name, String iconPath) {
            this.languageString = languageString;
            this.name = name;
            Toolkit kit = Toolkit.getDefaultToolkit();
            Image img = kit.createImage(Utilities.getResourceInJar(iconPath));
            this.icon = new ImageIcon(img);
        }
    }
}
