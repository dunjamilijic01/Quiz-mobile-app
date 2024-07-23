package com.example.mosisproject

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.mosisproject.data.Question
import com.example.mosisproject.data.User
import com.example.mosisproject.model.UsersViewModel

class MyQuestionListAdapter(private val context: Activity, private val questions:ArrayList<HashMap<String,Any>>)
    :BaseAdapter(),Filterable{
    val allQuestions=questions
    var filteredQuestions=questions
    override fun getCount(): Int {
        return filteredQuestions.size
    }

    override fun getItem(p0: Int): Any {
       return filteredQuestions.get(p0)
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.questions_list_view, null, true)

        val icon=rowView.findViewById<ImageView>(R.id.icon)
        val diff=rowView.findViewById<TextView>(R.id.difficulty)
        val btn=rowView.findViewById<TextView>(R.id.goToQuestion)
        val num=rowView.findViewById<TextView>(R.id.questionNum)
        val email=rowView.findViewById<TextView>(R.id.author)

        num.text=(position+1).toString()
        icon.setImageResource(filteredQuestions[position].get("icon") as Int)
       val q=filteredQuestions[position].get("question") as Question
        diff.setText(q.difficulty)
        btn.setText(filteredQuestions[position].get("answered").toString())
            if(btn.text!="Click to answer")
                btn.setBackgroundColor(context.resources.getColor(R.color.greenColor))


        email.setText(filteredQuestions[position].get("username").toString())


        return rowView
    }
    override fun getFilter(): Filter {
        return filter
    }

    private val filter=object: Filter(){
        override fun performFiltering(p0: CharSequence?): FilterResults {

            val result= FilterResults()
            if(p0==null || p0.length==0){
                result.values=allQuestions
                result.count=allQuestions.size
            }
            else{
                val filetered= java.util.ArrayList<HashMap<String,Any>>()
                for (u: HashMap<String,Any> in allQuestions) {
                    val str=u.get("username").toString()
                    if (str.toUpperCase().contains(p0.toString().toUpperCase()))
                        filetered.add(u)
                }
                result.values=filetered
                result.count=filetered.size
            }
            return result
        }

        override fun publishResults(p0: CharSequence?, p1: FilterResults) {
            filteredQuestions=p1.values as java.util.ArrayList<HashMap<String,Any>>
            notifyDataSetChanged()
        }
    }
}