package com.example.mosisproject

import android.app.Activity
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.example.mosisproject.data.User
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.net.URL
import java.util.*

class MyUsersListAdapter (private val context: Activity, private val users:ArrayList<User>)
    : BaseAdapter(),Filterable{
    private lateinit var storage:FirebaseStorage

    val allUseres=users
    var filteredUseres=users
    override fun getCount(): Int {

        return filteredUseres.size
    }

    override fun getItem(p0: Int): Any {
        return filteredUseres.get(p0)
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.users_list_view, null, true)


        storage=FirebaseStorage.getInstance()
        val imgName=filteredUseres[position].image

        val pic=rowView.findViewById<ImageView>(R.id.userPic)
        val name=rowView.findViewById<TextView>(R.id.nameLastName)
        val point=rowView.findViewById<TextView>(R.id.points)
        val num=rowView.findViewById<TextView>(R.id.ranking)
        Glide.with(context).load(imgName).into(pic)

        num.text = (position + 1).toString()

        point.text = filteredUseres[position].points.toString()
        name.text = filteredUseres[position].firstName + " " + filteredUseres[position].lastName

        return rowView
    }

    override fun getFilter():Filter{
        return filter
    }

    private val filter=object:Filter(){
        override fun performFiltering(p0: CharSequence?): FilterResults {

           val result=FilterResults()
            if(p0==null || p0.length==0){
                result.values=allUseres
                result.count=allUseres.size
            }
            else{
                val filetered=ArrayList<User>()
                for (u:User in allUseres) {
                    val str=u.firstName+" "+u.lastName
                    if (str.toUpperCase().contains(p0.toString().toUpperCase()))
                        filetered.add(u)
                }
                result.values=filetered
                result.count=filetered.size
            }
            return result
        }

        override fun publishResults(p0: CharSequence?, p1: FilterResults) {
            filteredUseres=p1.values as ArrayList<User>
            notifyDataSetChanged()
        }
    }

}