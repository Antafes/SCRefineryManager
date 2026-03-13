package antafes.sc.refinery.manager.gui.newRefinement;

import antafes.sc.refinery.manager.Configuration;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;

@Component
public class NewRefinementPanel extends JPanel
{
    @Autowired
    private Configuration configuration;

    @PostConstruct
    private void initAfterInjection()
    {
        this.initComponents();
        this.setFieldTexts();
    }

    private void initComponents()
    {
    }

    private void setFieldTexts()
    {
    }
}
