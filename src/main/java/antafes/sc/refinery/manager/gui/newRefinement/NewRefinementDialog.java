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

package antafes.sc.refinery.manager.gui.newRefinement;

import antafes.sc.refinery.manager.Configuration;
import antafes.sc.refinery.manager.SCRefineryManager;
import antafes.sc.refinery.manager.gui.event.RegisterEscapeCloseOperationEvent;
import antafes.utilities.language.LanguageInterface;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
    // Dialog buttons
    private JButton cancelButton;
    private JButton saveButton;

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
        constraints.gridwidth = 2;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.ipady = 2;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTHWEST;

        this.add(panel, constraints);

        constraints.gridy++;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        this.cancelButton = new JButton();
        this.cancelButton.setActionCommand("cancel");
        this.cancelButton.addActionListener(_ -> dispose());
        this.saveButton = new JButton();
        this.saveButton.setActionCommand("save");
        this.saveButton.addActionListener(_ -> SCRefineryManager.getDispatcher().dispatch(new antafes.sc.refinery.manager.gui.event.SaveRefinementEvent(this)));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonsPanel.add(this.saveButton);
        buttonsPanel.add(this.cancelButton);
        this.add(buttonsPanel, constraints);

        pack();
    }

    private void setFieldTexts()
    {
        this.setTitle(this.language.translate("newRefinement"));
        this.cancelButton.setText(this.language.translate("cancel"));
        this.saveButton.setText(this.language.translate("save"));
    }
}
