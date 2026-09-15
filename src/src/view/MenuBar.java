package view;

import javax.swing.*;

/**
 * Menu bar for the route planner application.
 * Provides file management and help-related menu options.
 *
 * @author Chene Van der Walt
 * @version Spring 2026
 */
public class MenuBar extends JMenuBar {

    /** File menu containing route save and load options. */
    private JMenu myFile;

    /** Help menu containing application information and instructions. */
    private JMenu myHelp;

    /** Menu item used to save a route. */
    private JMenuItem mySaveRoute;

    /** Menu item used to load a previously saved route. */
    private JMenuItem myLoadRoute;

    /** Menu item displaying application information. */
    private JMenuItem myAbout;

    /** Menu item used to exit the application. */
    private JMenuItem myExit;

    /** Menu item displaying application instructions. */
    private JMenuItem myInstructions;

    /** Menu item used to delete a previously saved route. */
    private JMenuItem myDeleteRoute;

    /**
     * Constructs the application menu bar and initializes all menus
     * and menu items.
     */
    public MenuBar() {

        myFile = new JMenu("File");
        myHelp = new JMenu("Help");

        mySaveRoute = new JMenuItem("Save Route");
        myLoadRoute = new JMenuItem("Load Route");
        // in constructor:
        myDeleteRoute = new JMenuItem("Delete Route");


        myAbout = new JMenuItem("About");
        myExit = new JMenuItem("Exit");
        myInstructions = new JMenuItem("Instructions");

        myFile.add(mySaveRoute);
        myFile.add(myLoadRoute);
        myFile.add(myDeleteRoute);
        myHelp.add(myAbout);
        myHelp.add(myInstructions);
        myHelp.add(myExit);

        add(myFile);
        add(myHelp);

        setVisible(true);

    }

    /**
     * Returns the Save Route menu item.
     *
     * @return the Save Route menu item
     */
    public JMenuItem getSaveRoute() {
        return mySaveRoute;
    }

    /**
     * Returns the Delete Route menu item.
     *
     * @return the Delete Route menu item
     */
    public JMenuItem getDeleteRoute() { return myDeleteRoute; }

    /**
     * Returns the About menu item.
     *
     * @return the About menu item
     */
    public JMenuItem getAbout() {
        return myAbout;
    }

    /**
     * Returns the Load Route menu item.
     *
     * @return the Load Route menu item
     */
    public JMenuItem getLoadRoute() {
        return myLoadRoute;
    }

    /**
     * Returns the Instructions menu item.
     *
     * @return the Instructions menu item
     */
    public JMenuItem getInstructions() {
        return myInstructions;
    }

    /**
     * Returns the Exit menu item.
     *
     * @return the Exit menu item
     */
    public JMenuItem getExit() {
        return myExit;
    }
}
