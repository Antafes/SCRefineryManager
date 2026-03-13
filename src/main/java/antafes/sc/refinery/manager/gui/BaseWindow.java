package antafes.sc.refinery.manager.gui;

import antafes.sc.refinery.manager.Configuration;
import antafes.sc.refinery.manager.SCRefineryManager;
import antafes.sc.refinery.manager.gui.event.*;
import antafes.sc.refinery.manager.gui.newRefinement.NewRefinementDialog;
import antafes.utilities.language.LanguageInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class BaseWindow extends JFrame
{
    private final ApplicationContext applicationContext;
    private final Configuration configuration;
    private final LanguageInterface language;
    private JTabbedPane panel;
    private JMenu fileMenu;
    private JMenuItem closeMenuItem;
    private JMenuItem newMenuItem;

    public BaseWindow(@Autowired ApplicationContext applicationContext, @Autowired Configuration configuration)
    {
        this.applicationContext = applicationContext;
        this.configuration = configuration;
        this.language = this.configuration.getLanguageObject();
        this.initComponents();
        this.registerEvents();
        this.init();
        this.setFieldTexts();
    }

    private void initComponents()
    {
        this.createMenu();

        this.panel = new JTabbedPane();

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(this.panel, GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(this.panel, GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
        );

        pack();
    }

    private void createMenu()
    {
        JMenuBar menuBar = new JMenuBar();
        this.fileMenu = new JMenu();
        this.closeMenuItem = new JMenuItem();
        this.newMenuItem = new JMenuItem();

        this.fileMenu.add(this.newMenuItem);
        this.fileMenu.add(this.closeMenuItem);
        menuBar.add(this.fileMenu);

        this.newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        this.newMenuItem.addActionListener(e -> SCRefineryManager.getDispatcher().dispatch(new NewRefinementEvent()));

        this.closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        this.closeMenuItem.addActionListener(e -> SCRefineryManager.getDispatcher().dispatch(new CloseProgramEvent()));

        this.setJMenuBar(menuBar);
    }

    private void registerEvents()
    {
        SCRefineryManager.getDispatcher().addListener(
            CloseProgramEvent.class,
            new CloseProgramListener((closeProgramEvent) -> this.closeProgram())
        );
        SCRefineryManager.getDispatcher().addListener(
            RegisterEscapeCloseOperationEvent.class,
            new RegisterEscapeCloseOperationListener((event) -> this.registerEscapeCloseOperation(event.getDialog()))
        );
        SCRefineryManager.getDispatcher().addListener(
            NewRefinementEvent.class,
            new NewRefinementListener(newRefinementEvent -> this.openNewRefinementDialog())
        );
    }

    private void openNewRefinementDialog()
    {
        int x, y, width, height;

        NewRefinementDialog dialog = this.applicationContext.getBean(NewRefinementDialog.class, this, true);
        dialog.setVisible(false);

        width = dialog.getWidth();
        height = dialog.getHeight();
        x = this.configuration.getWindowLocation().x + (this.getWidth() / 2 - width / 2);
        y = this.configuration.getWindowLocation().y + (this.getHeight() / 2 - height / 2);

        dialog.setBounds(x, y, width, height);
        dialog.setVisible(true);
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
        this.configuration.saveProperties();
        System.exit(0);
    }

    private void setFieldTexts()
    {
        this.fileMenu.setText(this.language.translate("file"));
        this.fileMenu.setMnemonic(this.language.translate("fileMnemonic").charAt(0));
        this.newMenuItem.setText(this.language.translate("new"));
        this.newMenuItem.setMnemonic(this.language.translate("newMnemonic").charAt(0));
        this.closeMenuItem.setText(this.language.translate("quit"));
        this.closeMenuItem.setMnemonic(this.language.translate("quitMnemonic").charAt(0));
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
        this.setExtendedState(this.configuration.getExtendedState());
    }
}
