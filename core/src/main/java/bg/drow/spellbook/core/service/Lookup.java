package bg.drow.spellbook.core.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: bozhidar
 * Date: Jul 11, 2010
 * Time: 8:52:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class Lookup {
    private static final Map<Class, Object> SERVICES = new HashMap<Class, Object>();

    public static <T> void register(Class<T> serviceInterface, T serviceImplementation) {
        SERVICES.put(serviceInterface, serviceImplementation);
    }

    public static <T> T lookup(Class<T> serviceInterface) {
        return (T) SERVICES.get(serviceInterface);
    }

    public static void init() {
        register(DictionaryService.class, DictionaryService.getInstance());
    }
}
