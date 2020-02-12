package `in`.testpress.course.util

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView

import java.util.ArrayList
import java.util.regex.Pattern


class PatternEditableBuilder {
    internal var patterns: ArrayList<SpannablePatternItem>

    inner class SpannablePatternItem(var pattern: Pattern, var styles: SpannableStyleListener?, var listener: SpannableClickedListener?)

    /* This stores the style listener for a pattern item
       Used to style a particular category of spans */
    abstract class SpannableStyleListener {
        var spanTextColor: Int = 0

        constructor(spanTextColor: Int) {
            this.spanTextColor = spanTextColor
        }

        internal abstract fun onSpanStyled(ds: TextPaint)
    }

    interface SpannableClickedListener {
        fun onSpanClicked(text: String)
    }

    inner class StyledClickableSpan(internal var item: SpannablePatternItem) : ClickableSpan() {

        override fun updateDrawState(ds: TextPaint) {
            if (item.styles != null) {
                item.styles!!.onSpanStyled(ds)
            }
            super.updateDrawState(ds)
            ds.setUnderlineText(false);
        }

        override fun onClick(widget: View) {
            if (item.listener != null) {
                val tv = widget as TextView
                val span = tv.text as Spanned
                val start = span.getSpanStart(this)
                val end = span.getSpanEnd(this)
                val text = span.subSequence(start, end)
                item.listener!!.onSpanClicked(text.toString())
            }
            widget.invalidate()
        }
    }

    init {
        this.patterns = ArrayList()
    }


    fun addPattern(pattern: Pattern, spanStyles: SpannableStyleListener?, listener: SpannableClickedListener?): PatternEditableBuilder {
        patterns.add(SpannablePatternItem(pattern, spanStyles, listener))
        return this
    }

    fun addPattern(pattern: Pattern, spanStyles: SpannableStyleListener): PatternEditableBuilder {
        addPattern(pattern, spanStyles, null)
        return this
    }

    fun addPattern(pattern: Pattern): PatternEditableBuilder {
        addPattern(pattern, null, null)
        return this
    }

    fun addPattern(pattern: Pattern, textColor: Int): PatternEditableBuilder {
        addPattern(pattern, textColor, null)
        return this
    }

    fun addPattern(pattern: Pattern, textColor: Int, listener: SpannableClickedListener?): PatternEditableBuilder {
        val styles = object : SpannableStyleListener(textColor) {
            public override fun onSpanStyled(ds: TextPaint) {
                ds.linkColor = this.spanTextColor
            }
        }
        addPattern(pattern, styles, listener)
        return this
    }

    fun addPattern(pattern: Pattern, listener: SpannableClickedListener): PatternEditableBuilder {
        addPattern(pattern, null, listener)
        return this
    }


    fun into(textView: TextView) {
        val result = build(textView.text)
        textView.text = result
        textView.movementMethod = LinkMovementMethod.getInstance()
    }


    fun build(editable: CharSequence): SpannableStringBuilder {
        val ssb = SpannableStringBuilder(editable)
        for (item in patterns) {
            val matcher = item.pattern.matcher(ssb)
            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                val url = StyledClickableSpan(item)
                ssb.setSpan(url, start, end, 0)
            }
        }
        return ssb
    }

}