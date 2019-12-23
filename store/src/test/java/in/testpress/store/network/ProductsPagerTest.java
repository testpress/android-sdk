package in.testpress.store.network;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class ProductsPagerTest {

    @Mock
    private TestpressStoreApiClient apiClient;

    @Mock
    private ProductsPager productsPager;

    @Mock
    public Map<String, Object> queryParams;


    @Test
    public void testProductPager_checkGetItemCallsGetProductsOrNot() {
        doCallRealMethod().when(productsPager).getItems(anyInt(), anyInt());
        doCallRealMethod().when(productsPager).setApiClient(apiClient);

        productsPager.queryParams = queryParams;
        productsPager.setApiClient(apiClient);
        productsPager.getItems(anyInt(), anyInt());

        verify(apiClient, times(1)).getProducts(ArgumentMatchers.<String, Object>anyMap());
    }
}

