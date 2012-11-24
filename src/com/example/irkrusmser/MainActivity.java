package com.example.irkrusmser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
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
	TextView txtName;
	TextView txtCaptcha1;
	TextView txtError;
	Button buttonSend;
	Button buttonSelectContact;
	//Button buttonReload;
	ImageView imgCaptcha;
	
    private String _cookie = "";
    private String strCaptcha0 = "";
    
	// ќпредел€ем подключены ли к интернету
	public boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo nInfo = cm.getActiveNetworkInfo();
	    if (nInfo != null && nInfo.isConnected()) {
	        Log.v("status", "ONLINE");
	        return true;
	    }
	    else {
	        Log.v("status", "OFFLINE");
	        return false;
	    }
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //isOnline();
        // найдем View-элементы
        txtPhoneNumber = (TextView) findViewById(R.id.editPhoneNumber);
        txtSMSText = (TextView) findViewById(R.id.editMessage);
        txtName = (TextView) findViewById(R.id.editName);
        txtCaptcha1 = (TextView) findViewById(R.id.editCaptcha1);
        txtError = (TextView) findViewById(R.id.textError);
        //buttonSend = (Button) findViewById(R.id.buttonSend);
        //buttonSelectContact = (Button) findViewById(R.id.buttonContact);
        //buttonSelectContact = (ImageButton) findViewById(R.id.imgbuttonContact);
        //buttonReload = (Button) findViewById(R.id.buttonReload);
        buttonSend = (Button) findViewById(R.id.btnSend);
        buttonSelectContact = (Button) findViewById(R.id.btnContacts);
        imgCaptcha = (ImageView) findViewById(R.id.imageCaptcha1);
        
        /*  Загружаем настройки. Если настроек с таким именем нету - 
        возвращаем второй аргумент. В данном случае пробел.  */		
 
        SharedPreferences settings = getPreferences(0);
        String name = settings.getString("Name"," ");
 
        /* Вставляем в текстовые поля загруженные параметры */        
        txtName.setText(name);
        
        // ѕроверка на подключение к »нтернет
        if (isOnline() == true){
        	// ѕолучаем ссылку на капчу
        	goUrl("http://irk.ru/sms");
        }
        else{
        	txtError.setText("¬ы не подключены к сети »нтернет.");
        }  
        // ќбработчик нажати€ на кнопку ртправить
        buttonSend.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		//Toast.makeText(getApplicationContext(), "ѕривет, мир!", Toast.LENGTH_SHORT).show();
        		// ѕо нажатию на кнопку button1 происходит отображение всплываещего сообщени€ "ѕривет, мир!"
        		if (isOnline() == true){
	        		String data_s = "csrfmiddlewaretoken=" + GetToken(_cookie) + "&number=" + txtPhoneNumber.getText() + "&message=" + txtSMSText.getText() + "\n" + txtName.getText() + "&captcha_0=" + strCaptcha0 + "&captcha_1=" + txtCaptcha1.getText();
	        		try 
	        		{
	        			txtError.setText(SendPost("http://irk.ru/sms/?", data_s));
	        		} 
	        		catch (IOException e) 
	        		{
	        			e.printStackTrace();
	        		}
	        		// Получаем ссылку на капчу
	                goUrl("http://irk.ru/sms");
        		}
        		else{
        			Toast.makeText(getApplicationContext(), "¬ы не подключены к сети »нтернет.", Toast.LENGTH_SHORT).show();
        		}
        	}
        });
        
        // ќбработчик нажати€ на капчу (обновление капчи)
        imgCaptcha.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		//Toast.makeText(getApplicationContext(), "ѕривет, мир!", Toast.LENGTH_SHORT).show();
        		// ѕо нажатию на кнопку button1 происходит отображение всплываещего сообщени€ "ѕривет, мир!"
        		if (isOnline() == true){
	        		// Получаем ссылку на капчу
	                goUrl("http://irk.ru/sms");
        		}
        		else{
        			Toast.makeText(getApplicationContext(), "¬ы не подключены к сети »нтернет.", Toast.LENGTH_SHORT).show();
        		}
        	}
        });
        
        // ќбработчик выбора контакта
        buttonSelectContact.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		// ¬ыбор только контактов звонков (без почтовых)
                Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pickIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                startActivityForResult(pickIntent, PICK_RESULT);
        	}
        });
    }
  
    // «акрытие приложения
	@Override
    protected void onStop(){
       super.onStop();
 
       String name = txtName.getText().toString().trim();
 
      /* «агружаем редактор настроек и вписываем новые значения */
 
      SharedPreferences settings = getPreferences(0);
      SharedPreferences.Editor editor = settings.edit();
      editor.putString("Name", name);
 
      /* —охраняем данные. Если не выполнить - ничего не сохранится =) */
      editor.commit();
    }

    // ќбработка выбора контакта
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

    // присваивание значения номера телефона 
    public void showSelectedNumber(int type, String number) {
    	txtPhoneNumber.setText(number);      
    }
    
 	// ѕереходим по ссылке
    private void goUrl(String strurl)
    {
        // Получаем ссылку на капчу
        strCaptcha0 = GetCaptchaPath(strurl);
        String url = "http://irk.ru/captcha/image/" + strCaptcha0;
        txtError.setText(url);
        // загружаем картинку с указанного адреса в ImageView
        try 
        {
        	imgCaptcha.setImageDrawable(grabImageFromUrl(url));
        } 
        catch (Exception e) 
        {
        	txtError.setText("Ошибка: Exception");
        }
    }
    
	// ‘ункция получения ссылки на картинку капчи
	public String GetCaptchaPath(String urlsite) 
	{
		String matchtemper = "";
		try
		{
			// загрузка страницы
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
			// работаем с регулярками
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
	
	// Функция загрузки изображения из Интернета
	private Drawable grabImageFromUrl(String url_) throws Exception 
	{
		return Drawable.createFromStream((InputStream) new URL(url_).getContent(), "src");
	}
	
	// ѕолучаем токен из куки
	public String GetToken(String data)
	{
		String matchtoken = "";
		// работаем с регулярками
		final Pattern pattern = Pattern.compile ("csrftoken=([a-zA-Z0-9]+);");
		Matcher matcher = pattern.matcher(data);
		if (matcher.find())
		{    
			matchtoken = matcher.group(1);            
		} 
		return matchtoken;
	}
    
	// ќтправл€ем SMS       
		public String SendPost(String httpURL, String data) throws IOException   
		{
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
				connection.setRequestProperty("Cookie", _cookie);
				connection.connect();
			}
	                
			// If Post Data not empty, then send POST Data
			if (data != "") 
			{
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
				getData += decodedString + "\n";
			}
			in.close();
	                
			return getData;
		}  
		
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
