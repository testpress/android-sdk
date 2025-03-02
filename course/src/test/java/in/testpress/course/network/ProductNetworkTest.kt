package `in`.testpress.course.network

import `in`.testpress.course.util.mock
import `in`.testpress.network.ErrorHandlingCallAdapterFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(JUnit4::class)
class ProductNetworkTest {

    private lateinit var mockWebServer: MockWebServer

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var service: ProductService

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addCallAdapterFactory(ErrorHandlingCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ProductService::class.java)
    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }

    fun createProductsJson(): String {
       return """
            {"count":20,"per_page":20,"previous":null,"results":{"prices":[{"id":70,"name":"price","price":"100000.00","validity":null,"start_date":"2020-07-06","end_date":"2020-11-13"},{"id":69,"name":"qw","price":"5430.00","validity":null,"start_date":"2020-07-07","end_date":"2020-09-11"},{"id":67,"name":"Price","price":"10000.00","validity":null,"start_date":"2020-07-20","end_date":"2020-08-06"},{"id":2,"name":"","price":"1.00","validity":null,"start_date":null,"end_date":null},{"id":38,"name":"","price":"1.00","validity":null,"start_date":null,"end_date":null},{"id":65,"name":"","price":"10.00","validity":null,"start_date":null,"end_date":null},{"id":42,"name":"Price","price":"0.01","validity":null,"start_date":null,"end_date":null},{"id":46,"name":"Price","price":"10.00","validity":null,"start_date":null,"end_date":null},{"id":48,"name":"Price","price":"10.00","validity":null,"start_date":null,"end_date":null},{"id":51,"name":"Price","price":"10.00","validity":null,"start_date":null,"end_date":null},{"id":62,"name":"Offer Price","price":"0.00","validity":null,"start_date":null,"end_date":null},{"id":63,"name":"Offer Price","price":"0.00","validity":null,"start_date":null,"end_date":null},{"id":64,"name":"Offer Price","price":"0.00","validity":null,"start_date":null,"end_date":null},{"id":74,"name":"","price":"1.00","validity":null,"start_date":null,"end_date":null},{"id":71,"name":"Free","price":"0.00","validity":null,"start_date":null,"end_date":null},{"id":40,"name":"","price":"2.00","validity":null,"start_date":null,"end_date":null},{"id":1,"name":"","price":"5000.00","validity":null,"start_date":null,"end_date":null}],"courses":[{"id":2,"url":"https://sandbox.testpress.in/api/v2.4/courses/2/","title":"Sample course 2","description":"","image":"https://static.testpress.in/institute/sandbox/custom_icons/134536950464400782feb368a9df1ae4.jpeg","created_by":1,"created":"2016-12-11T07:35:40.616257Z","modified":"2021-05-13T11:43:57.507013Z","contents_url":"https://sandbox.testpress.in/api/v2.4/courses/2/contents/","chapters_url":"https://sandbox.testpress.in/api/v2.4/courses/2/chapters/","slug":"sample-course-2","chapters_count":1,"contents_count":8,"exams_count":5,"videos_count":0,"attachments_count":1,"html_contents_count":2,"order":7,"external_content_link":"","external_link_label":"Register Here","enable_discussions":false,"device_access_control":"Both Web and Mobile App","layout":"tree","tags":[]},{"id":1,"url":"https://sandbox.testpress.in/api/v2.4/courses/1/","title":"Sample course 1","description":"<p>Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry&#39;s standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.</p>\r\n","image":"https://static.testpress.in/courses/general/1442847196_Anchor10.png","created_by":1,"created":"2016-12-11T07:34:59.663395Z","modified":"2021-05-18T18:25:57.855237Z","contents_url":"https://sandbox.testpress.in/api/v2.4/courses/1/contents/","chapters_url":"https://sandbox.testpress.in/api/v2.4/courses/1/chapters/","slug":"sample-course-1","chapters_count":16,"contents_count":51,"exams_count":23,"videos_count":13,"attachments_count":4,"html_contents_count":9,"order":8,"external_content_link":"","external_link_label":"Register Here","enable_discussions":false,"device_access_control":"Both Web and Mobile App","layout":"tree","tags":[]},{"id":4,"url":"https://sandbox.testpress.in/api/v2.4/courses/4/","title":"Sample course 4","description":"sample descrition","image":"https://static.testpress.in/courses/general/1442847530_Bell.png","created_by":1,"created":"2016-12-11T07:35:41.177118Z","modified":"2021-05-13T11:43:21.212140Z","contents_url":"https://sandbox.testpress.in/api/v2.4/courses/4/contents/","chapters_url":"https://sandbox.testpress.in/api/v2.4/courses/4/chapters/","slug":"sample-course-2-3","chapters_count":7,"contents_count":1,"exams_count":0,"videos_count":1,"attachments_count":0,"html_contents_count":0,"order":9,"external_content_link":null,"external_link_label":"Register Here","enable_discussions":false,"device_access_control":"Both Web and Mobile App","layout":"tree","tags":[]},{"id":5,"url":"https://sandbox.testpress.in/api/v2.4/courses/5/","title":"Sample course 5","description":"<p>sample descrition</p>\r\n","image":"https://static.testpress.in/institute/sandbox/custom_icons/0f2f909a906f4dd6bc2a48ec8edbacc6.png","created_by":1,"created":"2016-12-11T07:35:41.300351Z","modified":"2021-02-14T07:28:20.194632Z","contents_url":"https://sandbox.testpress.in/api/v2.4/courses/5/contents/","chapters_url":"https://sandbox.testpress.in/api/v2.4/courses/5/chapters/","slug":"sample-course-2-4","chapters_count":0,"contents_count":0,"exams_count":0,"videos_count":0,"attachments_count":0,"html_contents_count":0,"order":12,"external_content_link":"","external_link_label":"Register Here","enable_discussions":false,"device_access_control":"Both Web and Mobile App","layout":"tree","tags":[]},{"id":10,"url":"https://sandbox.testpress.in/api/v2.4/courses/10/","title":"Sample Course 6","description":"Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type","image":"https://static.testpress.in/courses/general/1442854983_lock.png","created_by":1,"created":"2018-01-02T14:55:29.648852Z","modified":"2021-05-13T11:43:20.681233Z","contents_url":"https://sandbox.testpress.in/api/v2.4/courses/10/contents/","chapters_url":"https://sandbox.testpress.in/api/v2.4/courses/10/chapters/","slug":"sample-course-6","chapters_count":1,"contents_count":1,"exams_count":0,"videos_count":0,"attachments_count":0,"html_contents_count":1,"order":13,"external_content_link":null,"external_link_label":"Register Here","enable_discussions":false,"device_access_control":"Both Web and Mobile App","layout":"tree","tags":[]},{"id":44,"url":"https://sandbox.testpress.in/api/v2.4/courses/44/","title":"Push Notifications","description":"","image":"https://static.testpress.in/courses/general/1442848777_069.png","created_by":2,"created":"2018-12-21T07:38:23.283678Z","modified":"2021-05-13T11:43:56.542312Z","contents_url":"https://sandbox.testpress.in/api/v2.4/courses/44/contents/","chapters_url":"https://sandbox.testpress.in/api/v2.4/courses/44/chapters/","slug":"push-notifications","chapters_count":1,"contents_count":7,"exams_count":1,"videos_count":2,"attachments_count":0,"html_contents_count":4,"order":15,"external_content_link":"","external_link_label":"Register Here","enable_discussions":false,"device_access_control":"Both Web and Mobile App","layout":"tree","tags":[]},{"id":45,"url":"https://sandbox.testpress.in/api/v2.4/courses/45/","title":"Video Course","description":"","image":"https://static.testpress.in/courses/general/1442850204_video.png","created_by":1,"created":"2019-01-24T12:14:56.099446Z","modified":"2021-05-13T11:44:04.553616Z","contents_url":"https://sandbox.testpress.in/api/v2.4/courses/45/contents/","chapters_url":"https://sandbox.testpress.in/api/v2.4/courses/45/chapters/","slug":"video-course","chapters_count":1,"contents_count":22,"exams_count":0,"videos_count":22,"attachments_count":0,"html_contents_count":0,"order":16,"external_content_link":"","external_link_label":"Register Here","enable_discussions":false,"device_access_control":"Both Web and Mobile App","layout":"tree","tags":[]},{"id":49,"url":"https://sandbox.testpress.in/api/v2.4/courses/49/","title":"Scheduled Content","description":"","image":"https://static.testpress.in/courses/science/if_2407_-_Square_Root_754271.png","created_by":2,"created":"2019-03-26T12:59:47.716475Z","modified":"2021-05-13T11:43:22.974618Z","contents_url":"https://sandbox.testpress.in/api/v2.4/courses/49/contents/","chapters_url":"https://sandbox.testpress.in/api/v2.4/courses/49/chapters/","slug":"scheduled-content-2","chapters_count":1,"contents_count":1,"exams_count":0,"videos_count":0,"attachments_count":0,"html_contents_count":1,"order":20,"external_content_link":"","external_link_label":"Register Here","enable_discussions":false,"device_access_control":"Both Web and Mobile App","layout":"tree","tags":[]},{"id":50,"url":"https://sandbox.testpress.in/api/v2.4/courses/50/","title":"Test store course","description":"","image":"https://static.testpress.in/courses/motivation/1441974808_watch.png","created_by":2,"created":"2019-05-28T11:26:11.449336Z","modified":"2021-05-13T11:43:34.222312Z","contents_url":"https://sandbox.testpress.in/api/v2.4/courses/50/contents/","chapters_url":"https://sandbox.testpress.in/api/v2.4/courses/50/chapters/","slug":"test-store-course","chapters_count":1,"contents_count":3,"exams_count":1,"videos_count":2,"attachments_count":0,"html_contents_count":0,"order":21,"external_content_link":"","external_link_label":"Register Here","enable_discussions":false,"device_access_control":"Both Web and Mobile App","layout":"tree","tags":[]},{"id":53,"url":"https://sandbox.testpress.in/api/v2.4/courses/53/","title":"Test Course Payment","description":"","image":"https://static.testpress.in/courses/motivation/1441975683_Achievement_Goal_Mission_Vision_Rocket_Growth_Profit.png","created_by":2,"created":"2019-12-20T05:29:15.692758Z","modified":"2021-05-13T11:43:39.849967Z","contents_url":"https://sandbox.testpress.in/api/v2.4/courses/53/contents/","chapters_url":"https://sandbox.testpress.in/api/v2.4/courses/53/chapters/","slug":"test-course-payment","chapters_count":3,"contents_count":5,"exams_count":1,"videos_count":2,"attachments_count":0,"html_contents_count":2,"order":22,"external_content_link":"","external_link_label":"Register Here","enable_discussions":false,"device_access_control":"Both Web and Mobile App","layout":"tree","tags":[]},{"id":54,"url":"https://sandbox.testpress.in/api/v2.4/courses/54/","title":"Single Chapter Course","description":"","image":"https://static.testpress.in/courses/sports/1441973827_Sports_paddling.png","created_by":2,"created":"2019-12-26T11:12:39.509222Z","modified":"2021-02-14T07:28:27.301951Z","contents_url":"https://sandbox.testpress.in/api/v2.4/courses/54/contents/","chapters_url":"https://sandbox.testpress.in/api/v2.4/courses/54/chapters/","slug":"single-chapter-course","chapters_count":0,"contents_count":0,"exams_count":0,"videos_count":0,"attachments_count":0,"html_contents_count":0,"order":23,"external_content_link":"","external_link_label":"Register Here","enable_discussions":false,"device_access_control":"Both Web and Mobile App","layout":"tree","tags":[]},{"id":55,"url":"https://sandbox.testpress.in/api/v2.4/courses/55/","title":"Course 16","description":"","image":"https://static.testpress.in/courses/sports/1441973834_Sports_bull.png","created_by":2,"created":"2020-01-08T10:06:32.948597Z","modified":"2021-02-14T07:28:30.453053Z","contents_url":"https://sandbox.testpress.in/api/v2.4/courses/55/contents/","chapters_url":"https://sandbox.testpress.in/api/v2.4/courses/55/chapters/","slug":"course-16","chapters_count":0,"contents_count":0,"exams_count":0,"videos_count":0,"attachments_count":0,"html_contents_count":0,"order":24,"external_content_link":"","external_link_label":"Register Here","enable_discussions":false,"device_access_control":"Both Web and Mobile App","layout":"tree","tags":[]},{"id":56,"url":"https://sandbox.testpress.in/api/v2.4/courses/56/","title":"Course 17","description":"","image":"https://static.testpress.in/courses/Finance/1488740395_07.png","created_by":2,"created":"2020-01-08T10:08:03.604281Z","modified":"2021-02-14T07:28:30.453053Z","contents_url":"https://sandbox.testpress.in/api/v2.4/courses/56/contents/","chapters_url":"https://sandbox.testpress.in/api/v2.4/courses/56/chapters/","slug":"course-17","chapters_count":0,"contents_count":0,"exams_count":0,"videos_count":0,"attachments_count":0,"html_contents_count":0,"order":25,"external_content_link":"","external_link_label":"Register Here","enable_discussions":false,"device_access_control":"Both Web and Mobile App","layout":"tree","tags":[]},{"id":57,"url":"https://sandbox.testpress.in/api/v2.4/courses/57/","title":"Course 18","description":"","image":"https://static.testpress.in/courses/sports/1441973848_Sports_kayak.png","created_by":2,"created":"2020-01-08T10:08:32.881758Z","modified":"2021-05-13T11:43:10.702339Z","contents_url":"https://sandbox.testpress.in/api/v2.4/courses/57/contents/","chapters_url":"https://sandbox.testpress.in/api/v2.4/courses/57/chapters/","slug":"course-18","chapters_count":1,"contents_count":0,"exams_count":0,"videos_count":0,"attachments_count":0,"html_contents_count":0,"order":26,"external_content_link":"","external_link_label":"Register Here","enable_discussions":false,"device_access_control":"Both Web and Mobile App","layout":"tree","tags":[]},{"id":59,"url":"https://sandbox.testpress.in/api/v2.4/courses/59/","title":"Course 20","description":"","image":"https://static.testpress.in/courses/Numbers/1488845176_six_number_count_chart.png","created_by":2,"created":"2020-01-08T10:09:37.601353Z","modified":"2021-02-26T04:45:18.550213Z","contents_url":"https://sandbox.testpress.in/api/v2.4/courses/59/contents/","chapters_url":"https://sandbox.testpress.in/api/v2.4/courses/59/chapters/","slug":"course-20","chapters_count":0,"contents_count":0,"exams_count":0,"videos_count":0,"attachments_count":0,"html_contents_count":0,"order":28,"external_content_link":"","external_link_label":"Register Here","enable_discussions":false,"device_access_control":"Both Web and Mobile App","layout":"tree","tags":[]},{"id":61,"url":"https://sandbox.testpress.in/api/v2.4/courses/61/","title":"Course with empty video","description":"","image":"https://static.testpress.in/courses/motivation/1441974795_star.png","created_by":2,"created":"2020-01-21T05:43:12.089157Z","modified":"2021-05-13T11:43:10.681280Z","contents_url":"https://sandbox.testpress.in/api/v2.4/courses/61/contents/","chapters_url":"https://sandbox.testpress.in/api/v2.4/courses/61/chapters/","slug":"course-with-empty-video","chapters_count":1,"contents_count":0,"exams_count":0,"videos_count":0,"attachments_count":0,"html_contents_count":0,"order":30,"external_content_link":"","external_link_label":"Register Here","enable_discussions":false,"device_access_control":"Both Web and Mobile App","layout":"tree","tags":[]},{"id":77,"url":"https://sandbox.testpress.in/api/v2.4/courses/77/","title":"Perfect Title","description":"<p>Perfect Title</p>\r\n","image":"https://static.testpress.in/courses/sports/1441973795_Sports_parashoot.png","created_by":2,"created":"2020-12-17T04:22:32.837184Z","modified":"2021-05-13T11:43:53.998441Z","contents_url":"https://sandbox.testpress.in/api/v2.4/courses/77/contents/","chapters_url":"https://sandbox.testpress.in/api/v2.4/courses/77/chapters/","slug":"perfect-title","chapters_count":3,"contents_count":8,"exams_count":3,"videos_count":1,"attachments_count":2,"html_contents_count":1,"order":42,"external_content_link":"","external_link_label":"Register Here","enable_discussions":false,"device_access_control":"Both Web and Mobile App","layout":"tree","tags":[]}],"products":[{"id":46,"title":"Push Notifications","slug":"push-notifications","description_html":"","image":"https://static.testpress.in/static/img/product-placeholder.png","start_date":null,"end_date":null,"buy_now_text":"GET FOR FREE","surl":null,"furl":null,"prices":[63],"courses":[44],"current_price":"0.00","payment_link":""},{"id":3,"title":"1 Rs Product","slug":"1-rs-product","description_html":"<p>sample product to test payment</p>","image":"https://static.testpress.in/i/d9290526c09643b1b77472ac2aed09d8.jpeg","start_date":null,"end_date":null,"buy_now_text":"Buy Now","surl":null,"furl":null,"prices":[2],"courses":[77],"current_price":"1.00","payment_link":""},{"id":69,"title":"1 dollar course","slug":"1-dollar-course","description_html":"","image":"https://static.testpress.in/static/img/product-placeholder.png","start_date":null,"end_date":null,"buy_now_text":"Buy Now","surl":null,"furl":null,"prices":[74],"courses":[77,1],"current_price":"1.00","payment_link":""},{"id":41,"title":"Sample course 2","slug":"sample-course-2","description_html":"","image":"https://static.testpress.in/static/img/product-placeholder.png","start_date":null,"end_date":null,"buy_now_text":"Buy Now","surl":null,"furl":null,"prices":[42],"courses":[77],"current_price":"0.01","payment_link":""},{"id":36,"title":"Test product+123 ","slug":"test-product","description_html":"","image":"https://static.testpress.in/i/d8bd2700e405467e903ea235a4c76a87.jpeg","start_date":null,"end_date":null,"buy_now_text":"Buy Now","surl":null,"furl":null,"prices":[38],"courses":[],"current_price":"1.00","payment_link":""},{"id":63,"title":"Free Course","slug":"free-course","description_html":"","image":"https://static.testpress.in/i/c22130675c2e497ab74ade81772df422.png","start_date":null,"end_date":null,"buy_now_text":"GET FOR FREE","surl":null,"furl":null,"prices":[],"courses":[77,45,44,1],"current_price":"0.00","payment_link":""},{"id":43,"title":"Sample course 4","slug":"sample-course-4","description_html":"<html><body><p>sample descrition</p>\n</body></html>","image":"https://static.testpress.in/static/img/product-placeholder.png","start_date":null,"end_date":null,"buy_now_text":"GET FOR FREE","surl":null,"furl":null,"prices":[64],"courses":[4],"current_price":"0.00","payment_link":""},{"id":1,"title":"UPSC PRELIMINARY - TEST","slug":"upsc-preliminary-test","description_html":"<p>\t<strong>Dear Aspirants</strong></p>\n\n<p>\tGreetings from us. We are happy to announce the new schedule for 2017 prelims test Batch. We have modified the schedule as per the emerging demands of UPSC. In accordance, we have added</p>\n\n\n\n\n\n\n\n<ul>\t\n<li>Full test for Environment</li>\t\n<li>Full test for Economic survey, Budget and Current Affairs</li>\t\n<li>10 Additional Practice Tests</li>\t\n<li>Detailed Explanation of every test will be uploaded in the website</li></ul>\n\n\n\n\n\n\n\n<p>\t<strong>Our Prediction</strong></p>\n\n\n\n\n\n\n\n<table>\n<tbody>\n<tr>\n\t<td>\t\t<strong>Year </strong> \t</td>\n\t<td>\t\t<strong>Percentage of Hit </strong> \t</td>\n</tr>\n<tr>\n\t<td>\t\t2016 \t</td>\n\t<td>\t\t65% \t</td>\n</tr>\n<tr>\n\t<td>\t\t2015 \t</td>\n\t<td>\t\t70% \t</td>\n</tr>\n<tr>\n\t<td>\t\t2014 \t</td>\n\t<td>\t\t60% \t</td>\n</tr>\n</tbody>\n</table>\n\n\n\n\n\n\n\n<p>\t<strong>FEATURES OF TEST SERIES:<br>\t</strong></p>\n\n\n\n\n\n\n\n<ul>\t\n<li><strong> </strong>Each Test from (Test No.1 to 18) will have a weightage of:\n<ul>\t\n<li>40% Questions from Fundamental Sources like NCERT</li>\t\n<li>40% Application oriented questions</li>\t\n<li>20% Questions from Current Affairs</li></ul></li><li>Each Test from Test No.19 to 30 will be Full test Similar to UPSC Prelims with all levels of Difficulties</li></ul>\n\n\n\n\n\n\n\n<table><tbody><tr><td>GS Tests<br></td><td>24<br></td></tr><tr><td>CSAT Tests</td><td>6</td></tr><tr><td>GS Practice Tests</td><td>10</td></tr><tr><td><strong>Total</strong></td><td><strong>40 Tests</strong></td></tr></tbody></table>\n\n\n\n\n\n\n\n<ul>\n<li><span style='font-size: 13px; font-family: Lato, \"Helvetica Neue\", Helvetica, Arial, sans-serif;'>Test Time:2 Hours.</span></li><li>Detailed Explanation given.</li><li>Mark &amp; Rank on the Same Day.</li><li>Fees 5,000/-# only.</li><li>*Free All India Mock Test</li></ul>","image":"https://static.testpress.in/i/681ae381e15d4fd18e60924d5ae52ee0.jpeg","start_date":null,"end_date":null,"buy_now_text":"Buy Now","surl":"","furl":"","prices":[1],"courses":[],"current_price":"5000.00","payment_link":""},{"id":45,"title":"Sample Course 6","slug":"sample-course-6","description_html":"<html><body><p>Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type</p>\n</body></html>","image":"https://static.testpress.in/static/img/product-placeholder.png","start_date":null,"end_date":null,"buy_now_text":"Buy Now","surl":null,"furl":null,"prices":[46],"courses":[10],"current_price":"10.00","payment_link":""},{"id":47,"title":"Video Course","slug":"video-course","description_html":"","image":"https://static.testpress.in/static/img/product-placeholder.png","start_date":null,"end_date":null,"buy_now_text":"Buy Now","surl":null,"furl":null,"prices":[48],"courses":[],"current_price":"10.00","payment_link":""},{"id":40,"title":"Test Course Payment With Very very very very long and incorrect title","slug":"test-course-payment","description_html":"","image":"https://static.testpress.in/i/564a2641928f47f48b8a8081fa5bb868.png","start_date":null,"end_date":null,"buy_now_text":"GET FOR FREE","surl":null,"furl":null,"prices":[],"courses":[77,53],"current_price":"0.00","payment_link":""},{"id":2,"title":"P","slug":"free-product","description_html":"<html><body><p>Avail to get the benifits</p>\n</body></html>","image":"https://static.testpress.in/i/7cd45234a52e421a8ee27f7a5531b772.jpeg","start_date":null,"end_date":null,"buy_now_text":"GET FOR FREE","surl":null,"furl":null,"prices":[],"courses":[],"current_price":"0.00","payment_link":""},{"id":39,"title":"Course Payment","slug":"course-payment","description_html":"<html><body><ul>\n<li><strong>Who is this product for?</strong> The target audience can be a gender (women or men), an age group (college kids, retirees), a lifestyle demographic (new mothers, car enthusiasts) or some other defined group of people.</li>\n<li><strong>What are the product’s basic details?</strong> This includes attributes such as dimensions, materials, product features and functions.</li>\n<li><strong>Where would someone use this product?</strong> Is it meant for indoor or outdoor use, for your car or your home?</li>\n<li><strong>When should someone use the product?</strong> Is it meant to be used during a certain time of day, seasonally or for a specific type of occasion? Just as important is pointing out if a product can or should be used every day or year-round, as that will speak to its long-term value.</li>\n<li><strong>Why is this product useful or better than its competitors?</strong> This can be anything from quality to value to features — really think about the benefits that will speak to customers. Also consider how images can complement your product copy.</li>\n<li><strong>How does the product work?</strong> This may not be necessary for every product, but if you are selling anything with moving parts or electronics, it’s a must-have.</li>\n</ul>\n</body></html>","image":"https://static.testpress.in/i/aadbfc899a384f2c8c36fa0a4da63c26.png","start_date":null,"end_date":null,"buy_now_text":"Buy Now","surl":null,"furl":null,"prices":[40],"courses":[50,49],"current_price":"2.00","payment_link":""},{"id":50,"title":"Scheduled Content","slug":"scheduled-content","description_html":"","image":"https://static.testpress.in/static/img/product-placeholder.png","start_date":null,"end_date":null,"buy_now_text":"Buy Now","surl":null,"furl":null,"prices":[51],"courses":[49],"current_price":"10.00","payment_link":""},{"id":42,"title":"Sample course 1","slug":"sample-course-1","description_html":"<html><body><p>Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.</p>\n</body></html>","image":"https://static.testpress.in/static/img/product-placeholder.png","start_date":null,"end_date":null,"buy_now_text":"GET FOR FREE","surl":null,"furl":null,"prices":[62],"courses":[1],"current_price":"0.00","payment_link":""},{"id":62,"title":"Paid Course","slug":"paid-course","description_html":"","image":"https://static.testpress.in/i/01b1d30d678e465d93f7d8dfd6c0a3db.jpeg","start_date":null,"end_date":null,"buy_now_text":"Buy Now","surl":null,"furl":null,"prices":[65],"courses":[61,59,57,56,55,54,53,45,44,10,5,4,2,1],"current_price":"10.00","payment_link":""},{"id":64,"title":"Free Course 1","slug":"free-course-1","description_html":"","image":"https://static.testpress.in/i/a2f3751000b74465aac8c2bb23135620.png","start_date":null,"end_date":null,"buy_now_text":"GET FOR FREE","surl":null,"furl":null,"prices":[],"courses":[2,1],"current_price":"0.00","payment_link":""},{"id":66,"title":"Soft Skills","slug":"soft-skills","description_html":"<html><body><p>This course help you to get the soft skills </p>\n</body></html>","image":"https://static.testpress.in/i/189189f925444d48abf627bbe17f73d6.png","start_date":null,"end_date":null,"buy_now_text":"GET FOR FREE","surl":null,"furl":null,"prices":[70,69,67],"courses":[],"current_price":"0.00","payment_link":""},{"id":44,"title":"Sample course 5","slug":"sample-course-5","description_html":"<html><body><p>sample descrition</p>\n</body></html>","image":"https://static.testpress.in/static/img/product-placeholder.png","start_date":null,"end_date":null,"buy_now_text":"GET FOR FREE","surl":null,"furl":null,"prices":[71],"courses":[5],"current_price":"0.00","payment_link":""},{"id":37,"title":"test","slug":"test-124","description_html":"<html><body><p>testing</p>\n</body></html>","image":"https://static.testpress.in/i/81a5346cad09438f93f50f7a162edf54.jpeg","start_date":"2019-02-06T18:30:00Z","end_date":null,"buy_now_text":"GET FOR FREE","surl":null,"furl":null,"prices":[36],"courses":[],"current_price":"0.00","payment_link":""}]},"next":null}
       """.trimIndent()
    }

    @Test
    fun testGetProductsReturnsCorrectData() {
        val successResponse = MockResponse().setResponseCode(200).setBody(createProductsJson())
        mockWebServer.enqueue(successResponse)
        runBlocking {
            val response = service.getProducts().execute()
            mockWebServer.takeRequest()
            assertEquals(response.isSuccessful, true)
            assertEquals(response.body().count, 20)
            assertEquals(response.body().results.prices!![0]!!.id, 70)
        }
    }

}