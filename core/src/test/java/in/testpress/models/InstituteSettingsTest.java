package in.testpress.models;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class InstituteSettingsTest {

    @Test
    public void testConstructor_withNullBaseUrl() throws Exception {
        try {
            new InstituteSettings(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("BaseUrl must not be null or Empty.", e.getMessage());
        }
    }

    @Test
    public void testConstructor_withEmptyBaseUrl() throws Exception {
        try {
            new InstituteSettings("");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("BaseUrl must not be null or Empty.", e.getMessage());
        }
    }
}
