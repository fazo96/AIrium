/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.game.desktop;

import gui.GUI;
import javax.swing.UIManager;

/**
 *
 * @author fazo
 */
public class DesktopLauncher {

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        // Show menu bar to the top osx bar
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        /* Show application name correctly 
         However it doesn't work, see 
         http://stackoverflow.com/questions/3154638/setting-java-swing-application-name-on-mac */
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "AIrium");

        /*
         Setting the native OS look and feel. This way the program uses the OS's
         window toolkit instead of the Java one to render the application, if it
         is possible.
         */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.out.println("Unable to load native look and feel");
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GUI().setVisible(true);
            }
        });
    }
}
