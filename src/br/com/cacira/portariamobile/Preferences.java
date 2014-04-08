package br.com.cacira.portariamobile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import android.widget.Toast;
import br.com.cacira.portariamobile.IntentReceiver.RespostaTask;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;
import com.urbanairship.preference.UAPreferenceAdapter;
import com.urbanairship.push.PushManager;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private UAPreferenceAdapter preferenceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AirshipConfigOptions options = UAirship.shared().getAirshipConfigOptions();

        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Only add the push preferences if the pushServiceEnabled is true
        //if (options.pushServiceEnabled) {
            this.addPreferencesFromResource(R.xml.preferences);
        //}

        // Only add the location preferences if the locationServiceEnabled is true
        /*
        if (options.locationOptions.locationServiceEnabled) {
            this.addPreferencesFromResource(R.xml.location_preferences);
        }

        // Display the advanced settings
        if (options.pushServiceEnabled) {
            this.addPreferencesFromResource(R.xml.advanced_preferences);
        }
		*/
        // Creates the UAPreferenceAdapter with the entire preference screen
        preferenceAdapter = new UAPreferenceAdapter(getPreferenceScreen());
        
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        EditTextPreference pref_condominio = (EditTextPreference) findPreference("condominio_preference");
        EditTextPreference pref_nome = (EditTextPreference) findPreference("nome_preference");
        EditTextPreference pref_email = (EditTextPreference) findPreference("email_preference");
        pref_condominio.setSummary(sharedPreferences.getString("con_nome", ""));
        pref_nome.setSummary(sharedPreferences.getString("mor_nome", ""));
        pref_email.setSummary(sharedPreferences.getString("mor_email", ""));

    }        
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        if (key.equals("login_preference")) {
//            Preference connectionPref = findPreference(key);
//            connectionPref.setSummary(sharedPreferences.getString(key, ""));
//        }
//        
//        if (key.equals("pass_preference")) {
//        	Preference connectionPref = findPreference(key);
//            if (!sharedPreferences.getString(key, "").trim().equals("")) {            	
//                connectionPref.setSummary("**********");
//            }
//            else {
//            	connectionPref.setSummary("");
//            }            	
//        }
//        
//        if (key.equals("login_preference") || key.equals("pass_preference")) {
//        	if (!sharedPreferences.getString("login_preference", "").trim().equals("") 
//        		&& !sharedPreferences.getString("pass_preference", "").trim().equals("")) {
//	        	String[] params = {sharedPreferences.getString("login_preference", ""), sharedPreferences.getString("pass_preference", "")};
//	        	new LoginTask().execute(params);
//        	}
//        }
    }
    
    /*
    class LoginTask extends AsyncTask<String, Void, Boolean>
	{
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(Preferences.this, "", "Realizando login...", true);
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			progressDialog.dismiss();
			
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(UAirship.shared().getApplicationContext());	  
            SharedPreferences.Editor editor = sharedPreferences.edit();
            
			if (result) {
				Toast.makeText(Preferences.this, "Login com sucesso!", Toast.LENGTH_SHORT).show();				
	            editor.putBoolean("loginOk", true);	 
	            // Muda o alias para o push para o email que logou com sucesso
	            PushManager.shared().setAlias(sharedPreferences.getString("login_preference", ""));
			}
			else {
				Toast.makeText(Preferences.this, "Erro de login. Verifique o usuário e a senha.", Toast.LENGTH_SHORT).show();
				editor.putBoolean("loginOk", false);				
				PushManager.shared().setAlias("");
			}
			
			editor.commit();
		}

		@Override
		protected Boolean doInBackground(String... params)
		{
			if (Util.loginUsuario(Preferences.this, params[0], params[1]))
			{
				return true;		
			}
			
			return false;
		}
	}
	*/
    
    @Override
    protected void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    protected void onStart() {
        super.onStart();

        // Activity instrumentation for analytic tracking
        UAirship.shared().getAnalytics().activityStarted(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Activity instrumentation for analytic tracking
        UAirship.shared().getAnalytics().activityStopped(this);

        // Apply any changed UA preferences from the preference screen
        preferenceAdapter.applyUrbanAirshipPreferences();
    }
}
