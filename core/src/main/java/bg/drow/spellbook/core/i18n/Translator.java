package bg.drow.spellbook.core.i18n;

import java.util.*;

/**
 * A simple wrapper around resource bundles, useful for translation purposes.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.1
 */
public class Translator {
    private String resourceBundleName;
    private ResourceBundle resourceBundle;

    private static Map<String, Translator> translators = new HashMap<String, Translator>();

    private Translator(final String resourceBundleName) {
        this.resourceBundleName = resourceBundleName;
        resourceBundle = ResourceBundle.getBundle("i18n/" + resourceBundleName, Locale.getDefault());

        if (resourceBundle == null) {
            throw new IllegalArgumentException("No such resource bundle - " + resourceBundleName);
        }
    }

    public String translate(String resourceKey) {
        if (resourceKey == null) {
            throw new IllegalArgumentException("Null resource key");
        }

        // prevent nasty exceptions from missing resources
        try {
            return resourceBundle.getString(resourceKey);
        } catch (MissingResourceException ex) {
            return resourceKey;
        }
    }

    public String translate(String resourceKey, Object... formatArgs) {
        return String.format(translate(resourceKey), formatArgs);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Translator other = (Translator) obj;
        if (this.resourceBundle != other.resourceBundle && (this.resourceBundle == null || !this.resourceBundle.equals(other.resourceBundle))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.resourceBundle != null ? this.resourceBundle.hashCode() : 0);
        return hash;
    }

    public static Translator getTranslator(final String resourceBundleName) {
        if (translators.containsKey(resourceBundleName)) {
            return translators.get(resourceBundleName);
        } else {
            Translator t = new Translator(resourceBundleName);
            translators.put(resourceBundleName, t);
            return t;
        }
    }

    public void reset() {
        resourceBundle = ResourceBundle.getBundle("i18n/" + resourceBundleName, Locale.getDefault());
    }
}
