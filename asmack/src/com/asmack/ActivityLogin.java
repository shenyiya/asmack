package com.asmack;

import org.jivesoftware.smack.packet.Presence;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author yiya
 * ��¼�Ľ���
 */
public class ActivityLogin extends Activity {

	private Button login, logout;
	private EditText etPassword, etUsername;

	public static String mCurrentAccount;

	public static Util util;

	private final String ACCOUNT_KEY = "login_account";
	private final String PASSWORD_KEY = "login_password";

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login);

		login = (Button) findViewById(R.id.login);
		logout = (Button) findViewById(R.id.logout);
		etPassword = (EditText) findViewById(R.id.password);
		etUsername = (EditText) findViewById(R.id.username);

		ActivityLogin.util = new Util(this);
		etUsername.setText(ActivityLogin.util.getString(ACCOUNT_KEY));
		etPassword.setText(ActivityLogin.util.getString(PASSWORD_KEY));

		logout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ActivityMain.connection.disconnect();
			}
		});

		login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {
					ActivityMain.connection.connect();// connect
					String account = etUsername.getText().toString();
					String password = etPassword.getText().toString();
					// �����û�������
					ActivityLogin.util.saveString(ACCOUNT_KEY, account);
					ActivityLogin.util.saveString(PASSWORD_KEY, password);
					ActivityMain.connection.login(account, password);// login
					// login success ��ʱ�������ж�
					System.out.println("login success");
					ActivityLogin.mCurrentAccount = account;
					System.out.println(ActivityMain.connection.getUser());
					// ��½�ɹ���֪ͨ�������û���������״̬
					Presence presence = new Presence(Presence.Type.available);
					ActivityMain.connection.sendPacket(presence);

					// ��ʼ��ת��main
					Intent intent = new Intent(ActivityLogin.this,
							ActivityMain.class);
					startActivity(intent);
					Toast.makeText(ActivityLogin.this, "��¼�ɹ�", 1).show();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});
	}

	@Override
	protected void onDestroy() {
		ActivityMain.connection.disconnect();
		super.onDestroy();
	}

}