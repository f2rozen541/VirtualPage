package com.hanvon.virtualpage.beans;

public class VirtualPageException extends Exception {
	private static final long serialVersionUID = 6978861145572189065L;
	private String mWrongMessage;

	public VirtualPageException(String wrongMessage){
		mWrongMessage = wrongMessage;
	}
	
	@Override
	public String toString(){
		return mWrongMessage;
	}
}
