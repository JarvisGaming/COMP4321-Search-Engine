package hk.ust.cse.comp4321.project.util;

import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URL;


public class URIUtil {
    public static @Nullable URL toURLWithoutFragment(String urlString) {
        try {
            URI uri = new URI(urlString);
            return new URI(uri.getScheme(), uri.getRawSchemeSpecificPart(), null).toURL();
        } catch (Exception ignored) {
            return null;
        }
    }
}
