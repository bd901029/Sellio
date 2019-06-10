package com.sellio.pos.engine.ble;

import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.sellio.pos.R;
import com.sellio.pos.engine.db.Cart;
import com.sellio.pos.engine.db.Sale;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class BluetoothPrinterManager {
	private static BluetoothPrinterManager instance = null;
	public static BluetoothPrinterManager sharedInstance() {
		if (instance == null)
			instance = new BluetoothPrinterManager();
		return instance;
	}

	private static BluetoothSocket bleSocket;
	OutputStream outputStream = null;

	BluetoothPrinterManager() {

	}

	public void initialize(BluetoothSocket bleSocket) {
		this.bleSocket = bleSocket;
	}

	public void printSale(Sale sale) {
		//print command
		try {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			outputStream = bleSocket.getOutputStream();

			byte[] printFormat = new byte[]{0x1B, 0x21, 0x03};
			outputStream.write(printFormat);

			printNewLine();

			for (Cart cart : sale.getCarts()) {
				printText(leftRightAlign(cart.getProduct().getName(), String.valueOf(cart.getProduct().getSalePrice())));
			}

			printNewLine();

			printText(leftRightAlign("Total:", String.valueOf(sale.getTotalPrice())));

			printNewLine();
			printNewLine();

			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printCustom(String msg, int size, int align) {
		//Print config "mode"
		byte[] cc = new byte[]{0x1B,0x21,0x03};  // 0- normal size text
		//byte[] cc1 = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
		byte[] bb = new byte[]{0x1B,0x21,0x08};  // 1- only bold text
		byte[] bb2 = new byte[]{0x1B,0x21,0x20}; // 2- bold with medium text
		byte[] bb3 = new byte[]{0x1B,0x21,0x10}; // 3- bold with large text
		try {
			switch (size) {
				case 0:
					outputStream.write(cc);
					break;
				case 1:
					outputStream.write(bb);
					break;
				case 2:
					outputStream.write(bb2);
					break;
				case 3:
					outputStream.write(bb3);
					break;
			}

			switch (align){
				case 0:
					//left align
					outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
					break;
				case 1:
					//center align
					outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
					break;
				case 2:
					//right align
					outputStream.write(PrinterCommands.ESC_ALIGN_RIGHT);
					break;
			}
			outputStream.write(msg.getBytes());
			outputStream.write(PrinterCommands.LF);
			//outputStream.write(cc);
			//printNewLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//print new line
	private void printNewLine() {
		try {
			outputStream.write(PrinterCommands.FEED_LINE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//print text
	private void printText(String msg) {
		try {
			// Print normal text
			outputStream.write(msg.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	//print byte[]
	private void printText(byte[] msg) {
		try {
			// Print normal text
			outputStream.write(msg);
			printNewLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String leftRightAlign(String str1, String str2) {
		String ans = str1 +str2;
		if(ans.length() <31){
			int n = (31 - str1.length() + str2.length());
			ans = str1 + new String(new char[n]).replace("\0", " ") + str2;
		}
		return ans;
	}
}
