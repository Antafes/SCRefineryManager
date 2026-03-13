package antafes.sc.refinery.manager.gui.newRefinement;

import antafes.sc.refinery.manager.Configuration;
import antafes.sc.refinery.manager.SCRefineryManager;
import antafes.sc.refinery.manager.gui.event.RegisterEscapeCloseOperationEvent;
import antafes.utilities.language.LanguageInterface;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import javax.swing.*;
import java.awt.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NewRefinementDialog extends JDialog
{
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private Configuration configuration;
    private LanguageInterface language;

    public NewRefinementDialog(Frame owner, @NonNull Boolean modal)
    {
        super(owner, modal);
    }

    @PostConstruct
    private void initAfterInjection()
    {
        this.language = configuration.getLanguageObject();
        this.initComponents();
        this.setFieldTexts();
    }

    private void initComponents()
    {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        SCRefineryManager.getDispatcher().dispatch(new RegisterEscapeCloseOperationEvent(this));

        NewRefinementPanel panel = this.applicationContext.getBean(NewRefinementPanel.class);
        GridBagLayout layout = new GridBagLayout();
        this.setLayout(layout);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.ipady = 2;
        constraints.insets.set(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTHWEST;

        this.add(panel, constraints);
    }

    private void setFieldTexts()
    {
        this.setTitle(this.language.translate("newRefinement"));
    }
}
