package com.example.fotomaniaapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.fotomaniaapp.databinding.ActivityPhotoBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream

class PhotoActivity : AppCompatActivity() {

    private lateinit var binding :ActivityPhotoBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    var selectedBitmap : Bitmap?  = null
    private lateinit var view : View
    private lateinit var database : SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoBinding.inflate(layoutInflater)
         view = binding.root
        setContentView(view)

        database = this.openOrCreateDatabase("Foto", MODE_PRIVATE,null)

        registerLauncher()

        val intent = intent
        val info = intent.getStringExtra("info")
        if(info.equals("new")){
            // burada yenı kayıt yapıyor options menuden geliyor
        }else{
            binding.buttonKaydet.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id",1)
            val cursor = database.rawQuery("SELECT * FROM foto WHERE id = ?", arrayOf(selectedId.toString()))

            val fotoNameIx = cursor.getColumnIndex("fotoname")
            val fotoMakineIx = cursor.getColumnIndex("fotomakine")
            val fotoMekanIx = cursor.getColumnIndex("fotomekan")
            val fotoZamanIx = cursor.getColumnIndex("fotozaman")
            val imageIX = cursor.getColumnIndex("image")

            while(cursor.moveToNext()){
                binding.editTextFotoAd.setText(cursor.getString(fotoNameIx))
                binding.editTextFotoMakine.setText(cursor.getString(fotoMakineIx))
                binding.editTextFotoMekan.setText(cursor.getString(fotoMekanIx))
                binding.editTextFotoZaman.setText(cursor.getString(fotoZamanIx))

                val byteArray = cursor.getBlob(imageIX)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.resimSecImageView.setImageBitmap(bitmap)

            }
            cursor.close()
        }





        binding.buttonKaydet.setOnClickListener {
            val fotoName = binding.editTextFotoAd.text.toString()
            val fotoMakine = binding.editTextFotoMakine.text.toString()
            val fotoMekan = binding.editTextFotoMekan.text.toString()
            val fotoZaman = binding.editTextFotoZaman.text.toString()

            if(selectedBitmap != null){
                val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 400)
                val outputStream = ByteArrayOutputStream()
                smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
                val byteArray = outputStream.toByteArray()

                try {
                    database.execSQL("CREATE TABLE IF NOT EXISTS foto (id INTEGER PRIMARY KEY, fotoname VARCHAR, fotomakine VARCHAR, fotomekan VARCHAR, fotozaman VARCHAR, image BLOB)")

                    val sqlString = "INSERT INTO foto (fotoname, fotomakine, fotomekan, fotozaman,  image) VALUES (?, ?, ?, ?,?)"
                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1,fotoName)
                    statement.bindString(2,fotoMakine)
                    statement.bindString(3,fotoMekan)
                    statement.bindString(4,fotoZaman)
                    statement.bindBlob(5,byteArray)
                    statement.execute()
                }catch (e:Exception){
                    e.printStackTrace()
                }
                val intent = Intent(this@PhotoActivity,MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)

            }

        }

        binding.resimSecImageView.setOnClickListener {
            resimSec()
        }

    }


    fun resimSec(){
        if(ContextCompat.checkSelfPermission(this@PhotoActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this@PhotoActivity, Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view,"Galeriye gitmek için izin lazım", Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver",View.OnClickListener {
                    // izin istenicek
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }).show()
            }else{
                // izin istenicek
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

        }else{
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
            // galeriye gidicek.
        }
    }

    private fun registerLauncher(){

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == RESULT_OK){
                val intentFromResult = result.data
                if(intentFromResult != null){
                    val imageData = intentFromResult.data
                    if(imageData != null){
                        try {
                            if(Build.VERSION.SDK_INT >= 28){
                                val source = ImageDecoder.createSource(this@PhotoActivity.contentResolver, imageData)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.resimSecImageView.setImageBitmap(selectedBitmap)
                            }else{
                                selectedBitmap = MediaStore.Images.Media.getBitmap(this@PhotoActivity.contentResolver,imageData)
                                binding.resimSecImageView.setImageBitmap(selectedBitmap)
                            }
                        }catch (e: Exception){
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if(result){
                // izin verildi
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                // izin verilmedi
                Toast.makeText(this@PhotoActivity,"İzin Gerekli!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun makeSmallerBitmap(image : Bitmap, maximumSize:Int) : Bitmap{

        var width = image.width
        var height = image.height

        val bitmapRatio : Double = width.toDouble() / height.toDouble()

        if(bitmapRatio > 1){
            // gorsel yatay demek
            width = maximumSize
            val scaleHeight = width / bitmapRatio
            height = scaleHeight.toInt()
        }else{
            // gorsel dikey demek
            height = maximumSize
            val scaleWidth = height * bitmapRatio
            width = scaleWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image,width,height,true)
    }


}