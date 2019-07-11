package edu.cnm.deepdive.qodclient.controller

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AlertDialog.Builder
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.TooltipCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.cnm.deepdive.qodclient.R
import edu.cnm.deepdive.qodclient.model.Quote
import edu.cnm.deepdive.qodclient.service.GoogleSignInService
import edu.cnm.deepdive.qodclient.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    private var viewModel: MainViewModel? = null
    private var randomIgnored = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupViewModel()
        setupToolbar()
        setupSearch()
        setupFab()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_out -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onItemClick(adapterView: AdapterView<*>, view: View, position: Int, rowId: Long) {
        val quote = adapterView.getItemAtPosition(position) as? Quote
        val term = search_term?.text?.toString()?.trim()
        val title = if (term?.isEmpty() == true)
            getString(R.string.search_all)
        else
            getString(R.string.search_title_format, term)
        val next = if (position < 0 || position >= adapterView.count - 1)
            null
        else
            Runnable {
                search_results?.apply {
                    performItemClick(
                            null,
                            position + 1,
                            getItemIdAtPosition(position + 1)
                    )
                }
            }
        if (quote != null) {
            showQuote(quote, title, next)
        }
    }


    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java).apply {
            let { lifecycle.addObserver(it) }
            (this@MainActivity as? LifecycleOwner)?.let { owner ->
                randomQuote()?.observe(owner, androidx.lifecycle.Observer { quote ->
                    if (!randomIgnored) {
                        showQuote(quote, getString(R.string.random_quote_title), Runnable {
                            getRandomQuote()
                        })
                    }
                })
                searchResults()?.observe(owner, androidx.lifecycle.Observer { quotes ->
                    search_results?.adapter = ArrayAdapter<Quote>(
                            this@MainActivity,
                            R.layout.quote_list_item,
                            quotes)
                })
            }
        }
    }


    private fun setupSearch() {
        search.setOnClickListener { v ->
            viewModel?.search(search_term?.toString()?.trim())
        }
        clear.setOnClickListener { v -> }
        search_term?.text?.clear()
        viewModel?.search(null)


        search_results?.onItemClickListener = this
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun setupFab() {
        fab?.setOnClickListener { view ->
            randomIgnored = false
            viewModel?.getRandomQuote()
        }
        TooltipCompat.setTooltipText(fab, getString(R.string.random_quote_tooltip))
    }

    private fun showQuote(quote: Quote, title: String, nextAction: Runnable?) {
        val builder = Builder(this)
                .setMessage(quote.getCombinedText(getString(R.string.combined_quote_pattern),
                        getString(R.string.source_delimiter), getString(R.string.unknown_source)))
                .setTitle(title)
                .setNegativeButton(R.string.dialog_done) { dialogInterface, i -> }
        if (nextAction != null) {
            builder.setPositiveButton(R.string.dialog_next) { dialogInterface, i -> nextAction.run() }
        }
        builder.create().show()
    }

    private fun signOut() {
        val service = GoogleSignInService.getInstance()
        service.client.signOut().addOnCompleteListener { task ->
            service.account = null
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(Intent(this, LoginActivity::class.java).apply {
                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

}
