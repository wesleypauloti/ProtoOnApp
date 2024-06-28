package com.example.login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ConsultationsActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    private lateinit var nome: String
    private lateinit var availableProtocols: List<String>
    var tempProtocolList = mutableListOf<String>()
    var selectedProtocol: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consultations)

        val lblProblema = findViewById<TextView>(R.id.probema)
        val lblBairro = findViewById<TextView>(R.id.bairro)
        val lblRua = findViewById<TextView>(R.id.rua)
        val lblDesc = findViewById<TextView>(R.id.descricao)
        val lblDate = findViewById<TextView>(R.id.dateTime)
        val lblStatus = findViewById<TextView>(R.id.status)

        val btnConsultar = findViewById<Button>(R.id.btn_consultar)
        val btnVoltar = findViewById<Button>(R.id.btn_voltar)
        val btnDeslogar = findViewById<ImageView>(R.id.exit)

        val clickAnimation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation)

        lblProblema.visibility = View.GONE
        lblBairro.visibility = View.GONE
        lblRua.visibility = View.GONE
        lblDesc.visibility = View.GONE
        lblDate.visibility = View.GONE
        lblStatus.visibility = View.GONE

        btnConsultar.setOnClickListener {
            btnConsultar.startAnimation(clickAnimation)
            val userId = auth.currentUser?.uid

            if(userId == null) {
                Toast.makeText(baseContext, "Not Found User", Toast.LENGTH_SHORT).show()
            } else if (userId != null) {
                val complaintsRef = FirebaseDatabase.getInstance().getReference("reclamacoes")

                complaintsRef.orderByChild("requerente").equalTo(nome)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val numberOfComplaints = dataSnapshot.childrenCount.toInt()
                            if (dataSnapshot.exists()) {
                                for (complaintSnapshot in dataSnapshot.children) {
                                    val protocol = complaintSnapshot.child("protocolo").value as? String
                                    protocol?.let {
                                        tempProtocolList.add(it)
                                    }
                                }

                                availableProtocols = tempProtocolList.toList()
                                if (numberOfComplaints > 1) {
                                    optionsProtocol(dataSnapshot)
                                } else {
                                    for (complaintSnapshot in dataSnapshot.children) {
                                        selectedProtocol = (complaintSnapshot.child("protocolo").value as? String).toString()
                                    }

                                    showDatails(dataSnapshot)
                            }

                            } else {
                                Toast.makeText(baseContext, "Você não tem reclamações registradas.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e("Firebase", "Erro ao ler Reclamações: ${databaseError.message}")
                        }
                    })
            }
        }

        btnDeslogar.setOnClickListener {
            btnDeslogar.startAnimation(clickAnimation)
            auth.signOut()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnVoltar.setOnClickListener {
            btnVoltar.startAnimation(clickAnimation)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()

        val textNameUser = findViewById<TextView>(R.id.name_user)
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val usersRef = FirebaseDatabase.getInstance().getReference("users")

            usersRef.child(userId).child("nome")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        nome = (dataSnapshot.value as? String).toString()
                        textNameUser.text = nome ?: "Nome não encontrado"
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("Firebase", "Erro ao ler nome do usuário: ${databaseError.message}")
                    }
                })
        }
    }

    fun msg(msg: String) {
        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
    }

    fun optionsProtocol(dataSnapshot: DataSnapshot) {
        var builder = AlertDialog.Builder(this)
        builder.setTitle("Escolha um Protocolo")
        builder.setItems(availableProtocols.toTypedArray()) { _, which ->
            selectedProtocol = availableProtocols[which]
            showDatails(dataSnapshot)
        }
        builder.show()
    }

    fun showDatails(dataSnapshot: DataSnapshot) {
        for (complaintSnapshot in dataSnapshot.children) {
            var protocolo = complaintSnapshot.child("protocolo").value.toString()

            if (protocolo == selectedProtocol) {

                val dateBD = complaintSnapshot.child("dataHora").value as? String
                val date = dateBD.toString()

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val dateTime = LocalDateTime.parse(date, formatter)

                val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                val data = dateTime.format(dateFormatter)

                val problema = complaintSnapshot.child("problema").value as? String
                val bairro = complaintSnapshot.child("bairro").value as? String
                val rua = complaintSnapshot.child("rua").value as? String
                val descricao = complaintSnapshot.child("descricao").value as? String
                val status = complaintSnapshot.child("status").value as? String

                val lblProblema = findViewById<TextView>(R.id.probema)
                val lblBairro = findViewById<TextView>(R.id.bairro)
                val lblRua = findViewById<TextView>(R.id.rua)
                val lblDesc = findViewById<TextView>(R.id.descricao)
                val lblDate = findViewById<TextView>(R.id.dateTime)
                val lblStatus = findViewById<TextView>(R.id.status)

                val btnConsultar = findViewById<Button>(R.id.btn_consultar)

                lblProblema.setText("Problema: $problema")
                lblProblema.visibility = View.VISIBLE
                lblBairro.setText("Bairro: $bairro")
                lblBairro.visibility = View.VISIBLE
                lblRua.setText("Rua: $rua")
                lblRua.visibility = View.VISIBLE
                lblDesc.setText("Descrição: $descricao")
                lblDesc.visibility = View.VISIBLE
                lblDate.setText("Data da Reclamação: $data")
                lblDate.visibility = View.VISIBLE
                lblStatus.setText("Status: $status")
                lblStatus.visibility = View.VISIBLE

                btnConsultar.visibility = View.GONE
            }
        }
    }
}