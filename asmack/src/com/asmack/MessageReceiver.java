package com.asmack;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.FromContainsFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import android.os.Handler;

/**
 * @author zhanghaitao
 * @date 2011-7-5
 * @version 1.0 �ڱ�дMessageReceiver��ʱ��Ҫע����������
 *          messageReceiver��updateContactStateһ��������ֱ�Ӵ���������Ϣ��
 *          ����ͨ������һ��processMessage�¼��������д���
 *          ���������ϢҪͨ��packetCollector.nextResult������ �����ǰû��������Ϣ��nextResult�����ᱻ������
 *          �������ط���Ҫ������Ϣ��һ������������棬 ��һ��������ϵ���б���� ��chatRoom���н��յ���Ϣ���ֱ����ʾ�������¼��
 */
public class MessageReceiver implements Runnable {
	private final static String TAG = "MessageReceiver";
	private String mAccount;
	private PacketFilter filter;
	private OnMessageListener mOnMessageListener;
	public PacketCollector mCollector;

	public boolean flag = true;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			Message message = (Message) msg.obj;
			if (mOnMessageListener != null) {
				mOnMessageListener.processMessage(message);
			}
			super.handleMessage(msg);
		}

	};

	public MessageReceiver(String account) {
		mAccount = account;
		// ���ڹ���ֻ�����ʻ���Ϣ��������Ϣ

		// ���ǿ���ʹ��һ��AndFilter���������������������
		filter = new AndFilter(new PacketTypeFilter(Message.class),
				new FromContainsFilter(mAccount));
		mCollector = ActivityMain.connection
				.createPacketCollector(filter);

	}

	@Override
	public void run() {
		while (flag) {
			Packet packet = mCollector.nextResult();
			if (packet instanceof Message) {
				System.out.println(((Message) packet).getBody());
				Message msg = (Message) packet;
				android.os.Message message = new android.os.Message();
				message.obj = msg;
				handler.sendMessage(message);
			}
		}
	}

	public void setOnMessageListener(OnMessageListener listener) {
		mOnMessageListener = listener;
	}

}
