package datagrams;

import java.io.Serializable;

public class Message<T> implements Serializable {
	
	private int clientSendingMsg;
	private int operation;
	private T content;
	private String messageHmac;
	
	public Message(int op, T acc) {
		this.operation = op;
		this.content = acc;
	}
	
	public Message(int op, T acc, String hmac) {
		this.operation = op;
		this.content = acc;
		this.messageHmac = hmac;
	}
	
	public Message(int op, T acc, int clientNumber) {
		this.operation = op;
		this.content = acc;
		this.clientSendingMsg = clientNumber;
	}
	
	public Message(int op, T acc, String hmac, int clientNumber) {
		this.operation = op;
		this.content = acc;
		this.messageHmac = hmac;
		this.clientSendingMsg = clientNumber;
	}
	
	public int getOperation() {
		return operation;
	}
	public void setOperation(int operation) {
		this.operation = operation;
	}
	public T getContent() {
		return content;
	}
	public void setContent(T content) {
		this.content = content;
	}
	public String getMessageHmac() {
		return messageHmac;
	}
	public void setMessageHmac(String messageHmac) {
		this.messageHmac = messageHmac;
	}
	public int getClientSendingMsg() {
		return clientSendingMsg;
	}
	public void setClientSendingMsg(int processSendingMsg) {
		this.clientSendingMsg = processSendingMsg;
	}
		
}
