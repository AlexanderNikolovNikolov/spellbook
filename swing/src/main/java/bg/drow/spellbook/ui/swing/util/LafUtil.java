package bg.drow.spellbook.ui.swing.util;

import com.google.common.collect.Lists;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

/**
 * A helper class to deal with look & feel management.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.4
 */
public class LafUtil {
//    private final transient static String[] names = {
//            "JGoodies Plastic",
//            "JGoodies PlasticXP",
//            "JGoodies Plastic3D",
//            "JGoodies Windows"};
//
//    private final transient static String[] classes = {
//            "com.jgoodies.looks.plastic.PlasticLookAndFeel",
//            "com.jgoodies.looks.plastic.PlasticXPLookAndFeel",
//            "com.jgoodies.looks.plastic.Plastic3DLookAndFeel",
//            "com.jgoodies.looks.windows.WindowsLookAndFeel"};

    public static List<UIManager.LookAndFeelInfo> getAvailableLookAndFeels() {
        final List<UIManager.LookAndFeelInfo> lookAndFeelInfos = Lists.newArrayList();

        // build the look and feel section
        lookAndFeelInfos.addAll(Arrays.asList(UIManager.getInstalledLookAndFeels()));

//        for (int i = 0; i < names.length; i++) {
//            lookAndFeelInfos.add(new UIManager.LookAndFeelInfo(names[i], classes[i]));
//        }

        return lookAndFeelInfos;
    }
}
