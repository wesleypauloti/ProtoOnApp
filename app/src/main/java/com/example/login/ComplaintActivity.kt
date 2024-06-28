package com.example.login

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ComplaintActivity : AppCompatActivity() {

    private lateinit var spinProb: Spinner
    private lateinit var editCep: EditText
    private lateinit var editBairro: EditText
    private lateinit var editRua: EditText
    private lateinit var editNum: EditText
    private lateinit var editDesc: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var btnVoltar: TextView
    private lateinit var btnDeslogar: ImageView

    private lateinit var selectedProblem: String
    private lateinit var textNameUser: TextView
    private lateinit var nome: String

    private var auth = FirebaseAuth.getInstance()

    val database = FirebaseDatabase.getInstance()
    val complaintsRef = database.getReference("reclamacoes")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complaint)

        spinProb = findViewById(R.id.spinner_problem)
        editCep = findViewById(R.id.edit_cep)
        editBairro = findViewById(R.id.edit_bairro)
        editRua = findViewById(R.id.edit_rua)
        editNum = findViewById(R.id.edit_num)
        editDesc = findViewById(R.id.edit_desc)

        btnRegistrar = findViewById(R.id.btn_registrar)
        btnVoltar = findViewById(R.id.text_voltar)
        btnDeslogar = findViewById(R.id.exit)

        val clickAnimation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation)

        val problemaOptions = resources.getStringArray(R.array.problema_options).toMutableList()
        val hint = "Selecione um Problema"
        problemaOptions.add(0, hint)

        val adapter = ArrayAdapter<String>(this, R.layout.spinner_item, problemaOptions)

// Defina o estilo do dropdown
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

// Configure o adaptador no Spinner
        spinProb.adapter = adapter

        spinProb.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Verifica se o hint está presente e remove
                if (problemaOptions.contains(hint)) {
                    problemaOptions.remove(hint)
                    adapter.notifyDataSetChanged()
                }
            }
            false
        }

        spinProb.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                // Lógica para lidar com a seleção do usuário
                selectedProblem = problemaOptions[position]
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Ação quando nada é selecionado
            }
        })

        editCep.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Nada a fazer aqui
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Nada a fazer aqui
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Remova o TextWatcher temporariamente para evitar chamadas recursivas
                editCep.removeTextChangedListener(this)

                // Obtém apenas os dígitos do texto atual
                val cepText = s.toString().replace("[^\\d]".toRegex(), "")

                // Adiciona um hífen após o quinto dígito
                val formattedCep = if (cepText.length >= 5) {
                    "${cepText.substring(0, 5)}-${cepText.substring(5)}"
                } else {
                    cepText
                }
                if (cepText.length == 8) {
                    // Faça a busca do CEP e preencha os campos necessários
                    buscarCep(cepText)
                }

                // Define o texto formatado no EditText
                editCep.setText(formattedCep)

                // Move o cursor para o final do texto
                editCep.setSelection(formattedCep.length)

                // Adiciona o TextWatcher de volta
                editCep.addTextChangedListener(this)
            }
        })

        btnRegistrar.setOnClickListener {
            btnRegistrar.startAnimation(clickAnimation)
            if (selectedProblem == "Selecione um Problema") {
                Toast.makeText(baseContext, "Selecione um Problema", Toast.LENGTH_SHORT).show()
            } else if (editBairro.text.isEmpty()) {
                Toast.makeText(baseContext, "Digite o Bairro", Toast.LENGTH_SHORT).show()
            } else if (editRua.text.isEmpty()) {
                Toast.makeText(baseContext, "Digite a Rua", Toast.LENGTH_SHORT).show()
            } else if (editNum.text.isEmpty()) {
                Toast.makeText(baseContext, "Digite o Número", Toast.LENGTH_SHORT).show()
            } else if (editDesc.text.isEmpty()) {
                Toast.makeText(baseContext, "Descreva a situação", Toast.LENGTH_SHORT).show()
            } else {
                registerProblem()
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

    fun registerProblem() {
        // Obtém o último número de protocolo
        complaintsRef.orderByChild("protocolo").limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var ultimoProtocolo = 0

                    // Verifica se há algum registro
                    if (dataSnapshot.exists()) {
                        for (registro in dataSnapshot.children) {
                            // Obtém o último número de protocolo
                            val protocolo = registro.child("protocolo").value.toString()
                            val numeroProtocolo = protocolo.split("/")[0].toInt()

                            // Incrementa o último número de protocolo
                            ultimoProtocolo = numeroProtocolo + 1
                        }
                    }

                    // Cria o novo número de protocolo
                    val novoProtocolo = String.format("%05d", ultimoProtocolo)
                    val anoAtual = LocalDateTime.now().year
                    val protocoloCompleto = "$novoProtocolo/$anoAtual"

                    // Restante do código para obter outros dados e registrar no banco de dados
                    val currentDateTime = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    val dataHora = currentDateTime.format(formatter)

                    val bairro = editBairro.text.toString()
                    val rua = editRua.text.toString()
                    val descricao = editDesc.text.toString()
                    val nameUser = textNameUser.text
                    val status = "Em Análise"

                    val complaintsData = mapOf(
                        "protocolo" to protocoloCompleto,
                        "problema" to selectedProblem,
                        "requerente" to nameUser,
                        "bairro" to bairro,
                        "rua" to rua,
                        "dataHora" to dataHora,
                        "descricao" to descricao,
                        "status" to status
                    )

                    // Registra no banco de dados
                    complaintsRef.push().setValue(complaintsData)
                        .addOnSuccessListener {
                            msg(R.string.register_success)
                        }
                        .addOnFailureListener {
                            val errorMessage = "Falha no registro: ${it.message}"
                            Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Erro ao obter o último número de protocolo: ${databaseError.message}")
                }
            })
    }

    fun buscarCep(cepText: String) {
        editCep.filters = arrayOf<InputFilter>(InputFilter.AllCaps(), InputFilter.LengthFilter(9))

        val retrofit = Retrofit.Builder()
            .baseUrl("https://viacep.com.br/ws/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val viaCepService = retrofit.create(ViaCepService::class.java)

        val cepValue = editCep.text.toString()
        val call = viaCepService.getCepData(cepValue)


        call.enqueue(object : retrofit2.Callback<CepResponse> {
            override fun onResponse(call: Call<CepResponse>, response: retrofit2.Response<CepResponse>) {
                if (response.isSuccessful) {
                    val cepResponse = response.body()
                    cepResponse?.bairro.also { editBairro.setText(it) }
                    cepResponse?.logradouro.also { editRua.setText(it) }

                } else {
                    // Trate erros aqui
                }
            }

            override fun onFailure(call: Call<CepResponse>, t: Throwable) {
                // Trate falhas de comunicação aqui
            }
        })
    }

    override fun onStart() {
        super.onStart()

        textNameUser = findViewById<TextView>(R.id.name_user)
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

    fun EditText.isCampoVazio(mensagem: Int): Boolean {
        if (text.isNullOrEmpty()) {
            msg(mensagem)
            return true
        }
        return false
    }

    fun msg(mensagem: Int) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }
}