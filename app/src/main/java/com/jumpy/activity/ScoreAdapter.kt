package com.jumpy.activity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.jumpy.R
import com.jumpy.data.Score

class ScoreAdapter(private val context: Context, private val scores: List<Score>) : BaseAdapter() {

    override fun getCount(): Int {
        return scores.size
    }

    override fun getItem(position: Int): Any {
        return scores[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_score, parent, false)

        val score = getItem(position) as Score

        val rankTextView = view.findViewById<TextView>(R.id.rank_text_view)
        rankTextView.text = (position + 1).toString()

        val scoreTextView = view.findViewById<TextView>(R.id.score_text_view)
        scoreTextView.text = score.value.toString()

        return view
    }

}
