package edu.cnm.deepdive.qodclient

import android.app.Application
import edu.cnm.deepdive.qodclient.service.GoogleSignInService

class QodApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        GoogleSignInService.setContext(this)
        // This is where we would initialize Stetho, Picasso, etc.
        // This is also where we could do some non-trivial DB operation to force database creation.
    }

}
