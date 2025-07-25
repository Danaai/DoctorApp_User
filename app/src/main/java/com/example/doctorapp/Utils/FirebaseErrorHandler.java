package com.example.doctorapp.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.database.DatabaseError;

public class FirebaseErrorHandler {
    public static void handleError(Context context, DatabaseError error, String defaultMessage) {
        Log.e("FirebaseError", defaultMessage + ": " + error.getMessage());
        if (context != null) {
            Toast.makeText(context, defaultMessage, Toast.LENGTH_SHORT).show();
        }
    }
}