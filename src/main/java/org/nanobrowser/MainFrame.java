package org.nanobrowser;

import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefFocusHandlerAdapter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class MainFrame extends JFrame {
    JFrame frame;
    JTabbedPane tabbedPane;
    BorderLayout borderLayout;
    JPanel topPanel;

    CefAppBuilder cefAppBuilder;
    CefApp cefApp;

    InputMap inputMap;
    ActionMap actionMap;

    boolean browserFocus;
    String startURL = "https://www.google.com";


    public MainFrame() throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedPlatformException, CefInitializationException, IOException, InterruptedException {
        createCefApp();
        setTitle("NanoBrowser");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setFocusable(true);
        requestFocusInWindow();
        // set look and feel to os
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        createCefClient();
        add(createMainPanel());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                CefApp.getInstance().dispose();
                dispose();
            }
        });
    }

    /**
     * Creates the main JPanel with a JTabbedPane.
     * @return JPanel with JTabbedPane
     */
    public JPanel createMainPanel() {
        borderLayout = new BorderLayout();
        topPanel = new JPanel();
        tabbedPane = new JTabbedPane();
        topPanel.setLayout(borderLayout);

        tabbedPane.setFocusable(true);
        tabbedPane.requestFocusInWindow();

        topPanel.add(tabbedPane, BorderLayout.CENTER);

        addTab("New Tab", createTabPanel());

        return topPanel;
    }

    /**
     * Adds a tab.
     */
    public void addTab(String title, JPanel body) {
        tabbedPane.addTab(title, body);
        int index = tabbedPane.getTabCount() - 1;
        JPanel tabPanel = new JPanel(new GridBagLayout());
        tabPanel.setOpaque(false);
        JLabel tabLabel = new JLabel(title);
        JButton closeButton = new JButton("x");

        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1;
        tabPanel.add(tabLabel, gridBagConstraints);

        gridBagConstraints.gridx++;
        gridBagConstraints.weightx = 0;
        tabPanel.add(closeButton, gridBagConstraints);

        tabbedPane.setTabComponentAt(index, tabPanel);

        closeButton.addActionListener(e -> tabbedPane.remove(tabbedPane.getSelectedComponent()));
    }

    /**
     * Creates a JPanel with a CefClient in a tab.
     * @return JPanel with CefClient
     */
    public JPanel createTabPanel() {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        JPanel tabContentPanel = new JPanel(new GridBagLayout());
        CefClient cefClient = createCefClient();

        CefBrowser cefBrowser = cefClient.createBrowser(startURL, false, false);
        Component browserUI = cefBrowser.getUIComponent();
        browserUI.setFocusable(true);

        // implement url changing neatly
        JTextField urlBar = new JTextField(startURL);
        JButton backButton = new JButton("<");
        JButton forwardButton = new JButton(">");
        JButton reloadButton = new JButton("R");

        gridBagConstraints.insets = new Insets(1, 1, 1, 1);

        backButton.addActionListener(e -> cefBrowser.goBack());
        forwardButton.addActionListener(e -> cefBrowser.goForward());
        reloadButton.addActionListener(e -> cefBrowser.reload());
        urlBar.addActionListener(e -> {
            if (urlBar.getText().startsWith("http://") || urlBar.getText().startsWith("https://")) {
                cefBrowser.loadURL(urlBar.getText());
            } else {
                cefBrowser.loadURL("https://" + urlBar.getText());
            }
            cefBrowser.setFocus(true);
        });
        cefClient.addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
                super.onAddressChange(browser, frame, url);
                urlBar.setText(url);
            }
        });

        inputMap = topPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        actionMap = topPanel.getActionMap();
        KeyStroke keyStroke = KeyStroke.getKeyStroke("control T");

        inputMap.put(keyStroke, "newTab");
        actionMap.put("newTab", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("new tab");
                addTab("New Tab", createTabPanel());
            }
        });

        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridx = 0;
        tabContentPanel.add(backButton, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        tabContentPanel.add(forwardButton, gridBagConstraints);

        gridBagConstraints.gridx = 2;
        tabContentPanel.add(reloadButton, gridBagConstraints);

        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 3;
        tabContentPanel.add(urlBar, gridBagConstraints);

        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        tabContentPanel.add(browserUI, gridBagConstraints);

        return tabContentPanel;
    }

    /**
     * Creates a CefAppBuilder and sets the CefSettings.
     * @throws UnsupportedPlatformException if the platform is not supported
     * @throws CefInitializationException if the CefApp can't be initialized
     * @throws IOException if the CefApp can't be created
     * @throws InterruptedException if the CefApp can't be created
     */
    public void createCefApp() throws UnsupportedPlatformException, CefInitializationException, IOException, InterruptedException {
        cefAppBuilder = new CefAppBuilder();

        cefAppBuilder.getCefSettings().windowless_rendering_enabled = false; //Default - select OSR mode
        cefAppBuilder.setAppHandler(new MavenCefAppHandlerAdapter() {
            @Override
            public void stateHasChanged(org.cef.CefApp.CefAppState state) {
                if (state == CefApp.CefAppState.TERMINATED) {
                    System.exit(0);
                }
            }
        });

        cefApp = cefAppBuilder.build();
    }

    /**
     * Creates a CefClient and adds a CefDisplayHandlerAdapter to it.
     * @return Component for browserUI
     */
    public CefClient createCefClient() {
        CefClient cefClient = cefApp.createClient();

        cefClient.addFocusHandler(new CefFocusHandlerAdapter(){
            @Override
            public void onGotFocus(CefBrowser browser) {
                if (browserFocus) return;
                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
                browserFocus = true;
                browser.setFocus(true);
            }

            @Override
            public void onTakeFocus(CefBrowser browser, boolean next) {
                browserFocus = false;
            }
        });

        CefMessageRouter msgRouter = CefMessageRouter.create();
        cefClient.addMessageRouter(msgRouter);

        return cefClient;
    }
}