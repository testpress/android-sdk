package `in`.testpress.exam.util

import `in`.testpress.exam.util.TextUtil.removeHtmlTags
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemoveHtmlTagsFromTextTest {

    @Test
    fun testStringWithHtmlTagsIsRemoved() {
        val textWithHtmlTags = createStringWithHtmlTags()
        val text = removeHtmlTags(textWithHtmlTags)
        Assert.assertEquals(createStringWithoutHtmlTags(), text)
    }

    private fun createStringWithHtmlTags(): String {
        return  """ <style type='text/css'>
      
        @font-face {
            font-family: 'Periyar';
            src: url(https://static.testpress.in/institute/sandbox/custom_fonts/3e118c5e00dd425d96bbb43eb806a578.ttf);
        }
      
        @font-face {
            font-family: 'DCI + Tml + Ismail';
            src: url(https://static.testpress.in/institute/sandbox/custom_fonts/33a979837e4c482abc7b5ed8c94deb9a.ttf);
        }
      
        @font-face {
            font-family: 'B068_TAMElango_Panchali Bold';
            src: url(https://static.testpress.in/institute/sandbox/custom_fonts/305f394165c94b98a8d36ab3ed858460.ttf);
        }
      
        @font-face {
            font-family: 'B067_TAMElango_Panchali';
            src: url(https://static.testpress.in/institute/sandbox/custom_fonts/080937a7f1d44a50807d65da25ed6e74.ttf);
        }
      
        @font-face {
            font-family: 'Symbol';
            src: url(https://static.testpress.in/institute/sandbox/custom_fonts/73d20b5b739a4510baed6bf5b4f761f2.ttf);
        }
      
    </style>
    <p style="" class="MsoNormal"><span style=""> What total amount will Ram pay to the shopkeeper for purchasing
    3 kgs. of apples and 2 kgs. of guava<span style=""> 
    </span>in Delhi?</span></p><p style="" class="MsoNormal"><span style=""> </span></p>"""
    }

    private fun createStringWithoutHtmlTags(): String {
        return "What total amount will Ram pay to the shopkeeper for purchasing 3 kgs. of apples and 2 kgs. of guava  in Delhi?"
    }
}
