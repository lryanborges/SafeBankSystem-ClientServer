package model;

import java.io.Serializable;

public class BankAccount implements Serializable {

	private long id;
	private String cpf;
	private String clientName;
	private String address;
	private String celNumber;
	private String password;
	private double money;
	
	private static long contClients = 0;
	
	public BankAccount() {
		
	}
	
	public BankAccount(String cpf, String name, String password, String address, String number) {
		this.id = ++contClients;
		this.cpf = cpf;
		this.clientName = name;
		this.password = password;
		this.address = address;
		this.celNumber = number;
		this.money = 0;
	}
	
	public BankAccount(String cpf, String password) {
		this.cpf = cpf;
		this.password = password;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCpf() {
		return cpf;
	}
	public void setCpf(String cpf) {
		this.cpf = cpf;
	}
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAddress() {
		return address;
	}
	public void setAddress(String adress) {
		this.address = adress;
	}
	public String getCelNumber() {
		return celNumber;
	}
	public void setCelNumber(String celNumber) {
		this.celNumber = celNumber;
	}
	public double getMoney() {
		return money;
	}
	public void setMoney(double money) {
		this.money = money;
	}
	public static long getContClients() {
		return contClients;
	}
	
}
