package com.example.fotomaniaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fotomaniaapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var fotoList : ArrayList<Foto>
    private lateinit var fotoAdapter: FotoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        fotoList = ArrayList<Foto>()

        fotoAdapter = FotoAdapter(fotoList)
        binding.rv.layoutManager = LinearLayoutManager(this@MainActivity)
        binding.rv.adapter = fotoAdapter

        try {
            val database = this.openOrCreateDatabase("Foto", MODE_PRIVATE,null)
            val cursor = database.rawQuery("SELECT * FROM foto",null)
            val fotoNameIx = cursor.getColumnIndex("fotoname")
            val idIx = cursor.getColumnIndex("id")

            while (cursor.moveToNext()){
                val name = cursor.getString(fotoNameIx)
                val id = cursor.getInt(idIx)
                val foto = Foto(name,id)
                fotoList.add(foto)
            }

            fotoAdapter.notifyDataSetChanged()

            cursor.close()

        }catch (e:Exception){
            e.printStackTrace()
        }




        binding.fab.setOnClickListener {
            val intent = Intent(this@MainActivity,PhotoActivity::class.java)
            intent.putExtra("info","new")
            startActivity(intent)
        }
    }




}