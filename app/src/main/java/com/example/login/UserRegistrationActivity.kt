package com.example.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class UserRegistrationActivity : AppCompatActivity() {

    private lateinit var editNome: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPhone: EditText
    private lateinit var editSenha: EditText
    private lateinit var editconfSenha: EditText
    private lateinit var btnCadastrar: Button
    private var auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_registration)


        val textVoltar = findViewById<TextView>(R.id.text_voltar)

        editNome = findViewById<EditText>(R.id.edit_nome)
        editEmail = findViewById<EditText>(R.id.edit_email)
        editPhone = findViewById<EditText>(R.id.edit_phone)
        editSenha = findViewById<EditText>(R.id.edit_senha)
        editconfSenha = findViewById<EditText>(R.id.edit_conf_senha)
        btnCadastrar = findViewById<Button>(R.id.btn_cadastrar)

        val clickAnimation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation)

        textVoltar.setOnClickListener {
            textVoltar.startAnimation(clickAnimation)
            finish()
        }

        editPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Nada a ser feito após a modificação do texto
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Nada a ser feito antes da modificação do texto
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Remove qualquer caractere não numérico do texto
                val digitsOnly = s.toString().replace("[^\\d]".toRegex(), "")

                // Formata o número de telefone (se tiver pelo menos 2 dígitos)
                if (digitsOnly.length >= 2) {
                    val formattedNumber = "(" + digitsOnly.substring(0, 2) + ")" +
                            digitsOnly.substring(2, minOf(7, digitsOnly.length)) +
                            if (digitsOnly.length >= 8) "-" + digitsOnly.substring(7, minOf(11, digitsOnly.length)) else ""

                    // Define o texto formatado no campo de telefone
                    editPhone.removeTextChangedListener(this)
                    editPhone.setText(formattedNumber)
                    editPhone.setSelection(formattedNumber.length)
                    editPhone.addTextChangedListener(this)
                }
            }
        })

        btnCadastrar.setOnClickListener {
            btnCadastrar.startAnimation(clickAnimation)

            if (editNome.isCampoVazio(R.string.fill_name)) {
            } else if (editNome.text.toString().length < 3) {
                msg(R.string.min_name_length)
            } else if (editNome.text.toString().any { it.isDigit() }) {
                msg(R.string.no_digits_in_name)
            } else if (editEmail.isCampoVazio(R.string.fill_email)) {
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(editEmail.text.toString()).matches()) {
                msg(R.string.invalid_email_format)
            } else if (editPhone.text.isEmpty()) {
                Toast.makeText(baseContext, "Digite o número de telefone", Toast.LENGTH_SHORT).show()
            } else if (editPhone.text.toString().length < 10) {
                Toast.makeText(baseContext, "Digite pelo menos 14 digítos para o telefone", Toast.LENGTH_SHORT).show()
            } else if (editSenha.isCampoVazio(R.string.fill_password)) {
            } else if (editSenha.text.toString().length < 6) {
                msg(R.string.min_password_length)
            } else if (!editconfSenha.text.toString().equals(editSenha.text.toString())) {
                msg(R.string.password_mismatch)
            } else {
                registerUser(editNome, editEmail, editPhone, editSenha)
            }
        }
    }

    fun registerUser(nome: EditText, email: EditText, telefone: EditText, senha: EditText) {
        val nomeText = nome.text.toString()
        val emailText = email.text.toString()
        val phoneText = telefone.text.toString()
        val senhaText = senha.text.toString()

        auth.createUserWithEmailAndPassword(emailText, senhaText)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    msg(R.string.signup_success)
                    val user = auth.currentUser
                    val database = FirebaseDatabase.getInstance()
                    val usersRef = database.getReference("users")

                    val userId = user?.uid ?: ""
                    val currentTime = Calendar.getInstance().time
                    val userData = mapOf(
                        "nome" to nomeText,
                        "email" to emailText,
                        "telefone" to phoneText,
                        "dateTimeRegistro" to currentTime.toString()
                    )
                    usersRef.child(userId).setValue(userData)
                    auth.signOut()
                } else {
                    // Se o registro falhou, exiba uma mensagem para o usuário
                    Toast.makeText( applicationContext, "Falha no registro: Email já cadastrado", Toast.LENGTH_SHORT).show()
                }
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