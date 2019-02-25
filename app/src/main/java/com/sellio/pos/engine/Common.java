package com.sellio.pos.engine;

public class Common {
	public enum SortType {
		DAILY(0),
		WEEKLY(1),
		MONTHLY(2),
		YEARLY(3);

		private int intValue;
		SortType(int value) {
			intValue = value;
		}

		@Override
		public String toString() {
			switch (intValue) {
				case 0:
					return "Daily";
				case 1:
					return "Weekly";
				case 2:
					return "Monthly";
				case 3:
					return "Yearly";
				default:
					return "Daily";
			}
		}

		public int getInt() {
			return intValue;
		}
	}

	public static String DB_CART_ONSALE = "OnSale";
	public static String DB_CART_SOLD = "Sold";

	public static String DB_INVENTORY_PURCHASE = "Purchase";
	public static String DB_INVENTORY_SALE = "Sale";
}
