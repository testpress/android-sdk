package in.testpress.core;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;


@RunWith(PowerMockRunner.class)
public class TestpressExceptionTest {

    @Mock
    private TestpressException testpressException;

    @Test
    public void testIsCancelled_returnCorrectValueOrNot() {
        doCallRealMethod().when(testpressException).setCancelled(anyBoolean());
        doCallRealMethod().when(testpressException).isCancelled();

        testpressException.setCancelled(true);

        Assert.assertEquals(true, testpressException.isCancelled());
    }
}
