package com.kosenventure.sansan.others;

import java.util.ArrayList;

import com.kosenventure.sansan.topick.R;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;


public class KeyPhraseListView extends ScrollView {

	final static int MP = LinearLayout.LayoutParams.MATCH_PARENT;
	final static int WC = LinearLayout.LayoutParams.WRAP_CONTENT;
	final static String DUMMY_DATE = "1111-11-11 11:11:11";
	
	private boolean mSortFlag = false;
	
	private Activity mContext;
	private ArrayList<KeyPhrase> mKeyPhrases = new ArrayList<KeyPhrase>();
	private ArrayList<KeyPhrase> mCancelKeyPhrases = new ArrayList<KeyPhrase>();
	private AccessDb mAd;
	
	private RelativeLayout mKeyPhraseListView;
	
	public KeyPhraseListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = (Activity) context;
		mAd = new AccessDb(mContext);

//		deleteData();
//		saveData();
		getKeyPhrasesFromDb();
		
		mKeyPhraseListView = new RelativeLayout(mContext);
		addView(mKeyPhraseListView, new ViewGroup.LayoutParams(MP, MP));
		
		setKeyPhrases();
	}
	
	private void setKeyPhrases(){
		int id = 1;
		if( mKeyPhrases != null ) for( KeyPhrase phrase : mKeyPhrases ) addKeyPhraseView(phrase, id++);
		if( mCancelKeyPhrases != null ) for( KeyPhrase phrase : mCancelKeyPhrases ) addKeyPhraseView(phrase, id++);
	}
	
	public void searchKeyPhrase(String key){
		int id = 1;
		removeAllKeyPhrases();
		if( mKeyPhrases != null ){
			for( KeyPhrase phrase : mKeyPhrases ){
				if( phrase.phrase.indexOf(key) != -1 )	addKeyPhraseView(phrase, id++);
			}
		}
		mSortFlag = false;
	}

	public void addKeyPhrase(String phrase) {
		// DBにキーフレーズを追加
		mAd.writeDb(getStr(R.string.keyphrase_table), phrase, DUMMY_DATE);
		// リストに追加
		mKeyPhrases.add( new KeyPhrase(mKeyPhrases.get(mKeyPhrases.size()-1).id+1, phrase, DUMMY_DATE));
		// Viewに追加
		addKeyPhraseView(mKeyPhrases.get(mKeyPhrases.size()-1), mKeyPhraseListView.getChildAt(mKeyPhraseListView.getChildCount()-1).getId()+1);
		// Viewをソート
		mSortFlag = false;
	}
	
	private void removeAllKeyPhrases(){
		mKeyPhraseListView.removeAllViews();
	}
	

	private void addKeyPhraseView(KeyPhrase phrase, int id){
		LayoutInflater inflater = mContext.getLayoutInflater();
		LinearLayout keyphraseView;
		TextView keyphrase;
		ImageButton keyphraseBtn;
		
		// ラインにフレーズを追加する
		keyphraseView = (LinearLayout) inflater.inflate(R.layout.keyphrase_layout, null);
		keyphraseView.setId(id);
		keyphrase = (TextView) keyphraseView.findViewById(R.id.keyphrase);
		keyphrase.setText(phrase.phrase);
		keyphraseBtn = (ImageButton) keyphraseView.findViewById(R.id.keyphrase_btn);
		keyphraseBtn.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
//				keyphraseLineView.removeView(keyphraseView);
			}
		});
		mKeyPhraseListView.addView(keyphraseView);
		
	}
	
	private void sortKeyPhraseViews(){
		mSortFlag = true;
		
		int count = mKeyPhraseListView.getChildCount();
		if( count == 0 ) return;
		
	    int margin = 10;
	    View pline = mKeyPhraseListView.getChildAt(0);
	    pline.setLayoutParams( params(WC, WC, null, 0));
	    int total = pline.getWidth() + margin;
	    View view;
	    RelativeLayout.LayoutParams prm;
	    for (int i = 1; i < count; i++) {
	    	view = mKeyPhraseListView.getChildAt(i);
	    	int w = view.getWidth() + margin;
	    	
	    	// 横幅を超えないなら前のボタンの右に出す
	    	if ( mKeyPhraseListView.getWidth() > total + w) {
	    		total += w;
	    		prm = params(WC, WC, new int[]{ RelativeLayout.ALIGN_TOP, RelativeLayout.RIGHT_OF}, i);
	    	}
	    	// 超えたら下に出す
	    	else {	
	    		prm = params(WC, WC, new int[]{ RelativeLayout.BELOW}, pline.getId());
	    		// 基点を変更
	    		pline = mKeyPhraseListView.getChildAt(i);
	   	     	// 長さをリセット
	    		total = pline.getWidth() + margin;
	      	}
	    	view.setLayoutParams(prm);
	    }
	}
	
	// デバッグ用。DBにダミーデータを追加
	private void saveData(){
		mAd.writeDb(getStr(R.string.keyphrase_table), "キーフレーズキーフレーズキーフレーズキーフレーズ", "2009-08-24 23:10:15");
		mAd.writeDb(getStr(R.string.keyphrase_table), "テストテストテスト", "2009-08-24 23:10:15");	
		mAd.writeDb(getStr(R.string.keyphrase_table), "キーフレーズキーフレーズキーフレーズキーフレーズ", "2009-08-24 23:10:15");
		mAd.writeDb(getStr(R.string.keyphrase_table), "テストテストテスト", "2009-08-24 23:10:15");	
		mAd.writeDb(getStr(R.string.keyphrase_table), "キーフレーズキーフレーズキーフレーズキーフレーズ", "2009-08-24 23:10:15");
		mAd.writeDb(getStr(R.string.keyphrase_table), "テストテストテスト", "2009-08-24 23:10:15");	
		mAd.writeDb(getStr(R.string.keyphrase_table), "キーフレーズキーフレーズキーフレーズキーフレーズ", "2009-08-24 23:10:15");
		mAd.writeDb(getStr(R.string.keyphrase_table), "テストテストテスト", "2009-08-24 23:10:15");	
		mAd.writeDb(getStr(R.string.keyphrase_table), "キーフレーズキーフレーズキーフレーズキーフレーズ", "2009-08-24 23:10:15");
		mAd.writeDb(getStr(R.string.keyphrase_table), "テストテストテスト", "2009-08-24 23:10:15");	
		mAd.writeDb(getStr(R.string.keyphrase_table), "キーフレーズキーフレーズキーフレーズキーフレーズ", "2009-08-24 23:10:15");
		mAd.writeDb(getStr(R.string.keyphrase_table), "テストテストテスト", "2009-08-24 23:10:15");	
		mAd.writeDb(getStr(R.string.keyphrase_table), "キーフレーズキーフレーズキーフレーズキーフレーズ", "2009-08-24 23:10:15");
		mAd.writeDb(getStr(R.string.keyphrase_table), "テストテストテスト", "2009-08-24 23:10:15");	
		mAd.writeDb(getStr(R.string.keyphrase_table), "キーフレーズキーフレーズキーフレーズキーフレーズ", "2009-08-24 23:10:15");
		mAd.writeDb(getStr(R.string.keyphrase_table), "テストテストテスト", "2009-08-24 23:10:15");	
		mAd.writeDb(getStr(R.string.keyphrase_table), "キーフレーズキーフレーズキーフレーズキーフレーズ", "2009-08-24 23:10:15");
		mAd.writeDb(getStr(R.string.keyphrase_table), "テストテストテスト", "2009-08-24 23:10:15");	
		mAd.writeDb(getStr(R.string.keyphrase_table), "キーフレーズキーフレーズキーフレーズキーフレーズ", "2009-08-24 23:10:15");
		mAd.writeDb(getStr(R.string.keyphrase_table), "テストテストテスト", "2009-08-24 23:10:15");	
		mAd.writeDb(getStr(R.string.keyphrase_table), "キーフレーズキーフレーズキーフレーズキーフレーズ", "2009-08-24 23:10:15");
		mAd.writeDb(getStr(R.string.keyphrase_table), "テストテストテスト", "2009-08-24 23:10:15");	
		mAd.writeDb(getStr(R.string.keyphrase_table), "キーフレーズキーフレーズキーフレーズキーフレーズ", "2009-08-24 23:10:15");
		mAd.writeDb(getStr(R.string.keyphrase_table), "テストテストテスト", "2009-08-24 23:10:15");	
		mAd.writeDb(getStr(R.string.keyphrase_table), "キーフレーズキーフレーズキーフレーズキーフレーズ", "2009-08-24 23:10:15");
		mAd.writeDb(getStr(R.string.keyphrase_table), "テストテストテスト", "2009-08-24 23:10:15");	
	}
	// デバッグ用。DBのデータをすべて削除
	private void deleteData(){
		Cursor cursor = mAd.readDb(getStr(R.string.keyphrase_table), null, null, null, "id");
		int id;
		if(cursor != null){
			do {
				id = cursor.getInt(cursor.getColumnIndex("id"));
				mAd.deleteDb(getStr(R.string.keyphrase_table), String.valueOf(id));
			} while (cursor.moveToNext());
			cursor.close();
		}
		
		cursor = mAd.readDb(getStr(R.string.cancel_keyphrase_table), null, null, null, "id");
		if(cursor != null){
			do {
				id = cursor.getInt(cursor.getColumnIndex("id"));
				mAd.deleteDb(getStr(R.string.cancel_keyphrase_table), String.valueOf(id));
			} while (cursor.moveToNext());
			cursor.close();
		}
	}
	// DBをcloseする
	public void closeDb(){
		mAd.closeDb();
	}
	// DBからキーフレーズを取得する
	private void getKeyPhrasesFromDb(){
		int id;
		String phrase,date;
		Cursor cursor = mAd.readDb(getStr(R.string.keyphrase_table), null, null, null, "id");
		if(cursor != null){
			do {
				id = cursor.getInt(cursor.getColumnIndex("id"));
				phrase = cursor.getString(cursor.getColumnIndex("phrase"));
				date = cursor.getString(cursor.getColumnIndex("date"));
				
				mKeyPhrases.add(new KeyPhrase(id, phrase, date));
			} while (cursor.moveToNext());
			cursor.close();
		}
		
//			i = 0;
//			cursor = mAd.readDb(getStr(R.string.cancel_keyphrase_table), null, null, null, "id");
//			if(cursor != null){
//				mCancelKeyPhrases = new KeyPhrase[cursor.getCount()];
//				do {
//					id = cursor.getInt(cursor.getColumnIndex("id"));
//					phrase = cursor.getString(cursor.getColumnIndex("phrase"));
//					date = cursor.getString(cursor.getColumnIndex("date"));
//					
//					mCancelKeyPhrases[i++] = new KeyPhrase(id, phrase, date);
//				} while (cursor.moveToNext());
//				cursor.close();
//			}
	}

	private String getStr(int resourceId) {
		return mContext.getResources().getString(resourceId);
	}
	
	private RelativeLayout.LayoutParams params(int width, int height, int[] verbs, int anchor){
		RelativeLayout.LayoutParams params  = new RelativeLayout.LayoutParams(width, height);
		params.setMargins(5, 0, 5, 5);
		if( anchor > 0 ) for( int verb : verbs ) params.addRule(verb, anchor);
		return params;
	}
	
	@Override
	protected void onDraw (Canvas canvas) {
		super.onDraw(canvas);
		
		if( mSortFlag ) return;
		sortKeyPhraseViews();
	}
}
