package bg.drow.spellbook.ui.swing.util;

import javax.swing.ImageIcon;

/**
 * User: bozhidar
 * Date: Sep 7, 2009
 * Time: 10:39:28 PM
 */
public class IconManager {
    public static ImageIcon getImageIcon(String filename, IconSize iconSize) {
        String iconPath = "/icons/";

        switch (iconSize) {
            case SIZE16:
                iconPath += "16x16/";
                break;
            case SIZE24:
                iconPath += "24x24/";
                break;
            case SIZE32:
                iconPath += "32x32/";
                break;
            case SIZE48:
                iconPath += "48x48/";
                break;
        }

        iconPath += filename;

        if (IconManager.class.getResource(iconPath) == null) {
            return null;
        }

        return new ImageIcon(IconManager.class.getResource(iconPath));
    }

    public static ImageIcon getMenuIcon(String filename) {
        return getImageIcon(filename, IconSize.SIZE16);
    }

    public static enum IconSize {
        SIZE16, SIZE24, SIZE32, SIZE48
    }
}
