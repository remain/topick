package com.kosenventure.sansan.topick;

import com.kosenventure.sansan.others.AccessDb;
import com.kosenventure.sansan.others.PickUpKeyPhrasesTask;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;

public class ManagementKeyPhraseActivity extends MyActivity implements OnClickListener,OnKeyListener{


	final static private int SELECT_PICK_UP_KEY_PHRASE = 200;
	
	private AccessDb mAd;
	private KeyPhraseCursorAdapter mKeyPhraseCursorAdapter;
	
	private ImageView mBackBtn,mShowAddKeyPhraseMenu;
	private EditText mEditSearchKeyPhrase;
	private Button mSearchKeyPhraseBtn,mShowAddKeyPhraseDialogBtn,mShowPickUpKeyPhraseDialogBtn;
	private ListView mKeyPhraseListView;

	AlertDialog selectWayToAddKeyPhraseDialog,addKeyPhraseDialog,pickUpKeyPhraseDialog;

	LayoutInflater inflater;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_management_key_phrase_layout);
		
		mAd = new AccessDb(mContext);
		inflater = getLayoutInflater();
//		saveData();
		
		mBackBtn = (ImageView) findViewById(R.id.btn_back_management_key_phrase);
		mBackBtn.setOnClickListener(this);
		
		mShowAddKeyPhraseMenu = (ImageView) findViewById(R.id.btn_show_add_key_phrase_menu);
		mShowAddKeyPhraseMenu.setOnClickListener(this);
		
		mEditSearchKeyPhrase = (EditText) findViewById(R.id.edit_search_key_phrase);
		mEditSearchKeyPhrase.setOnKeyListener(this);
		
		mSearchKeyPhraseBtn = (Button) findViewById(R.id.btn_search_key_phrase);
		mSearchKeyPhraseBtn.setOnClickListener(this);
		
		mKeyPhraseCursorAdapter = new KeyPhraseCursorAdapter(mContext, getKeyPhrasesFromDb(null, null), false);
		mKeyPhraseListView = (ListView) findViewById(R.id.list_key_phrase);
		mKeyPhraseListView.setAdapter(mKeyPhraseCursorAdapter);
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
    	if( requestCode == SELECT_PICK_UP_KEY_PHRASE ){
    		if( resultCode == RESULT_OK ){
    			mKeyPhraseCursorAdapter.requery();
    		}
    	}
    }
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		closeDb();
	}
	
	// DBからキーフレーズを取得する
	private Cursor getKeyPhrasesFromDb(String where, String[] answer){
		return mAd.readDb(getStr(R.string.keyphrase_table), new String[]{"id as _id","phrase"}, where, answer, "id");
	}
	
	// DBからキーフレーズを削除する
	private void deleteKeyPhraseFromDb(int id) {
		mAd.deleteDb(getStr(R.string.keyphrase_table), String.valueOf(id));
	}
	
	// DBをcloseする
	public void closeDb(){
		mAd.closeDb();
	}

	// キーフレーズの検索
	public void searchKeyPhrase(){
		// 検索フレーズを取得してIMEを閉じる
		String searchKey = mEditSearchKeyPhrase.getText().toString();
		mEditSearchKeyPhrase.clearFocus();
		closeIME(mEditSearchKeyPhrase);
		
		// リストを再構成してアダプターを更新する
		mKeyPhraseCursorAdapter.swapCursor(getKeyPhrasesFromDb("phrase like ?", new String[]{"%"+searchKey+"%"}));
		mKeyPhraseCursorAdapter.notifyDataSetChanged();
	}

	// キーフレーズの追加
	public void addKeyPhrase(String phrase) {
		mAd.writeDb(getStr(R.string.keyphrase_table), phrase);
		mKeyPhraseCursorAdapter.requery();
	}
	
	private void closeIME(View v){
        //ソフトキーボードを閉じる
		InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
	
	private void showAddKeyPhraseMenu(){
		View view = inflater.inflate(R.layout.dialog_select_way_to_add_key_phrase_dialog, null);
		
		mShowAddKeyPhraseDialogBtn = (Button) view.findViewById(R.id.btn_add_key_phrase);
		mShowAddKeyPhraseDialogBtn.setOnClickListener(this);
		mShowPickUpKeyPhraseDialogBtn = (Button) view.findViewById(R.id.btn_pick_up_key_phrase);
		mShowPickUpKeyPhraseDialogBtn.setOnClickListener(this);
		
		selectWayToAddKeyPhraseDialog = new AlertDialog.Builder(this)
											.setTitle("追加方法を選択")
											.setView(view)
											.setCancelable(false)
											.setNegativeButton("閉じる", null)
											.show();
		
//		
	}
	
	private void showAddKeyPhraseDialog() {
		View view = inflater.inflate(R.layout.dialog_add_key_phrase_layout, null);
		final EditText edit = (EditText) view.findViewById(R.id.edit_add_key_phrase_name);
		
		addKeyPhraseDialog = new AlertDialog.Builder(this)
											.setView(view)
											.setCancelable(false)
											.setPositiveButton("追加", new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													String text = edit.getEditableText().toString();
													String ttext = text.replace(" ", "");
													ttext = ttext.replace("　", "");
													
													if( ttext.length() == 0 ){
														toast("空白は追加できません。");
														showAddKeyPhraseDialog();
													}else{
														dialog.dismiss();
														addKeyPhrase(edit.getEditableText().toString());
													}	
												}
											})
											.setNegativeButton("閉じる", null)
											.show();
	}
	
	private void showPickUpKeyPhraseDialog(){
		View view = inflater.inflate(R.layout.dialog_pick_up_key_phrase_layout, null);
		final CheckBox fbCheck = (CheckBox) view.findViewById(R.id.cb_pick_up_from_facebook),
		twCheck = (CheckBox) view.findViewById(R.id.cb_pick_up_from_twitter);
		
		addKeyPhraseDialog = new AlertDialog.Builder(this)
											.setView(view)
											.setCancelable(false)
											.setPositiveButton("抽出", new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													boolean isFb = fbCheck.isChecked();
													boolean isTw = twCheck.isChecked();
													
													if ( !isFb && !isTw ) {
														toast("どちらかを選択してください");
														showPickUpKeyPhraseDialog();
													}else {
														PickUpKeyPhrasesTask mPickUpKeyPhrasesTask = new PickUpKeyPhrasesTask(ManagementKeyPhraseActivity.this);
														mPickUpKeyPhrasesTask.execute(new boolean[]{isFb,isTw});
														dialog.dismiss();
													}
												}
											})
											.setNegativeButton("閉じる", null)
											.show();
	}
	
	@Override
	public void onClick(View v) {
		if ( v == mBackBtn) {
			finish();
		}
		else if ( v == mShowAddKeyPhraseMenu) {
			showAddKeyPhraseMenu();
		}
		else if ( v == mSearchKeyPhraseBtn ) {
			searchKeyPhrase();
		}
		else if ( v == mShowAddKeyPhraseDialogBtn ) {
			selectWayToAddKeyPhraseDialog.dismiss();
			showAddKeyPhraseDialog();
		}
		else if ( v == mShowPickUpKeyPhraseDialogBtn ) {
			selectWayToAddKeyPhraseDialog.dismiss();
			showPickUpKeyPhraseDialog();
		}
	}
	
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){	// 完了ボタンを押した時
			if ( v == mEditSearchKeyPhrase ) {
				searchKeyPhrase();
			}
		}
		return false;
	}
	
	private class KeyPhraseCursorAdapter extends CursorAdapter {

		private KeyPhraseCursorAdapter mMe = this;

		public KeyPhraseCursorAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
			mContext = context;
		}
		
		public void requery(){
			if( mMe.getCursor() == null ){
				mMe.changeCursor(getKeyPhrasesFromDb(null, null));
				mMe.notifyDataSetChanged();
			}else{
				mMe.getCursor().requery();
			}
		}
		
		private void showConfirmDialog(String phrase, final int id){
			new AlertDialog.Builder(ManagementKeyPhraseActivity.this)
						   .setMessage("「"+phrase+"」を削除してよろしいですか？")
						   .setCancelable(false)
						   .setPositiveButton("はい", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// DBから削除
									deleteKeyPhraseFromDb(id);
									requery();
								}
							})
							.setNegativeButton("いいえ", null)
							.show();
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final int id = cursor.getInt(cursor.getColumnIndex("_id"));
			final String phrase = cursor.getString(cursor.getColumnIndex("phrase"));
			
			TextView textPhrase = (TextView) view.findViewById(R.id.text_key_phrase);
			textPhrase.setText(phrase);
			
			ImageButton deleteBtn = (ImageButton) view.findViewById(R.id.btn_delete_key_phrase);
			deleteBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showConfirmDialog(phrase, id);
				}
			});
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v = getLayoutInflater().inflate(R.layout.list_key_phrase_layout, null);
			return v;
		}
	}
}
