package com.example.appdevfinal;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;

public class BukSUComelecApp extends Application {
    private static final String TAG = "BukSUComelecApp";
    
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Initializing Firebase App Check in Debug Mode");
        }
        
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance());
    }
}
