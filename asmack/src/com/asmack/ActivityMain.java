package com.asmack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.asmack.model.User;

/**
 * @author zhanghaitao
 * @date 2011-7-10
 * @version 1.0
 * 
 *          �����棬��Ҫ�ǵ�½��Ľ��棬��ʾ�����б�
 */
public class ActivityMain extends Activity {
	public static String TAG = "shenyiya";
	public static XMPPConnection connection;
	private static String host = "192.168.0.102";
	private MyServiceConnection fileServiceConnection;
	private MyServiceConnection messageServiceConnection;
	// ���촰�� �û���,chat����
	public static HashMap<String, Chat> chats = new HashMap<String, Chat>();
	private ListView listview;
	private RosterAdapter rosterAdapter;
	private List<User> userinfos = new ArrayList<User>();
	private Roster roster;
	private String fileaction = "com.asmack.fileservice";
	private String messageaction = "com.asmack.messageservice";
	public final int MENU_MULCHAT = 1; // ��������
	public final int MENU_REGISTER = 2; // ע��
	public final int MENU_FILETRANSFER = 3; // �ļ�����
	// init
	static {
			Connection.DEBUG_ENABLED = true;
			final ConnectionConfiguration connectionConfig = new ConnectionConfiguration(
					host, 5222, "");
			// Google talk
			// ConnectionConfiguration connectionConfig = new
			// ConnectionConfiguration(
			// "talk.google.com", 5222, "gmail.com");
			// gtalk��֧��SASL��֤������Ϊfalse
			// connectionConfig.setSASLAuthenticationEnabled(false);
			ActivityMain.connection = new XMPPConnection(connectionConfig);

			Connection.DEBUG_ENABLED = true;

			ProviderManager pm = ProviderManager.getInstance();
			configure(pm);
			chats.clear();
		}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		listview = (ListView) findViewById(R.id.listview);
		userinfos.clear();
		rosterAdapter = new RosterAdapter(this);
		listview.setAdapter(rosterAdapter);
		listview.setOnCreateContextMenuListener(this);
		// ��ȡ������
		roster = ActivityMain.connection.getRoster();
		// ��ӻ��������������������״̬�ĸı䡣
		roster.addRosterListener(new RosterListener() {
			@Override
			public void entriesAdded(Collection<String> addresses) {
				System.out.println("entriesAdded");
			}

			@Override
			public void entriesUpdated(Collection<String> addresses) {
				System.out.println("entriesUpdated");
			}

			@Override
			public void entriesDeleted(Collection<String> addresses) {
				System.out.println("entriesDeleted");
			}

			@Override
			public void presenceChanged(Presence presence) {
				System.out.println("presenceChanged - >" + presence.getStatus());
			}

		});

		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				// ��ȡ�û�������������
				User user = userinfos.get(position);
				Intent intent = new Intent(ActivityMain.this,
						ActivityChat.class);
				intent.putExtra("user", user.getUser());
				startActivity(intent);

			}

		});

		updateRoster(); // ���»�����
