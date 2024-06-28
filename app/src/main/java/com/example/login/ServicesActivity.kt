package com.example.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ServicesActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services)

        val btnReclamar = findViewById<Button>(R.id.btn_reclamar)
        val btnConsultar = findViewById<Button>(R.id.btn_consultar)
        val btnSair = findViewById<Button>(R.id.btn_sair)
        val btnDeslogar = findViewById<ImageView>(R.id.exit)

        val clickAnimation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation)

        btnReclamar.setOnClickListener {
            btnReclamar.startAnimation(clickAnimation)
            val intent = Intent(this, ComplaintActivity::class.java)
            startActivity(intent)
        }

        btnConsultar.setOnClickListener {
            btnConsultar.startAnimation(clickAnimation)
            val intent = Intent(this, ConsultationsActivity::class.java)
            startActivity(intent)
        }

        btnDeslogar.setOnClickListener {
            btnDeslogar.startAnimation(clickAnimation)
            auth.signOut()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnSair.setOnClickListener {
            btnSair.startAnimation(clickAnimation)
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmação")
            builder.setMessage("Deseja realmente sair?")

            builder.setPositiveButton("Sim") { _, _ ->
                finishAffinity()
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }
    }

    override fun onStart() {
        super.onStart()

        val textNameUser = findViewById<TextView>(R.id.name_user)
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val usersRef = FirebaseDatabase.getInstance().getReference("users")

            usersRef.child(userId).child("nome")
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val nome = dataSnapshot.value as? String
                        textNameUser.text = nome ?: "Nome não encontrado"
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("Firebase", "Erro ao ler nome do usuário: ${databaseError.message}")
                    }
                })
        }
    }
}