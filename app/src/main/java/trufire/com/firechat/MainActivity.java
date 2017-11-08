package trufire.com.firechat;

import com.firebase.ui.*;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;

import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity{
    private  String TAG= "firechat.MainActivity";
    private final static int RC_SIGN_IN=123;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         mAuth=FirebaseAuth.getInstance();
         if(mAuth.getCurrentUser()!=null){
                startActivity(new Intent(MainActivity.this,HubActivity.class));
         }else {
             //not signed in
             startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setIsSmartLockEnabled(!BuildConfig.DEBUG).build(),RC_SIGN_IN);
         }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                //startActivity();
                finish();
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(getApplicationContext(),"Sign in failed",Toast.LENGTH_SHORT);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(getApplicationContext(),"No Internet Connection",Toast.LENGTH_SHORT);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(getApplicationContext(),"Unknown error occurred",Toast.LENGTH_SHORT);
                    return;
                }
            }


        }
    }

}
