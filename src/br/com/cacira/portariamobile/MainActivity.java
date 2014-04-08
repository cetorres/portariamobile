package br.com.cacira.portariamobile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.cacira.portariamobile.IntentReceiver.RespostaTask;

import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.push.BasicPushNotificationBuilder;
import com.urbanairship.push.PushManager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ListActivity {	
	
    JSONArray arrayEntradas = null;
    ArrayList<HashMap<String, Object>> entradasList;
    ProgressDialog pDialog;
    SharedPreferences sharedPreferences;
    Boolean carregaEntradasLocal;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);				
		        
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		if (!sharedPreferences.getBoolean("loginOk", false)) {
			Intent login = new Intent(getBaseContext(), LoginActivity.class);
			startActivity(login);
			finish();
		}
		else {		   
		   PushManager.enablePush();

		   // Obtém lista de entradas
		   if (sharedPreferences.getString("jsonListaEntradas","") != null && !sharedPreferences.getString("jsonListaEntradas","").equals("")) {
			   carregaEntradasLocal = true; 
		   } else {
			   carregaEntradasLocal = false;
		   }
		   
		   if (sharedPreferences.getBoolean("precisaAtualizarListaEntradas",false)) {
			   carregaEntradasLocal = false;
			   SharedPreferences.Editor editor = sharedPreferences.edit();
	           editor.putBoolean("precisaAtualizarListaEntradas", false);
	           editor.commit();
		   }
		   
	       new GetEntradas().execute();
		}
				
        ListView lv = getListView();
        
        // Listview on item click listener        
        lv.setOnItemClickListener(new OnItemClickListener() {
 
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
            	String ent_id = (String) entradasList.get(position).get("ent_id");
            	String mor_nome = (String) entradasList.get(position).get("mor_nome");
            	String ent_visitante = (String) entradasList.get(position).get("ent_visitante");
                String ent_obs = (String) entradasList.get(position).get("ent_obs");
                String ent_autorizada = (String) entradasList.get(position).get("ent_autorizada");
                String ent_datacriacao = (String) entradasList.get(position).get("ent_datacriacao");
                String ent_dataresposta = (String) entradasList.get(position).get("ent_dataresposta");
                String por_nome = (String) entradasList.get(position).get("por_nome");
                String mor_unidade = (String) entradasList.get(position).get("mor_unidade");
                String ent_mor_obs = (String) entradasList.get(position).get("ent_mor_obs");

                // Abre detalhe da entrada
                Intent in = new Intent(getApplicationContext(), DetailActivity.class);
                in.putExtra("ent_id", ent_id);
                in.putExtra("ent_visitante", ent_visitante);
                in.putExtra("mor_nome", mor_nome);
                in.putExtra("ent_obs", ent_obs);
                in.putExtra("ent_autorizada", ent_autorizada);
                in.putExtra("ent_datacriacao", ent_datacriacao);
                in.putExtra("ent_dataresposta", ent_dataresposta);
                in.putExtra("por_nome", por_nome);
                in.putExtra("mor_unidade", mor_unidade);
                in.putExtra("ent_mor_obs", ent_mor_obs);
                startActivity(in);
            }
            
        });
                
	}

	private class GetEntradas extends AsyncTask<Void, Void, Boolean> {
		 
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Carregando lista de entradas...");
            pDialog.setCancelable(true);
            if (!carregaEntradasLocal)
            	pDialog.show(); 
            
            entradasList = new ArrayList<HashMap<String, Object>>();
        }
 
        @Override
        protected Boolean doInBackground(Void... arg0) {
        	
            try {
                JSONObject jsonObj;
                
                if (carregaEntradasLocal) {
                	jsonObj = new JSONObject(sharedPreferences.getString("jsonListaEntradas",""));
                	carregaEntradasLocal = false;
                }
                else {
                	jsonObj = Util.getEntradas(getBaseContext(), sharedPreferences.getString("mor_id", ""));
                	
                	if (jsonObj == null) {
                		return false;
                		
                	}
                }
                
                if (jsonObj != null && jsonObj.getString("sucesso").equals("1")) {
                	arrayEntradas = jsonObj.getJSONArray("registros");
                	
                	for (int i = 0; i < arrayEntradas.length(); i++) {
                        JSONObject c = arrayEntradas.getJSONObject(i);
                        
                        String ent_id = c.getString("ent_id");
                        String mor_nome = c.getString("mor_nome");
                        String ent_visitante = c.getString("ent_visitante");
                        String ent_obs = c.getString("ent_obs");
                        String ent_autorizada = c.getString("ent_autorizada");
                        String ent_datacriacao = c.getString("ent_datacriacao");
                        String ent_dataresposta = c.getString("ent_dataresposta");
                        String por_nome = c.getString("por_nome");
                        String ent_mor_obs = c.getString("ent_mor_obs");
                        String mor_unidade = c.getString("mor_unidade");
                        
                        HashMap<String, Object> entrada = new HashMap<String, Object>();
                        
                        Date dateCriacao = null;
						try {
							dateCriacao = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(ent_datacriacao);
							
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        long milliseconds = dateCriacao.getTime();
                        long millisecondsFromNow = (new Date()).getTime() - milliseconds;
                        String ent_datacriacao_fuzzy = Util.millisToLongDHMS(millisecondsFromNow);
                        
                        entrada.put("ent_id", ent_id);
                        entrada.put("mor_nome", mor_nome);
                        entrada.put("ent_visitante", ent_visitante);
                        entrada.put("mor_unidade", mor_unidade);
                        entrada.put("ent_mor_obs", ent_mor_obs);
                        entrada.put("ent_obs", ent_obs);                        
                        entrada.put("ent_obs_label", "Observação: " + (ent_obs.equals("")?"-":ent_obs));
                        entrada.put("ent_autorizada", ent_autorizada);
                        entrada.put("ent_autorizada_imagem", (ent_autorizada.equals("-1")?R.drawable.icon_yellow:ent_autorizada.equals("1")?R.drawable.icon_green:R.drawable.icon_red));
                        entrada.put("ent_datacriacao", ent_datacriacao);
                        entrada.put("ent_datacriacao_fuzzy", ent_datacriacao_fuzzy);
                        entrada.put("ent_dataresposta", ent_dataresposta);
                        entrada.put("por_nome", por_nome);
                        entrada.put("por_nome_label", "Porteiro: " + por_nome);
                        
                        entradasList.add(entrada);
                	}
                }                	            
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
          
 
            return true;
        }
 
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            
            if (result) {
	            /**
	             * Updating parsed JSON data into ListView
	             * */
	            ListAdapter adapter = new SimpleAdapter(
	                    MainActivity.this, entradasList,
	                    R.layout.list_item, new String[] { 
	                    		"ent_autorizada_imagem",
	                    		"ent_visitante", 
	                    		"ent_obs_label",
	                            "por_nome_label",
	                            "ent_datacriacao_fuzzy"
	                    		}, new int[] { 
	                    		R.id.icon,
	                    		R.id.visitante,
	                            R.id.obs, 
	                            R.id.porteiro,
	                            R.id.data_criacao
	                    });
	 
	            setListAdapter(adapter);
            }
            else {
            	Toast.makeText(getBaseContext(), "Não possível obter lista atualizada. Verifique acesso à internet.", Toast.LENGTH_SHORT).show();
            }
        }
 
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      	case R.id.action_refresh: {
	      	new GetEntradas().execute();
	      	break;
      	}
        case R.id.action_settings: {
        	Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
        	startActivity(settingsActivity);
        	break;
        }
        case R.id.action_logout: {
        	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(UAirship.shared().getApplicationContext());	  
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("loginOk", false);
            editor.putString("mor_id", "");
            editor.putString("mor_nome", "");
            editor.putString("mor_email", "");
            editor.putString("con_nome", "");
            editor.putString("jsonListaEntradas","");
            editor.commit();
            
            // Urban Airship Notifications
 		    PushManager.disablePush();
            
        	Intent login = new Intent(getBaseContext(), LoginActivity.class);
        	startActivity(login);
        	
        	finish();
        	break;
        }        
        case R.id.action_about: {
        	PackageInfo pInfo = null;
    		try {
    			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
    		} catch (NameNotFoundException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		
        	final SpannableString message = new SpannableString(getString(R.string.app_name) + " v. " + pInfo.versionName + "\n\n" + getString(R.string.descricao) +  "\n\n" + getString(R.string.sobre_empresa));
        	
        	Linkify.addLinks(message, Linkify.ALL);

        	final AlertDialog d = new AlertDialog.Builder(this)
            .setMessage(message)
            .setTitle(R.string.app_name)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton("Ok", null)
        	.create();
        	
        	d.show();
        	
        	// Make the textview clickable. Must be called after show()
            ((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

        	break;
        }

      }
      return super.onOptionsItemSelected(item);
    }
	
//	private void showNotification() {
//
//		if (getIntent().getExtras() != null && !getIntent().getExtras().getString(PushManager.EXTRA_ALERT).equals("")) {
//	    	
//	    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());	  
//	    	
//	    	if (!sharedPreferences.getBoolean("mostrouNotificacao", false)) {		    	
//				final AlertDialog alert = new AlertDialog.Builder(MainActivity.this)		            
//			    .setTitle(getIntent().getExtras().getString(PushManager.EXTRA_ALERT))
//			    .setMessage(getIntent().getExtras().getString("visitante") + " deseja entrar. Posso autorizar?")  	    
//			    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
//			        public void onClick(DialogInterface dialog, int whichButton) {
//			        	Toast.makeText(MainActivity.this, "Usuário autorizou entrada.", Toast.LENGTH_SHORT).show();
// 
//			        	finish();
//			        }
//			    })
//			    .setNegativeButton("Não",null)	    	    
//			    .create();
//		    	alert.show();
//		    	
//		    	// Salva notificação recebida
//	        	SharedPreferences.Editor editor = sharedPreferences.edit();
//	        	editor.putBoolean("mostrouNotificacao", true);
//	        	editor.commit();
//	        	
//	        	
//	    	}
//		}
//	}
	
	@Override
	public void onStart() {
	    super.onStart();
	    UAirship.shared().getAnalytics().activityStarted(this);	    	    
	    	    	    
	    //showNotification();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		if (sharedPreferences.getBoolean("precisaAtualizarListaEntradas",false)) {
		   carregaEntradasLocal = false;
		   SharedPreferences.Editor editor = sharedPreferences.edit();
           editor.putBoolean("precisaAtualizarListaEntradas", false);
           editor.commit();
           new GetEntradas().execute();
	   }
       
	}

	@Override
	public void onStop() {
	    super.onStop();
	    UAirship.shared().getAnalytics().activityStopped(this);
	}

}
