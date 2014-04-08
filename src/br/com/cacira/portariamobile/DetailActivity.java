package br.com.cacira.portariamobile;

import br.com.cacira.portariamobile.R.drawable;

import com.urbanairship.UAirship;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends Activity {

	SharedPreferences sharedPreferences;
	TextView txtVisitante;
	TextView txtData;
	TextView txtCondominio;
	EditText editTextObsMor;
	Boolean resposta;
	RelativeLayout layoutDetailResposta;
	TextView txtTituloObs;
	TextView txtObs;
	RelativeLayout layoutDetailRespondido;
	ImageView imageViewIcon;
	TextView txtRespondido;
	TextView txtObsMor;
	ScrollView mainScrollView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		mainScrollView = (ScrollView) findViewById(R.id.mainScrollView);
		
		txtVisitante = (TextView) findViewById(R.id.txtDetailVisitante);
		txtVisitante.setText(getIntent().getStringExtra("ent_visitante"));
		
		txtData = (TextView) findViewById(R.id.txtData);
		txtData.setText(getIntent().getStringExtra("ent_datacriacao"));

		editTextObsMor = (EditText) findViewById(R.id.editTextObsMor);
		
		layoutDetailResposta = (RelativeLayout) findViewById(R.id.layoutDetailResposta);
		layoutDetailRespondido = (RelativeLayout) findViewById(R.id.layoutDetailRespondido);
		
		imageViewIcon = (ImageView) findViewById(R.id.imageViewIcon);
		txtRespondido = (TextView) findViewById(R.id.txtRespondido);
		
		imageViewIcon = (ImageView) findViewById(R.id.imageViewIcon);
		txtRespondido = (TextView) findViewById(R.id.txtRespondido);
		txtObsMor = (TextView) findViewById(R.id.txtObsMor);
		
		if (!getIntent().getStringExtra("ent_autorizada").equals("-1")) {
			layoutDetailResposta.setVisibility(RelativeLayout.GONE);			
			layoutDetailRespondido.setVisibility(RelativeLayout.VISIBLE);
			
			if (getIntent().getStringExtra("ent_autorizada").equals("0")) {
				imageViewIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_red));
				txtRespondido.setText("ENTRADA NÃO AUTORIZADA");
			} else {
				imageViewIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_green));
				txtRespondido.setText("ENTRADA AUTORIZADA");
			}		
			
			txtObsMor.setText(getIntent().getStringExtra("ent_mor_obs") != null && !getIntent().getStringExtra("ent_mor_obs").equals("")?getIntent().getStringExtra("ent_mor_obs"):"Sem observações");
		}
		else {
			layoutDetailRespondido.setVisibility(RelativeLayout.GONE);
		}
		
		txtTituloObs = (TextView) findViewById(R.id.txtTituloObs);
		
		txtObs = (TextView) findViewById(R.id.txtObs);
		
		if (!getIntent().getStringExtra("ent_obs").trim().equals("")) {
			txtTituloObs.setText("Observação do porteiro " + getIntent().getStringExtra("por_nome"));
			txtObs.setText(getIntent().getStringExtra("ent_obs"));
		} else {
			txtTituloObs.setVisibility(View.GONE);
			txtObs.setVisibility(View.GONE);
		}
		
	}	
	
	public void butAutoriza_OnClick(View v) {
		resposta = true;
		new RespostaTask().execute();
	}
	
	public void butNaoAutoriza_OnClick(View v) {
		resposta = false;
		new RespostaTask().execute();
	}

	class RespostaTask extends AsyncTask<Void, Void, Boolean>
	{
		ProgressDialog pDialog;
		
		@Override
		protected void onPreExecute()
		{
			pDialog = new ProgressDialog(DetailActivity.this);
            pDialog.setMessage("Enviando...");
            pDialog.setCancelable(false);
            pDialog.show(); 
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			if (pDialog.isShowing())
                pDialog.dismiss();
			
			if (result) {
				layoutDetailResposta.setVisibility(RelativeLayout.GONE);
				layoutDetailRespondido.setVisibility(RelativeLayout.VISIBLE);
				
				if (!resposta) {
					imageViewIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_red));
					txtRespondido.setText("ENTRADA NÃO AUTORIZADA");
				} else {
					imageViewIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_green));
					txtRespondido.setText("ENTRADA AUTORIZADA");
				}	
				
				txtObsMor.setText(editTextObsMor.getText().toString().trim().equals("")?"Sem observações":editTextObsMor.getText());
				mainScrollView.invalidate();	
				
				// Sinaliza que precisa atualizar lista de entradas na próxima vez que mostrar
	            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(UAirship.shared().getApplicationContext());
	            SharedPreferences.Editor editor = sharedPreferences.edit();
	        	editor.putBoolean("precisaAtualizarListaEntradas", true);
	        	editor.commit();
			}
			else {
				Toast.makeText(UAirship.shared().getApplicationContext(), "Erro ao tentar enviar resposta. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected Boolean doInBackground(Void... v)
		{
			if (Util.respostaSolicitacaoEntrada(UAirship.shared().getApplicationContext(), resposta, getIntent().getStringExtra("ent_id"), editTextObsMor.getText().toString()))
			{				
				return true;		
			}			
			
			return false;
		}
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.detail, menu);
//		return true;
//	}

}
