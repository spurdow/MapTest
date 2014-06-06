package com.maptest.background;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class GetLocation extends AsyncTask<String , String , String>{

	private ProgressDialog dialog;
	
	private Context context;
	
	public GetLocation(Context context){
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		dialog = new ProgressDialog(context);
		dialog.setIndeterminate(true);
		dialog.setTitle("GPS Location");
		dialog.setMessage("Please wait while we get your current location");
		dialog.show();
	}
	
	@Override
	protected String doInBackground(String... arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		dialog.dismiss();
		super.onPostExecute(result);
	}


	
	
}
