package com.example.irkrusmser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	protected static final int PICK_RESULT = 0;
	protected static final int ReqCodeContact = 0;
	
	TextView txtPhoneNumber;
	TextView txtSMSText;
	TextView txtCaptcha1;
	TextView txtError;
	Button buttonSend;
	Button buttonSelectContact;
	ImageView imgCaptcha;
	ImageView imgStatus;
	
    private String _cookie = "";
    private String strCaptcha0 = "";
    private String strMyName = "";
    
    // ���������� ���� �������� ����
    final int PROGRESS_DLG_ID = 666;
  //������ ��������
    private ProgressDialog pd;
    private ProgressDialog pdSMS;
    
    // ��� ���������� ��������
    SharedPreferences sp;
    
    
	// ���������� ���������� �� � ���������
	public boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo nInfo = cm.getActiveNetworkInfo();
	    if (nInfo != null && nInfo.isConnected()) {
	        //Log.v("status", "ONLINE");
	        return true;
	    }
	    else {
	        //Log.v("status", "OFFLINE");
	        return false;
	    }
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //isOnline();
        // ������ View-��������
        txtPhoneNumber = (TextView) findViewById(R.id.editPhoneNumber);
        txtSMSText = (TextView) findViewById(R.id.editMessage);
        //txtName = (TextView) findViewById(R.id.editName);
        txtCaptcha1 = (TextView) findViewById(R.id.editCaptcha1);
        txtError = (TextView) findViewById(R.id.textError);

        buttonSend = (Button) findViewById(R.id.btnSend);
        buttonSelectContact = (Button) findViewById(R.id.btnContacts);
        imgCaptcha = (ImageView) findViewById(R.id.imageCaptcha1);
        imgStatus = (ImageView) findViewById(R.id.imageStatus);
        
        /*  ��������� ���������. ���� �������� � ����� ������ ���� - 
        ���������� ������ ��������. � ������ ������ ������.  */		
        
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        
        strMyName = sp.getString("Name","");
        
        /* ��������� � ��������� ���� ����������� ��������� */        
        //txtName.setText(name);
        
        // �������� �� ����������� � ��������
        if (isOnline() == true){
        	pd = ProgressDialog.show(MainActivity.this, "���������...", "��������� �����", true, false);
        	// �������� �����
        	new DownloadImageTask().execute("http://irk.ru/sms");
        }
        else{
        	txtError.setText("�� �� ���������� � ���� ��������.");
        }  
        // ���������� ������� �� ������ ���������
        buttonSend.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		if (txtPhoneNumber.length() < 1)
        		{
        			Toast.makeText(getApplicationContext(), "������� ����� ��������!", Toast.LENGTH_SHORT).show();
        			return;
        		}
        		
        		if (txtSMSText.length() < 1)
        		{
        			Toast.makeText(getApplicationContext(), "������� ����� ���!", Toast.LENGTH_SHORT).show();
        			return;
        		}
        		
        		if (txtCaptcha1.length() < 1)
        		{
        			Toast.makeText(getApplicationContext(), "������� ���-���!", Toast.LENGTH_SHORT).show();
        			return;
        		}
        		
        		if (isOnline() == true){
	        		String data_s = "csrfmiddlewaretoken=" + GetToken(_cookie) + "&number=" + txtPhoneNumber.getText() + "&message=" + txtSMSText.getText() + "\n" + strMyName + "&captcha_0=" + strCaptcha0 + "&captcha_1=" + txtCaptcha1.getText();
	        		imgStatus.setVisibility(View.INVISIBLE);
	        		pdSMS = ProgressDialog.show(MainActivity.this, "���������...", "�������� ���", true, false);
	        		new SendSMSTask().execute("http://irk.ru/sms/?", data_s);
	        			//Log.v("status", "SEND");
	        		pd = ProgressDialog.show(MainActivity.this, "���������...", "��������� �����", true, false);
	        		// �������� �����
	        		new DownloadImageTask().execute("http://irk.ru/sms");
	        		
        		}
        		else{
        			Toast.makeText(getApplicationContext(), "�� �� ���������� � ���� ��������.", Toast.LENGTH_SHORT).show();
        		}
        	}
        });
        
        // ���������� ������� �� ����� (���������� �����)
        imgCaptcha.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		if (isOnline() == true){
        			pd = ProgressDialog.show(MainActivity.this, "���������...", "��������� �����", true, false);
	        		// �������� �����
        			new DownloadImageTask().execute("http://irk.ru/sms");
        		}
        		else{
        			Toast.makeText(getApplicationContext(), "�� �� ���������� � ���� ��������.", Toast.LENGTH_SHORT).show();
        		}
        	}
        });
        
        // ���������� ������ ��������
        buttonSelectContact.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		// ����� ������ ��������� ������� (��� ��������)
                Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pickIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                startActivityForResult(pickIntent, PICK_RESULT);
        	}
        });
    }
  
    // �������� ����������
	@Override
    protected void onStop(){
       super.onStop();
 
       //String name = txtName.getText().toString().trim();
 
      /* ��������� �������� �������� � ��������� ����� �������� */
 
      //SharedPreferences settings = getPreferences(0);
      //SharedPreferences.Editor editor = settings.edit();
      //editor.putString("Name", strMyName);
 
      /* ��������� ������. ���� �� ��������� - ������ �� ���������� =) */
      //editor.commit();
    }

    // ��������� ������ ��������
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri uri = data.getData();

            if (uri != null) {
                Cursor c = null;
                try {
                    c = getContentResolver().query(uri, new String[]{ 
                                ContactsContract.CommonDataKinds.Phone.NUMBER,  
                                ContactsContract.CommonDataKinds.Phone.TYPE },
                            null, null, null);

                    if (c != null && c.moveToFirst()) {
                        String number = c.getString(0);
                        int type = c.getInt(1);
                        showSelectedNumber(type, number);
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }

    // ������������ �������� ������ �������� 
    public void showSelectedNumber(int type, String number) {
    	txtPhoneNumber.setText(number);      
    }
    

// ����� �������� ����������� ����� � ��������� ������
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        /** The system calls this to perform work in a worker thread and
          * delivers it the parameters given to AsyncTask.execute() 
          * ��� ������������ ���� �����
          * public void onClick(View v) {
	      * 	new DownloadImageTask().execute("http://example.com/image.png");
	      * }*/
        protected Bitmap doInBackground(String... urls) {
            try {
            	// �������� ����������� ����� � ��������� ������
            	strCaptcha0 = GetCaptchaPath(urls[0]);
				return getImageByUrl("http://irk.ru/captcha/image/" + strCaptcha0);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
        }
        
        private Bitmap getImageByUrl(String url) throws IOException,
			MalformedURLException {
			//��� ��� ����� �������� ����������� �� url
			Bitmap image = BitmapFactory.decodeStream((InputStream)new URL(url).getContent());
			return image;
		}

        
    	// ������� ��������� ������ �� �������� �����
    	public String GetCaptchaPath(String urlsite) 
    	{
    		String matchtemper = "";
    		try
    		{
    			// �������� ��������
    			URL url = new URL(urlsite);
    			URLConnection conn = url.openConnection();

    			// Save Cookie
    			String headerName = null;
    			//_cookies.clear();
    			if (_cookie == "") {
    				for (int i=1; (headerName = conn.getHeaderFieldKey(i))!=null; i++) {
    					if (headerName.equalsIgnoreCase("Set-Cookie")) 
    					{    
    						String cookie = conn.getHeaderField(i);
    						_cookie += cookie.substring(0,cookie.indexOf(";")) + "; ";
    					}
    				}
    			}
                    
    			InputStreamReader rd = new InputStreamReader(conn.getInputStream());
    			StringBuilder allpage = new StringBuilder();
    			int n = 0;
    			char[] buffer = new char[40000];
    			while (n >= 0)
    			{
    				n = rd.read(buffer, 0, buffer.length);
    				if (n > 0)
    				{
    					allpage.append(buffer, 0, n);                    
    				}
    			}
    			// �������� � �����������
    			final Pattern pattern = Pattern.compile ("/captcha/image/([a-z0-9]+)/");
    			Matcher matcher = pattern.matcher(allpage.toString());
    			if (matcher.find())
    			{    
    				matchtemper = matcher.group(1);            
    			}        
    			return matchtemper;
    		}
    		catch (Exception e)
    		{
                    
    		}
    		return matchtemper; 
    	};  
    	/*
		@Override
        protected void onProgressUpdate(Void... values) {
             super.onProgressUpdate(values);
             // �������� ������
             showDialog(PROGRESS_DLG_ID);
        }
		*/
        protected void onPostExecute(Bitmap result) {
        	pd.dismiss();
            imgCaptcha.setImageBitmap(result);
         // ���������� ���� �������
        	//dismissDialog(PROGRESS_DLG_ID);
        }
    }
    

	
	// �������� ����� �� ����
	public String GetToken(String data)
	{
		String matchtoken = "";
		// �������� � �����������
		//Log.v("coocies",data);
		final Pattern pattern = Pattern.compile ("csrftoken=([a-zA-Z0-9]+);");
		Matcher matcher = pattern.matcher(data);
		if (matcher.find())
		{    
			matchtoken = matcher.group(1);            
		} 
		//Log.v("coocies_t",matchtoken);
		return matchtoken;
	}
 
// ����� �������� ��� � ��������� ������
    private class SendSMSTask extends AsyncTask<String, Void, String> {
        /** The system calls this to perform work in a worker thread and
          * delivers it the parameters given to AsyncTask.execute() 
          * ��� ������������ ���� �����
          * public void onClick(View v) {
	      * 	new SendSMSTask().execute("http://example.com/image.png");
	      * }*/

        protected String doInBackground(String... urls) {
            try {
            	// �������� ����������� ����� � ��������� ������
				return SendPost(urls[0],urls[1]);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
        }
        
    	// ���������� SMS       
		public String SendPost(String httpURL, String data) throws IOException   
		{
			//Log.v("Url", httpURL);
			//Log.v("Data", data);
			
			URL url = new URL(httpURL);

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:10.0.2) Gecko/20100101 Firefox/10.0.2");
			connection.setRequestProperty("Host", "www.irk.ru");
			connection.setRequestProperty("Referer","http://www.irk.ru/sms/");
			connection.setRequestMethod("POST");
	                
			// If cookie exists, then send cookie
			if (_cookie != "") 
			{
				//Log.v("Cookie", _cookie);
				connection.setRequestProperty("Cookie", _cookie);
				connection.connect();
			}
	                
			// If Post Data not empty, then send POST Data
			if (data != "") 
			{
				//Log.v("POST", "Data not null");
				OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
				out.write(data);
				out.flush();
				out.close();
			}
	                
			// Save Cookie
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String headerName = null;
			//_cookies.clear();
			if (_cookie == "") 
			{
				for (int i=1; (headerName = connection.getHeaderFieldKey(i))!=null; i++) 
				{
					if (headerName.equalsIgnoreCase("Set-Cookie")) 
					{    
						String cookie = connection.getHeaderField(i);
						_cookie += cookie.substring(0,cookie.indexOf(";")) + "; ";
					}
				}
			}
	                
			// Get HTML from Server
			String getData = "";
			String decodedString;
			while ((decodedString = in.readLine()) != null) 
			{
				//Log.v("DATA", "read data");
				getData += decodedString + "\n";
			}
			in.close();
			
			return getData;
		}

		@Override
        protected void onProgressUpdate(Void... values) {
             super.onProgressUpdate(values);

        }

		// ��������� ���������� ������ ������ ������ � �������������� � ���������� ��������� ������
        protected void onPostExecute(String result) {
        	// ���������� ���� �������
        	pdSMS.dismiss();
        	
    		String matchtoken = "";
    		// �������� � �����������
    		final Pattern pattern = Pattern.compile ("(succes[a-z]+)");
    		Matcher matcher = pattern.matcher(result);
    		if (matcher.find())
    		{    
    			matchtoken = matcher.group(1); 
    			imgStatus.setVisibility(View.VISIBLE);
    			//Log.v("matcher", matchtoken);
    		} 
    		
    		txtError.setText(matchtoken);
        }
    }  

    protected void onResume() {
        strMyName = sp.getString("Name", "");
        super.onResume();
      }
    
	// �������� ����	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        //menu.add("menu1");
        //return true;
	      MenuItem mi = menu.add(0, 1, 0, "���������");
	      mi.setIntent(new Intent(this, PrefActivity.class));
	      return super.onCreateOptionsMenu(menu);
    }
}
