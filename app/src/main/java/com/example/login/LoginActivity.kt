package com.example.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class LoginActivity : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var editSenha: EditText
    private lateinit var btnEntrar: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val editEmail = findViewById<TextView>(R.id.edit_email)
        val editSenha = findViewById<TextView>(R.id.edit_senha)
        val btnEntrar = findViewById<Button>(R.id.btn_entrar)
        val buttonCadastro = findViewById<TextView>(R.id.text_tela_cadastro)

        val clickAnimation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation)

        btnEntrar.setOnClickListener {
            btnEntrar.startAnimation(clickAnimation)
            val email = editEmail.text.toString()
            val senha = editSenha.text.toString()

            if (email.isNullOrEmpty()) {
                msg(R.string.fill_email)
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                msg(R.string.invalid_email_format)
            } else if (senha.isNullOrEmpty()) {
                msg(R.string.fill_password)
            } else if (senha.length < 6) {
                msg(R.string.min_password_length)
            } else {
                autenticUser(email, senha)
            }
        }

        buttonCadastro.setOnClickListener {
            buttonCadastro.startAnimation(clickAnimation)
            val intent = Intent(this@LoginActivity, UserRegistrationActivity::class.java)

            // Iniciar a nova atividade
            startActivity(intent)
        }
    }

    fun autenticUser(email: String, senha: String) {
        val auth = FirebaseAuth.getInstance()

        val progressBar = findViewById<ProgressBar>(R.id.progressbar)
        progressBar.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    msg(R.string.succesLogin)
                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(this@LoginActivity, ServicesActivity::class.java)
                        startActivity(intent)
                        finish() // Finalizar a atividade atual para impedir que o usuário volte com o botão "voltar"
                    }, 3000) // Atraso de 3000 milissegundos (3 segundos)
                } else {
                    // Autenticação falhou
                    when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            // Senha incorreta
                            Toast.makeText(this@LoginActivity, "Email ou Senha incorreto", Toast.LENGTH_SHORT).show()
                            progressBar.visibility = View.INVISIBLE
                        }
                        else -> {
                            // Outra falha de autenticação
                            Toast.makeText(this@LoginActivity, "Falha na autenticação: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }

    override fun onStart() {
        super.onStart()

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {
            val intent = Intent(this@LoginActivity, ServicesActivity::class.java)
            startActivity(intent)
        }
    }

    fun msg(mensagem: Int) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }
}