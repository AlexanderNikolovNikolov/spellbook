package bg.drow.spellbook.core.model;

/**
 * This class represents the application's current version.
 * It has three components - major version, minor version and revision.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.4
 */
public class Version implements Comparable<Version> {
    private int major;
    private int minor;
    private int revision;

    public Version(String versionString) {
        String[] version = versionString.split("\\.");

        if (version.length != 3) {
            throw new IllegalArgumentException("Invalid version string format");
        }

        major = Integer.parseInt(version[0]);
        minor = Integer.parseInt(version[1]);
        revision = Integer.parseInt(version[2]);
    }

    @Override
    public int compareTo(Version other) {
        if ((major < other.major) ||
                (major == other.major && minor < other.minor) ||
                (major == other.major && minor == other.minor && revision < other.revision)) {
            return -1;
        } else if (major == other.major && minor == other.minor && revision == other.revision) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + revision;
    }
}
