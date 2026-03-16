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

package antafes.sc.refinery.manager.gui;

import antafes.sc.refinery.manager.Configuration;
import antafes.sc.refinery.manager.SCRefineryManager;
import antafes.sc.refinery.manager.gui.editRefinement.EditRefinementDialog;
import antafes.sc.refinery.manager.gui.event.*;
import antafes.sc.refinery.manager.gui.newRefinement.NewRefinementDialog;
import antafes.sc.refinery.manager.repository.RefinementRepository;
import antafes.utilities.language.LanguageInterface;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@org.springframework.stereotype.Component
public class BaseWindow extends JFrame
{
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private Configuration configuration;
    @Autowired
    private RefinementRepository refinementRepository;

    private LanguageInterface language;
    private JPanel panel;
    private TitledBorder refinementsBorder;
    private RefinementTable refinementsTable;
    private JMenu fileMenu;
    private JMenuItem closeMenuItem;
    private JMenuItem newMenuItem;

    private JMenu languageMenu;
    private JRadioButtonMenuItem languageEnglishItem;
    private JRadioButtonMenuItem languageGermanItem;

    @PostConstruct
    private void initAfterInjection()
    {
        this.language = this.configuration.getLanguageObject();
        this.initComponents();
        this.registerEvents();
        this.init();
        this.setFieldTexts();
    }

