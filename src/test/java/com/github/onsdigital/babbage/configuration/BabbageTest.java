package com.github.onsdigital.babbage.configuration;
import org.junit.Test;
import org.junit.Before;
import java.util.Map;

import static org.mockito.Mockito.when;
import org.mockito.Mock;

public class BabbageTest extends junit.framework.TestCase {
    @Mock
    private Babbage mockBabbage;

    @Before
    public void setUp() throws Exception {
        org.mockito.MockitoAnnotations.initMocks(this);
        when(mockBabbage.isPublishing()).thenReturn(true);
    }

    @Test
    public void testGetInstance_testdefaults() {
        Babbage testInstance = Babbage.getInstance();
        assertEquals(testInstance.getExportSeverUrl(), "http://localhost:9999/");
        assertEquals(testInstance.getMaxHighchartsServerConnections(), 50);
        assertEquals(testInstance.getMaxResultsPerPage(), 250);
        assertEquals(testInstance.getMaxVisiblePaginatorLink(), 5);
        assertEquals(testInstance.getResultsPerPage(), 10);
        assertEquals(testInstance.getSearchResponseCacheTime(), 5);
        assertEquals(testInstance.isDevEnv(), false);
        assertEquals(testInstance.isDevEnvironment(), false);
        assertEquals(testInstance.isPublishing(), false);

        Map<String, Object> mockConfig;
        mockConfig = Babbage.getInstance().getConfig();
        assertEquals(mockConfig.get("exportSeverUrl"), testInstance.getExportSeverUrl());
        assertEquals(mockConfig.get("isDevEnv"), testInstance.isDevEnv());
        assertEquals(mockConfig.get("isPublishing"), testInstance.isPublishing());
        assertEquals(mockConfig.get("maxHighchartsServerConnections"), testInstance.getMaxHighchartsServerConnections());
        assertEquals(mockConfig.get("maxResultsPerPage"), testInstance.getMaxResultsPerPage());
        assertEquals(mockConfig.get("maxVisiblePaginatorLink"), testInstance.getMaxVisiblePaginatorLink());
        assertEquals(mockConfig.get("resultsPerPage"), testInstance.getResultsPerPage());
        assertEquals(mockConfig.get("searchResponseCacheTime"), testInstance.getSearchResponseCacheTime());
    }
    @Test
    public void testGetApiRouterURL() {
        Babbage testInstance = Babbage.getInstance();
        assertNotNull(testInstance.getApiRouterURL());
        assertEquals(testInstance.getApiRouterURL(),"http://localhost:23200/v1");
    }

    @Test
    public void testGetServiceAuthToken() {
        Babbage testInstance = Babbage.getInstance();
        assertNotNull(testInstance.getServiceAuthToken());
        assertEquals(testInstance.getServiceAuthToken(),"ahyofaem2ieVie6eipaX6ietigh1oeM0Aa1aiyaebiemiodaiJah0eenuchei1ai");
    }

    @Test
    public void testIsNavigationEnabled() {
        Babbage testInstance = Babbage.getInstance();
        assertNotNull(testInstance.isNavigationEnabled());
        assertFalse(testInstance.isNavigationEnabled());
    }

    @Test
    public void testGetMaxCacheEntries() {
        Babbage testInstance = Babbage.getInstance();
        assertNotNull(testInstance.getMaxCacheEntries());
        assertEquals(testInstance.getMaxCacheEntries(),3000);
    }

    // getMaxCacheObjectSize
    @Test
    public void testGetMaxCacheObjectSize() {
        Babbage testInstance = Babbage.getInstance();
        assertNotNull(testInstance.getMaxCacheObjectSize());
        assertEquals(testInstance.getMaxCacheObjectSize(),50000);
    }

}
