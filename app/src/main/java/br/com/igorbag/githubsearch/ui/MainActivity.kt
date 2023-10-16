package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {

    lateinit var nomeUsuario: EditText
    lateinit var btnConfirmar: Button
    lateinit var listaRepositories: RecyclerView
    lateinit var githubApi: GitHubService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        showUserName()
        setupRetrofit()
        getAllReposByUserName()
    }

    // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
    fun setupView() {

        nomeUsuario = findViewById(R.id.et_nome_usuario)
        btnConfirmar = findViewById(R.id.btn_confirmar)
        listaRepositories = findViewById(R.id.rv_lista_repositories)
    }

    //metodo responsavel por configurar os listeners click da tela
    private fun setupListeners() {

        btnConfirmar.setOnClickListener {
            // Ação a ser executada quando o botão for clicado
            saveUserLocal()
        }
    }


    // salvar o usuario preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal() {

        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("username", nomeUsuario.text.toString())
        editor.apply()
    }

    private fun showUserName() {

        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val savedUsername = sharedPref.getString("username", "")
        if (savedUsername.isNullOrBlank()) {
            nomeUsuario.setText(savedUsername)
        }
    }

    //Metodo responsavel por fazer a configuracao base do Retrofit
    fun setupRetrofit() {

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        githubApi = retrofit.create(GitHubService::class.java)
    }

    //Metodo responsavel por buscar todos os repositorios do usuario fornecido
    fun getAllReposByUserName() {
        val nomeUsuarioEditText = findViewById<EditText>(R.id.et_nome_usuario)
        val username = nomeUsuarioEditText.text.toString()
        val call = githubApi.getAllRepositoriesByUser(username)
        call.enqueue(object : Callback<List<Repository>> {
            override fun onResponse(call: Call<List<Repository>>, response: Response<List<Repository>>) {
                if (response.isSuccessful) {
                    val repositories = response.body()
                    if (repositories != null) {
                        setupAdapter(repositories)
                    }
                }
            }

            override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                // Lidar com o erro
            }
        })
    }

    // Metodo responsavel por realizar a configuracao do adapter
    fun setupAdapter(list: List<Repository>) {

        val adapter = RepositoryAdapter(list) // Crie um adaptador personalizado com sua lista de repositórios
        listaRepositories.adapter = adapter

    }



    // Metodo responsavel por compartilhar o link do repositorio selecionado

    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // Metodo responsavel por abrir o browser com o link informado do repositorio


    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )

    }

}