package org.workcraft;

import org.workcraft.Version.Status;
import org.workcraft.plugins.builtin.settings.DebugCommonSettings;

import java.util.Calendar;

public class Info {

    private static final String title = "Workcraft";
    private static final String subtitle1 = "A New Hope";
    private static final String subtitle2 = "Metastability Strikes Back";
    private static final String subtitle3 = "Return of the Hazard";
    private static final String subtitle4 = "Revenge of the Timing Assumption";

    private static final Version version = new Version(3, 3, 9, Status.ALPHA);

    private static final int startYear = 2006;
    private static final int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    private static final String organisation = "Newcastle University";
    private static final String homepage = "https://workcraft.org/";
    private static final String email = "support@workcraft.org";

    public static Version getVersion() {
        return version;
    }

    public static String getTitle() {
        return title + " " + version.major;
    }

    public static String getSubtitle() {
        switch (version.major) {
        case 1: return subtitle1;
        case 2: return subtitle2;
        case 3: return subtitle3;
        case 4: return subtitle4;
        default: return "";
        }
    }

    public static String getFullTitle() {
        return getTitle() + " (" + getSubtitle() + "), version " + getVersion();
    }

    public static String getCopyright() {
        return "Copyright " + startYear + "-" + currentYear + " " + organisation;
    }

    public static String getHomepage() {
        return homepage;
    }

    public static String getEmail() {
        return email;
    }

    public static String getGeneratedByText(String prefix, String suffix) {
        String info = DebugCommonSettings.getShortExportHeader() ? getTitle() : getFullTitle();
        return prefix + "generated by " + info + suffix;
    }

}
