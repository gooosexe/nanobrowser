package org.nanobrowser;

import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;

import javax.swing.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedPlatformException, CefInitializationException, IOException, InterruptedException {
        JFrame frame = new MainFrame();
        frame.setVisible(true);
    }
}
