package com.mixer.interactive.test.unit.services;

import com.mixer.interactive.GameClient;
import com.mixer.interactive.services.AbstractServiceProvider;
import com.mixer.interactive.test.util.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for <code>ServiceManager</code>.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class ServiceManagerUnitTest {

    /**
     * The project version ID for the Interactive integration that this test will use
     */
    private static final int PROJECT_VERSION_ID = 1234;

    /**
     * The <code>GameClient</code> that this test will use
     */
    private static GameClient gameClient = new GameClient(PROJECT_VERSION_ID, TestUtils.CLIENT_ID);

    @Before
    public void setup() {
        gameClient.getServiceManager().removeAll();
    }

    @Test
    public void service_manager_exists() {
        Assert.assertEquals("A service manager exists for the client", true, gameClient.getServiceManager() != null);
    }

    @Test
    public void service_manager_starts_empty() {
        Assert.assertEquals("Service manager starts empty", true, gameClient.getServiceManager().getServiceProviders().isEmpty());
    }

    @Test
    public void can_register_provider_by_instance() {
        Assert.assertEquals("Can register service provider by instance", true, gameClient.getServiceManager().register(new TestServiceProvider(gameClient)));
    }

    @Test
    public void can_register_provider_by_class() {
        Assert.assertEquals("Can register service provider by class", true, gameClient.getServiceManager().register(TestServiceProvider.class));
    }

    @Test
    public void only_register_one_provider() {
        gameClient.getServiceManager().register(TestServiceProvider.class);
        Assert.assertEquals("Only one provider registered", 1, gameClient.getServiceManager().getServiceProviders().size());
    }

    @Test
    public void cannot_register_null_provider() {
        AbstractServiceProvider serviceProvider = null;
        Assert.assertEquals("Add null service provider", false, gameClient.getServiceManager().register(serviceProvider));
    }

    @Test
    public void cannot_register_duplicate_providers() {
        gameClient.getServiceManager().register(TestServiceProvider.class);
        gameClient.getServiceManager().register(new TestServiceProvider(gameClient));
        Assert.assertEquals("Only one provider registered", 1, gameClient.getServiceManager().getServiceProviders().size());
    }
    
    @Test
    public void can_get_service_provider() {
        gameClient.getServiceManager().register(TestServiceProvider.class);
        Assert.assertEquals("Get the expected service provider", true, gameClient.getServiceManager().get(TestServiceProvider.class).getClass().equals(TestServiceProvider.class));
    }

    @Test
    public void get_expected_service_provider() {
        gameClient.getServiceManager().register(TestServiceProvider.class);
        gameClient.getServiceManager().register(TestServiceProviderTwo.class);
        Assert.assertEquals("Get the expected service provider", true, gameClient.getServiceManager().get(TestServiceProvider.class).getClass().equals(TestServiceProvider.class));
        Assert.assertEquals("Get the expected service provider", false, gameClient.getServiceManager().get(TestServiceProviderTwo.class).getClass().equals(TestServiceProvider.class));
    }

    @Test
    public void cannot_remove_null_provider() {
        AbstractServiceProvider serviceProvider = null;
        Assert.assertEquals("Remove non-existent provider", false, gameClient.getServiceManager().remove(serviceProvider));
    }

    @Test
    public void can_remove_provider_by_instance() {
        TestServiceProvider serviceProvider = new TestServiceProvider(gameClient);
        gameClient.getServiceManager().register(serviceProvider);
        Assert.assertEquals("Remove provider by instance", true, gameClient.getServiceManager().remove(serviceProvider));
        Assert.assertEquals("No providers registered", true, gameClient.getServiceManager().getServiceProviders().isEmpty());
    }

    @Test
    public void can_remove_provider_by_class() {
        gameClient.getServiceManager().register(TestServiceProvider.class);
        Assert.assertEquals("Remove provider by instance", true, gameClient.getServiceManager().remove(TestServiceProvider.class));
        Assert.assertEquals("No providers registered", true, gameClient.getServiceManager().getServiceProviders().isEmpty());
    }

    @Test
    public void remove_all_providers_none_registered() {
        gameClient.getServiceManager().removeAll();
        Assert.assertEquals("No service providers exist", 0, gameClient.getServiceManager().getServiceProviders().size());
    }

    @Test
    public void remove_all_providers_one_registered() {
        gameClient.getServiceManager().register(TestServiceProvider.class);
        Assert.assertEquals("One service provider registered", 1, gameClient.getServiceManager().getServiceProviders().size());
        gameClient.getServiceManager().removeAll();
        Assert.assertEquals("All service providers removed", 0, gameClient.getServiceManager().getServiceProviders().size());
    }

    @Test
    public void remove_all_providers_many_registered() {
        gameClient.getServiceManager().register(TestServiceProvider.class);
        gameClient.getServiceManager().register(TestServiceProviderTwo.class);
        Assert.assertEquals("Two service providers registered", 2, gameClient.getServiceManager().getServiceProviders().size());
        gameClient.getServiceManager().removeAll();
        Assert.assertEquals("All service providers removed", 0, gameClient.getServiceManager().getServiceProviders().size());
    }

    /**
     * Test service provider
     *
     * @author  Microsoft Corporation
     * @author  pahimar
     *
     * @since   1.0.0
     */
    public static final class TestServiceProvider extends AbstractServiceProvider {

        /**
         * Initializes a new <code>TestServiceProvider</code>.
         *
         * @param   gameClient
         *          The <code>GameClient</code> that this test service provider will use
         *
         * @since   1.0.0
         */
        public TestServiceProvider(GameClient gameClient) {
            super(gameClient);
        }
    }

    /**
     * A different test service provider
     *
     * @author  Microsoft Corporation
     * @author  pahimar
     *
     * @since   1.0.0
     */
    public static final class TestServiceProviderTwo extends AbstractServiceProvider {

        /**
         * Initializes a new <code>TestServiceProviderTwo</code>.
         *
         * @param   gameClient
         *          The <code>GameClient</code> that this test service provider will use
         *
         * @since   1.0.0
         */
        public TestServiceProviderTwo(GameClient gameClient) {
            super(gameClient);
        }
    }
}
