package br.com.cacira.portariamobile;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.InputType;
//import android.content.SharedPreferences;
//import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.urbanairship.UAirship;
import com.urbanairship.push.GCMMessageHandler;
import com.urbanairship.push.PushManager;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

public class IntentReceiver extends BroadcastReceiver {

    private static final String logTag = "PortariaMobilePush";
    public static String APID_UPDATED_ACTION_SUFFIX = ".apid.updated";
    String ent_id;
    boolean resposta;
    EditText editObs;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(logTag, "Received intent: " + intent.toString());
        String action = intent.getAction();

        if (action.equals(PushManager.ACTION_PUSH_RECEIVED)) {

            int id = intent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID, 0);

            Log.i(logTag, "Received push notification. Alert: "
                    + intent.getStringExtra(PushManager.EXTRA_ALERT)
                    + " [NotificationID="+id+"]");

            logPushExtras(intent);

        } else if (action.equals(PushManager.ACTION_NOTIFICATION_OPENED)) {

            Log.i(logTag, "User clicked notification. Message: " + intent.getStringExtra(PushManager.EXTRA_ALERT));

            logPushExtras(intent);
            
            ent_id = intent.getStringExtra("ent_id");
            
            // Sinaliza que precisa atualizar lista de entradas na próxima vez que mostrar
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(UAirship.shared().getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
        	editor.putBoolean("precisaAtualizarListaEntradas", true);
        	editor.commit();
            
            // Abre detalhe da entrada
            Intent in = new Intent();
            in.setClass(UAirship.shared().getApplicationContext(), DetailActivity.class);
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);            
            in.putExtra("ent_id", intent.getStringExtra("ent_id"));
            in.putExtra("ent_visitante", intent.getStringExtra("ent_visitante"));
            in.putExtra("ent_obs", intent.getStringExtra("ent_obs"));
            in.putExtra("ent_datacriacao", intent.getStringExtra("ent_datacriacao"));
            in.putExtra("por_nome", intent.getStringExtra("por_nome"));
            in.putExtra("ent_autorizada", "-1");            
            UAirship.shared().getApplicationContext().startActivity(in);
            
//            editObs = new EditText(UAirship.shared().getApplicationContext());
//            editObs.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
//            editObs.requestFocus();
//    	    
//            final AlertDialog alert = new AlertDialog.Builder(UAirship.shared().getApplicationContext())	            
//		    .setTitle(intent.getStringExtra(PushManager.EXTRA_ALERT))
//		    .setMessage(intent.getStringExtra("visitante") + " " + UAirship.shared().getApplicationContext().getString(R.string.perguntaAutorizacao)) 
//		    .setView(editObs)
//		    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
//		        public void onClick(DialogInterface dialog, int whichButton) {
//		        	resposta = true;		        
//		        	new RespostaTask().execute();
//		        }
//		    })
//		    .setNegativeButton("Não",new DialogInterface.OnClickListener() {
//		        public void onClick(DialogInterface dialog, int whichButton) {
//		        	resposta = false;	
//		        	new RespostaTask().execute();
//		        }
//		    })	    	    
//		    .create();
//            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//	    	alert.show();	    	

//            // Informa que a notificação não foi mostrada ainda
//            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(UAirship.shared().getApplicationContext());	  
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//        	editor.putBoolean("mostrouNotificacao", false);
//        	editor.commit();
//	    			
//        	// Abre a activity principal com os parâmetros
//            Intent launch = new Intent(Intent.ACTION_MAIN);
//            launch.putExtras(intent.getExtras());            
//            launch.setClass(UAirship.shared().getApplicationContext(), MainActivity.class);
//            launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            UAirship.shared().getApplicationContext().startActivity(launch);

        } else if (action.equals(PushManager.ACTION_REGISTRATION_FINISHED)) {
            Log.i(logTag, "Registration complete. APID:" + intent.getStringExtra(PushManager.EXTRA_APID)
                    + ". Valid: " + intent.getBooleanExtra(PushManager.EXTRA_REGISTRATION_VALID, false));

            // Notify any app-specific listeners
            Intent launch = new Intent(UAirship.getPackageName() + APID_UPDATED_ACTION_SUFFIX);
            UAirship.shared().getApplicationContext().sendBroadcast(launch);

        } else if (action.equals(GCMMessageHandler.ACTION_GCM_DELETED_MESSAGES)) {
            Log.i(logTag, "The GCM service deleted "+intent.getStringExtra(GCMMessageHandler.EXTRA_GCM_TOTAL_DELETED)+" messages.");
        }

    }
    
    class RespostaTask extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected void onPreExecute()
		{
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			if (result) {
				if (resposta)
					Toast.makeText(UAirship.shared().getApplicationContext(), "Morador autorizou entrada.", Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(UAirship.shared().getApplicationContext(), "Morador não autorizou entrada.", Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(UAirship.shared().getApplicationContext(), "Erro ao tentar enviar resposta. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected Boolean doInBackground(Void... v)
		{
			if (Util.respostaSolicitacaoEntrada(UAirship.shared().getApplicationContext(), resposta, ent_id, editObs.getText().toString()))
			{
				return true;		
			}
			
			return false;
		}
	}

    /**
     * Log the values sent in the payload's "extra" dictionary.
     * 
     * @param intent A PushManager.ACTION_NOTIFICATION_OPENED or ACTION_PUSH_RECEIVED intent.
     */
    private void logPushExtras(Intent intent) {
        Set<String> keys = intent.getExtras().keySet();
        for (String key : keys) {

            //ignore standard C2DM extra keys
            List<String> ignoredKeys = (List<String>)Arrays.asList(
                    "collapse_key",//c2dm collapse key
                    "from",//c2dm sender
                    PushManager.EXTRA_NOTIFICATION_ID,//int id of generated notification (ACTION_PUSH_RECEIVED only)
                    PushManager.EXTRA_PUSH_ID,//internal UA push id
                    PushManager.EXTRA_ALERT);//ignore alert
            if (ignoredKeys.contains(key)) {
                continue;
            }
            Log.i(logTag, "Push Notification Extra: ["+key+" : " + intent.getStringExtra(key) + "]");
        }
    }
}
