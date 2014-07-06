package com.asmack;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;

/**
 * @author zhanghaitao
 * @date 2011-7-11
 * @version 1.0
 */
public class XmppMessageManager implements ChatManagerListener {
	private XMPPConnection _connection;
	private ChatManager manager = null;

	public void initialize(XMPPConnection connection) {
		_connection = connection;
		manager = _connection.getChatManager();
		manager.addChatListener(this);
	}

	@Override
	public void chatCreated(Chat chat, boolean arg1) {
		// TODO Auto-generated method stub
		chat.addMessageListener(new MessageListener() {
			public void processMessage(Chat newchat, Message message) {
				// �������촰���Ѵ��ڣ�����Ϣת��Ŀǰ����
				// ���Ǵ��ڲ����ڣ����µĴ��ڲ�ע��
				System.out.println(message.getFrom() + ":" + message.getBody());
				
				if (!ActivityMain.chats.containsKey(message.getFrom())) {
					ActivityMain.chats.put(message.getFrom(), newchat);
				} else {

				}

			}
		});
	}

}
