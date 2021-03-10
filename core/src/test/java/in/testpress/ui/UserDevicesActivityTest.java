//package in.testpress.ui;
//
//import androidx.fragment.app.Fragment;
//import android.widget.TextView;
//
//import androidx.test.core.app.ApplicationProvider;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.robolectric.Robolectric;
//import org.robolectric.RobolectricTestRunner;
//
//import in.testpress.R;
//import in.testpress.core.TestpressSdk;
//import in.testpress.core.TestpressSession;
//import in.testpress.models.AccountActivity;
//import in.testpress.models.InstituteSettings;
//import in.testpress.util.SingleTypeAdapter;
//import okhttp3.mockwebserver.MockResponse;
//import okhttp3.mockwebserver.MockWebServer;
//import retrofit2.Response;
//
//import junit.framework.Assert;
//
//import java.io.IOException;
//import java.time.LocalDate;
//import java.time.Period;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//import static org.mockito.Mockito.spy;
//
//@RunWith(RobolectricTestRunner.class)
//public class UserDevicesActivityTest {
//
//    private UserDevicesActivity activity;
//    private static final String USER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6NDYsInVzZXJfaWQiOjQ2LCJlbWFpbCI6IiIsImV4cCI6MTUxOTAzNjUzM30.FUuyJfYNSAw_VcypZsN8_ZHvZra6gHU3njcXmr-TGVU";
//    private AccountActivity accountActivity;
//    private MockWebServer mockWebServer;
//    private InstituteSettings instituteSettings;
//
//
//    public String randomDataString() {
//        return LocalDate.now().minus(Period.ofDays((new Random().nextInt(365 * 70)))).toString();
//    }
//
//    public AccountActivity createAccountActivity() {
//        Random random = new Random();
//
//        AccountActivity accountActivity = new AccountActivity();
//        accountActivity.setId(random.nextInt(100));
//        accountActivity.setCurrentDevice((random.nextInt(2) == 0) ? "true" : "false");
//        accountActivity.setIpAddress(random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256));
//        accountActivity.setLastUsed(randomDataString());
//        accountActivity.setLocation("IN");
//        return accountActivity;
//    }
//
//    public String getAccountActivityJSON() {
//        return "{ `count`: 1, `next`: null, `previous`: null, `per_page`: 20, `results`: [ { `id`: 182, `user_agent`: `Generic Smartphone / Android / okhttp 3.4.1`, `ip_address`: `49.207.142.221`, `last_used`: `2019-07-30T05:32:52.226664Z`, `location`: `IN`, `current_device`: false }] }".replace('`', '"');
//    }
//
//    @Before
//    public void setUp() throws IOException{
//        mockWebServer = new MockWebServer();
//        accountActivity = createAccountActivity();
//        instituteSettings =
//                new InstituteSettings("http://localhost:9200");
//        instituteSettings.setLockoutLimit(1);
//        TestpressSdk.setTestpressSession(ApplicationProvider.getApplicationContext(),
//                new TestpressSession(instituteSettings, USER_TOKEN));
//        activity = Robolectric.buildActivity(UserDevicesActivity.class)
//                .create()
//                .resume()
//                .get();
//        activity = spy(activity);
//        mockWebServer.start(9200);
//    }
//
//    @Test
//    public void testFragment() {
//        /*
//         * UserActivityFragment should be used in UserDevicesActivity
//         * */
//
//        Fragment fragment = activity.getSupportFragmentManager().getFragments().get(0);
//
//        Assert.assertTrue(fragment instanceof UserActivityFragment);
//    }
//
//    @Test
//    public void testAdapter() {
//        /*
//        * Adapter in UserActivityFragment should contain accountActivities
//        * */
//        UserActivityFragment fragment = (UserActivityFragment) activity.getSupportFragmentManager().getFragments().get(0);
//        List<AccountActivity> accountActivities = new ArrayList<>();
//        accountActivities.add(accountActivity);
//        SingleTypeAdapter adapter = fragment.createAdapter(accountActivities);
//
//        Assert.assertEquals(adapter.getCount(), accountActivities.size());
//    }
//
//    @Test
//    public void testPager() throws IOException, InterruptedException {
//        /*
//        * Pager should fetch accountActivity
//        * */
//        UserActivityFragment fragment = (UserActivityFragment) activity.getSupportFragmentManager().getFragments().get(0);
//        MockResponse successResponse = new MockResponse().setBody(getAccountActivityJSON()).setResponseCode(200);
//        mockWebServer.enqueue(successResponse);
//        Response response = fragment.getPager().getItems(1, 1);
//        fragment.refreshAdapter();
//        mockWebServer.takeRequest();
//
//        Assert.assertTrue(response.isSuccessful());
//    }
//
//    @Test
//    public void testLoginAttemptRestrictionInfo() {
//        activity.setInfoText();
//        String lockout_limit_info = "Note : Admin has restricted login attempts to %s";
//        TextView info = activity.findViewById(R.id.parallel_login_restriction_note);
//
//        Assert.assertEquals(info.getText(), String.format(lockout_limit_info, instituteSettings.getLockoutLimit()));
//    }
//
//    @After
//    public void tearDown() throws IOException {
//        mockWebServer.shutdown();
//    }
//
//}
