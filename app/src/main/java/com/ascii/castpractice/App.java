package com.ascii.castpractice;

import android.app.Application;

public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Cast = new ChromecastController(this);
	}

	public ChromecastController Cast;

}