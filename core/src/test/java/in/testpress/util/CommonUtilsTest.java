package in.testpress.util;

import android.app.Dialog;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class CommonUtilsTest {

    @Mock
    private Dialog dialog;

    @Test
    public void test_dismissDialogs_callingDialogDismiss() {
        Dialog[] dialogs = new Dialog[] { dialog };

        when(dialog.isShowing()).thenReturn(true);

        CommonUtils.dismissDialogs(dialogs);

        verify(dialog, times(1)).isShowing();
        verify(dialog, times(1)).dismiss();
    }
}
