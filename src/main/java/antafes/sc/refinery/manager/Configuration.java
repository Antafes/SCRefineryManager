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
