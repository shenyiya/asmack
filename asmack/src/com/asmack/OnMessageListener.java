package com.asmack;

import org.jivesoftware.smack.packet.Message;

/**
 * @author zhanghaitao
 * @date 2011-7-5
 * @version 1.0
 */
/*
 * version����OnContactStateListener��һ���¼��ӿڣ��������£�
 */
public interface OnMessageListener {
	/*
	 * @return ������յ�����Ϣ
	 */
	public void processMessage(Message message);
}
