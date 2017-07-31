package com.mixer.interactive.services;

import com.mixer.interactive.GameClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

/**
 * A <code>ServiceManager</code> contains instances of service providers that perform some function within the client.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class ServiceManager<T extends AbstractServiceProvider> {

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * <code>Set</code> of service provider instances
     */
    private final Set<T> serviceProviders = new HashSet<>();

    /**
     * <code>GameClient</code> that owns this service manager
     */
    private final GameClient gameClient;

    /**
     * Initializes a new <code>ServiceManager</code>.
     *
     * @param   gameClient
     *          The <code>GameClient</code> that owns this service manager
     *
     * @since   1.0.0
     */
    public ServiceManager(GameClient gameClient) {
        if (gameClient == null) {
            LOG.fatal("GameClient may not be null");
            throw new IllegalArgumentException("GameClient may not be null");
        }
        this.gameClient = gameClient;
    }

    /**
     * Retrieves all service provider instances that are currently registered with this service provider as a
     * <code>Set</code>.
     *
     * @return  <code>Set</code> of service provider instances currently registered with this service manager
     *
     * @since   1.0.0
     */
    public Set<T> getServiceProviders() {
        return serviceProviders;
    }

    /**
     * Gets the service provider instance specified by the provided class. If there does not exist a service provider
     * instance for the provided class, <code>null</code> is returned.
     *
     * @param   clazz
     *          Class of service provider to be retrieved
     * @param   <V>
     *          Type of service provider expected
     *
     * @return  The service provider instance specified by the provided class. If there does not exist
     *          a service provider instance for the provided class, <code>null</code> is returned.
     *
     * @since   1.0.0
     */
    public <V extends T> V get(Class<V> clazz) {
        for (T serviceProvider : this.serviceProviders) {
            if (serviceProvider.getClass() == clazz) {
                return clazz.cast(serviceProvider);
            }
        }

        return null;
    }

    /**
     * Registers a service provider instance with this service manager.
     *
     * @param   serviceProvider
     *          Service provider instance
     *
     * @return  <code>true</code> if the service provider was registered successfully, <code>false</code> otherwise
     *
     * @since   1.0.0
     */
    public boolean register(T serviceProvider) {
        return serviceProvider != null && serviceProviders.add(serviceProvider);
    }

    /**
     * Registers a new service provider instance with this service manager, specified by the provided class. The
     * provided class is assumed to have a constructor with a sole argument (<code>GameClient</code>).
     *
     * @param   clazz
     *          Class of service provider to be registered
     * @param   <V>
     *          Type of service provider to be registered
     *
     * @return  <code>true</code> if the service provider was registered successfully, <code>false</code> otherwise
     *
     * @since   1.0.0
     */
    public <V extends T> boolean register(Class<V> clazz) {
        if (clazz == null || gameClient == null || !AbstractServiceProvider.class.isAssignableFrom(clazz)) {
            return false;
        }

        try {
            return serviceProviders.add(clazz.getConstructor(GameClient.class).newInstance(gameClient));
        }
        catch (InstantiationException | IllegalAccessException| InvocationTargetException | NoSuchMethodException e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * Removes a service provider from the service manager.
     *
     * @param   serviceProvider
     *          Service provider instance
     * @param   <V>
     *          Type of service provider to be removed
     *
     * @return  <code>true</code> if the service provider was removed successfully, <code>false</code> otherwise
     *
     * @since   1.0.0
     */
    public <V extends T> boolean remove(V serviceProvider) {
        return serviceProviders.remove(serviceProvider);
    }

    /**
     * Removes a service provider from the service manager.
     *
     * @param   clazz
     *          Class of service provider to be registered
     * @param   <V>
     *          Type of service provider to be removed
     *
     * @return  <code>true</code> if the service provider was removed successfully, <code>false</code> otherwise
     *
     * @since   1.0.0
     */
    public <V extends T> boolean remove(Class<V> clazz) {
        for (T serviceProvider : this.serviceProviders) {
            if (serviceProvider.getClass() == clazz) {
                return serviceProviders.remove(serviceProvider);
            }
        }

        return false;
    }

    /**
     * Removes all service providers from this service manager.
     *
     * @since   1.0.0
     */
    public void removeAll() {
        serviceProviders.clear();
    }
}
