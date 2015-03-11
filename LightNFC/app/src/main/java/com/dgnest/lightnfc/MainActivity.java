package com.dgnest.lightnfc;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.dgnest.lightnfc.util.BluetoothDroid;

import java.math.BigInteger;


public class MainActivity extends ActionBarActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    private String uid;

    private TextView tvTitle;
    private Button btnGetPairedDevices;
    private ListView lvPairedDevices;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        );

        tvTitle = (TextView) findViewById(R.id.tvTitle);
        btnGetPairedDevices = (Button) findViewById(R.id.btnGetPairedDevices);
        lvPairedDevices = (ListView) findViewById(R.id.lvPairedDevices);

        BluetoothDroid.getInstance(MainActivity.this);

        // Obtenemos dispositivos apareados y los mostramos en un listview
        btnGetPairedDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        MainActivity.this,
                        android.R.layout.simple_list_item_1,
                        BluetoothDroid.getInstance(MainActivity.this).getNameBtDevices()
                );

                lvPairedDevices.setAdapter(adapter);
            }
        });

        // Nos conectamos con el dispositivo que elijamos del listView
        lvPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
                pd = new ProgressDialog(MainActivity.this);
                pd.setMessage(getString(R.string.connecting));
                pd.setCancelable(false);
                pd.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BluetoothDroid.getInstance(MainActivity.this).connectDevice(position);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lvPairedDevices.setAdapter(null);
                                pd.dismiss();
                            }
                        });
                    }
                }).start();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        NfcProcessThread nfcThread = new NfcProcessThread(intent);
        Log.d("NfcActivity: ", "new intent");
        nfcThread.start();
    }

    public class NfcProcessThread extends Thread {
        Intent intent;

        public NfcProcessThread(Intent intent) {
            this.intent = intent;
        }

        @Override
        public void run() {
            Looper.prepare();
            final String action = intent.getAction();
            Log.d("NfcWrite", "Action: " + action);

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            uid = bin2hex(tag.getId());
            Log.d("NfcActivity: ", "leido = " + uid);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (uid.equals("04A5484ABD3980")) {
                        tvTitle.setText("prender led");
                        // Enviamos el string "0", vendria a ser el 48 en decimal
                        BluetoothDroid.getInstance(getApplicationContext()).enviarPaqBT("0");
                    } else if (uid.equals("0459B24ABD3980")) {
                        tvTitle.setText("apagar led");
                        // Enviamos el string "1", vendria a ser el 49 en decimal
                        BluetoothDroid.getInstance(getApplicationContext()).enviarPaqBT("1");
                    } else {
                        tvTitle.setText("tag desconocido");
                    }


                }
            });
        }
    }

    //To display the UID
    static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }

}