    private void initComponents()
    {
        this.createMenu();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {
                // Persist window state/size on shutdown.
                SCRefineryManager.getDispatcher().dispatch(new CloseProgramEvent());
            }
        });

        this.panel = new JPanel(new BorderLayout());
        this.refinementsBorder = BorderFactory.createTitledBorder("");
        this.panel.setBorder(this.refinementsBorder);
        this.refinementsTable = new RefinementTable(
            this,
            this.refinementRepository,
            this.language,
            (Configuration.Language) this.configuration.getLanguage()
        );

        JScrollPane refinementsScrollPane = new JScrollPane(this.refinementsTable);
        this.panel.add(refinementsScrollPane, BorderLayout.CENTER);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(this.panel, GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(this.panel, GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
        );

        pack();
    }

    private void createMenu()
    {
        JMenuBar menuBar = new JMenuBar();
        this.fileMenu = new JMenu();
        this.closeMenuItem = new JMenuItem();
        this.newMenuItem = new JMenuItem();

        this.languageMenu = new JMenu();
        this.languageEnglishItem = new JRadioButtonMenuItem();
        this.languageGermanItem = new JRadioButtonMenuItem();

        this.fileMenu.add(this.newMenuItem);
        this.fileMenu.add(this.closeMenuItem);
        menuBar.add(this.fileMenu);

        ButtonGroup languageGroup = new ButtonGroup();
        languageGroup.add(this.languageEnglishItem);
        languageGroup.add(this.languageGermanItem);

        this.languageEnglishItem.setIcon(Configuration.Language.ENGLISH.getIcon());
        this.languageGermanItem.setIcon(Configuration.Language.GERMAN.getIcon());

        this.languageMenu.add(this.languageEnglishItem);
        this.languageMenu.add(this.languageGermanItem);

        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(this.languageMenu);

        Configuration.Language currentLanguage = (Configuration.Language) this.configuration.getLanguage();
        this.languageMenu.setIcon(currentLanguage.getIcon());
        this.languageMenu.setText("");

        this.languageEnglishItem.setSelected(currentLanguage == Configuration.Language.ENGLISH);
        this.languageGermanItem.setSelected(currentLanguage == Configuration.Language.GERMAN);

        this.languageEnglishItem.addActionListener(_ -> this.switchLanguage(Configuration.Language.ENGLISH));
        this.languageGermanItem.addActionListener(_ -> this.switchLanguage(Configuration.Language.GERMAN));

        this.newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        this.newMenuItem.addActionListener(_ -> SCRefineryManager.getDispatcher().dispatch(new NewRefinementEvent()));

        this.closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        this.closeMenuItem.addActionListener(_ -> SCRefineryManager.getDispatcher().dispatch(new CloseProgramEvent()));

        this.setJMenuBar(menuBar);
    }

    private void registerEvents()
    {
        SCRefineryManager.getDispatcher().addListener(
            CloseProgramEvent.class,
            new CloseProgramListener(_ -> this.closeProgram())
        );
        SCRefineryManager.getDispatcher().addListener(
            RegisterEscapeCloseOperationEvent.class,
            new RegisterEscapeCloseOperationListener((event) -> this.registerEscapeCloseOperation(event.getDialog()))
        );
        SCRefineryManager.getDispatcher().addListener(
            NewRefinementEvent.class,
            new NewRefinementListener(_ -> this.openNewRefinementDialog())
        );
        SCRefineryManager.getDispatcher().addListener(
            EditRefinementEvent.class,
            new EditRefinementListener(event -> this.openEditRefinementDialog(event.getKey()))
        );
    }

    private void openNewRefinementDialog()
    {
        NewRefinementDialog dialog = this.applicationContext.getBean(NewRefinementDialog.class, this, true);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        this.refreshRefinementsTable();
    }

    private void openEditRefinementDialog(Integer key)
    {
        EditRefinementDialog dialog = this.applicationContext.getBean(EditRefinementDialog.class, this, true, key);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        this.refreshRefinementsTable();
    }

    private void registerEscapeCloseOperation(JDialog dialog)
    {
        Action dispatchClosing = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dialog.dispatchEvent(new WindowEvent(
                    dialog, WindowEvent.WINDOW_CLOSING
                ));
            }
        };
        KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        String dispatchWindowClosingActionMapKey = "com.spodding.tackline.dispatch:WINDOW_CLOSING";
        JRootPane root = dialog.getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            escapeStroke, dispatchWindowClosingActionMapKey
        );
        root.getActionMap().put(dispatchWindowClosingActionMapKey, dispatchClosing);
    }

    private void closeProgram()
    {
        this.configuration.setWindowLocation(this.getLocationOnScreen());
        this.configuration.setExtendedState(this.getExtendedState());
        if ((this.getExtendedState() & Frame.MAXIMIZED_BOTH) == 0) {
            this.configuration.setWindowSize(this.getSize());
        }
        this.configuration.saveProperties();
        System.exit(0);
    }

    private void setFieldTexts()
    {
        this.setTitle(this.language.translate("title"));
        this.refinementsBorder.setTitle(this.language.translate("refinements"));
        this.panel.repaint();

        this.fileMenu.setText(this.language.translate("file"));
        this.fileMenu.setMnemonic(this.language.translate("fileMnemonic").charAt(0));
        this.newMenuItem.setText(this.language.translate("new"));
        this.newMenuItem.setMnemonic(this.language.translate("newMnemonic").charAt(0));
        this.closeMenuItem.setText(this.language.translate("quit"));
        this.closeMenuItem.setMnemonic(this.language.translate("quitMnemonic").charAt(0));

        Configuration.Language currentLanguage = (Configuration.Language) this.configuration.getLanguage();
        this.languageMenu.setIcon(currentLanguage.getIcon());
        this.languageMenu.setText("");

        this.languageEnglishItem.setText(this.language.translate("english"));
        this.languageGermanItem.setText(this.language.translate("german"));

        this.languageEnglishItem.setSelected(currentLanguage == Configuration.Language.ENGLISH);
        this.languageGermanItem.setSelected(currentLanguage == Configuration.Language.GERMAN);
    }

    private void init()
    {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(BaseWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.setLocation(this.configuration.getWindowLocation());

        Dimension windowSize = this.configuration.getWindowSize();
        if (windowSize != null && windowSize.width > 0 && windowSize.height > 0) {
            this.setSize(windowSize);
        }

        this.setExtendedState(this.configuration.getExtendedState());
    }

    private void switchLanguage(Configuration.Language language)
    {
        if (language == null) {
            return;
        }

        this.configuration.setLanguage(language);
        this.configuration.saveProperties();

        this.language = this.configuration.getLanguageObject();

        SCRefineryManager.getDispatcher().dispatch(new LanguageChangedEvent(this.language, language));

        this.setFieldTexts();
    }

    public void refreshRefinementsTable()
    {
        this.refinementsTable.refreshData();
    }
}