//		// ���ļ���������
//		Intent fileIntent = new Intent();
//		fileIntent.setAction(fileaction);
//		startService(fileIntent); // ��������
//		fileServiceConnection = new MyServiceConnection();
//		bindService(fileIntent, fileServiceConnection, Context.BIND_AUTO_CREATE);
		Intent messageIntent = new Intent();
		messageIntent.setAction(messageaction);
		startService(messageIntent);
		messageServiceConnection = new MyServiceConnection();
		bindService(messageIntent, messageServiceConnection,
				Context.BIND_AUTO_CREATE);

	}

	public void updateRoster() {
		Log.e(TAG, "���»�����.....");
		Collection<RosterEntry> entries = roster.getEntries();
		for (RosterEntry entry : entries) {
			System.out.print(entry.getName() + " - " + entry.getUser() + " - "
					+ entry.getType() + " - " + entry.getGroups().size());
			Presence presence = roster.getPresence(entry.getUser());
			System.out.println(" - " + presence.getStatus() + " - "
					+ presence.getFrom());
			User user = new User();
			user.setName(entry.getName());
			user.setUser(entry.getUser());
			user.setType(entry.getType());
			user.setSize(entry.getGroups().size());
			user.setStatus(presence.getStatus());
			user.setFrom(presence.getFrom());
			Log.e(TAG, user.toString());
			userinfos.add(user);
		}
		rosterAdapter.notifyDataSetChanged();
	}

	

	private class MyServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			Log.d("info", "Servicea Connection Success");
			// ���ӳɹ�ִ��
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub
			Log.d("info", "Service Connection Filed");
			// ����ʧ��ִ��
		}
	}

	private class RosterAdapter extends BaseAdapter {
		private Context context;

		public RosterAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return userinfos.size();
		}

		@Override
		public Object getItem(int position) {
			return userinfos.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup viewGroup) {
			User user = userinfos.get(position);
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = getLayoutInflater().inflate(
						R.layout.user_list_item, null);
				holder.user = (TextView) convertView
						.findViewById(R.id.user_list_name);
				holder.status = (TextView) convertView
						.findViewById(R.id.user_list_status);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.user.setText("name: " + user.getUser());
			holder.status.setText("status: " + user.getStatus());
			return convertView;
		}

		class ViewHolder {
			TextView user;
			TextView status;
		}

	}

	@Override
	protected void onDestroy() {
		// unbindService(fileServiceConnection);
		// unbindService(messageServiceConnection);
		Intent fileIntent = new Intent();
		fileIntent.setAction(fileaction);
		stopService(fileIntent);
		Intent messageIntent = new Intent();
		messageIntent.setAction(messageaction);
		stopService(messageIntent);
		super.onDestroy();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.add(2, MENU_FILETRANSFER, Menu.NONE, "����");
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// �ؼ�����
		AdapterView.AdapterContextMenuInfo menuInfo;
		menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		// ��ȡposition
		int position = menuInfo.position;
		String user = userinfos.get(position).getUser();
		Intent intent;
		switch (item.getItemId()) {
		case MENU_FILETRANSFER:
			intent = new Intent(this, ActivityFileTransfer.class);
			intent.putExtra("user", user);
			startActivity(intent);
			break;
		default:
			break;
		}
		return false;// false��ʾ�������ݵ����ദ��
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(1, MENU_MULCHAT, Menu.NONE, "��������");
		menu.add(1, MENU_REGISTER, Menu.NONE, "ע��");

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case MENU_MULCHAT:
			intent = new Intent(this, ActivityMultiChat.class);
			startActivity(intent);
			break;
		case MENU_REGISTER:
			intent = new Intent(this, ActivityRegister.class);
			startActivity(intent);
			break;
		case MENU_FILETRANSFER:
			intent = new Intent(this, ActivityFileTransfer.class);
			startActivity(intent);
			break;
		default:
			break;
		}
		return false;// false��ʾ�������ݵ����ദ��
	}

	public static void configure(ProviderManager pm) {

		// Private Data Storage
		pm.addIQProvider("query", "jabber:iq:private",
				new PrivateDataManager.PrivateDataIQProvider());

		// Time
		try {
			pm.addIQProvider("query", "jabber:iq:time",
					Class.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (ClassNotFoundException e) {
			Log.w("TestClient",
					"Can't load class for org.jivesoftware.smackx.packet.Time");
		}

		// Roster Exchange
		pm.addExtensionProvider("x", "jabber:x:roster",
				new RosterExchangeProvider());

		// Message Events
		pm.addExtensionProvider("x", "jabber:x:event",
				new MessageEventProvider());

		// Chat State
		pm.addExtensionProvider("active",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("composing",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("paused",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("inactive",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("gone",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		// XHTML
		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
				new XHTMLExtensionProvider());

		// Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference",
				new GroupChatInvitation.Provider());

		// Service Discovery # Items
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());

		// Service Discovery # Info
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());

		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());

		// MUC User
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
				new MUCUserProvider());

		// MUC Admin
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
				new MUCAdminProvider());

		// MUC Owner
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
				new MUCOwnerProvider());

		// Delayed Delivery
		pm.addExtensionProvider("x", "jabber:x:delay",
				new DelayInformationProvider());

		// Version
		try {
			pm.addIQProvider("query", "jabber:iq:version",
					Class.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
			// Not sure what's happening here.
		}
		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());

		// Offline Message Requests
		pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
				new OfflineMessageRequest.Provider());

		// Offline Message Indicator
		pm.addExtensionProvider("offline",
				"http://jabber.org/protocol/offline",
				new OfflineMessageInfo.Provider());

		// Last Activity
		pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());

		// User Search
		pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

		// SharedGroupsInfo
		pm.addIQProvider("sharedgroup",
				"http://www.jivesoftware.org/protocol/sharedgroup",
				new SharedGroupsInfo.Provider());

		// JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses",
				"http://jabber.org/protocol/address",
				new MultipleAddressesProvider());

		// FileTransfer
		pm.addIQProvider("si", "http://jabber.org/protocol/si",
				new StreamInitiationProvider());

		pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
				new BytestreamsProvider());

		// pm.addIQProvider("open", "http://jabber.org/protocol/ibb",
		// new IBBProviders.Open());
		//
		// pm.addIQProvider("close", "http://jabber.org/protocol/ibb",
		// new IBBProviders.Close());
		//
		// pm.addExtensionProvider("data", "http://jabber.org/protocol/ibb",
		// new IBBProviders.Data());

		// Privacy
		pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());

		pm.addIQProvider("command", "http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider());
		pm.addExtensionProvider("malformed-action",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.MalformedActionError());
		pm.addExtensionProvider("bad-locale",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadLocaleError());
		pm.addExtensionProvider("bad-payload",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadPayloadError());
		pm.addExtensionProvider("bad-sessionid",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadSessionIDError());
		pm.addExtensionProvider("session-expired",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.SessionExpiredError());
	}
}
