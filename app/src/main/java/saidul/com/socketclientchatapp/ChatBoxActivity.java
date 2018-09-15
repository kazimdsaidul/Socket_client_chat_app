package saidul.com.socketclientchatapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatBoxActivity extends AppCompatActivity {


    public RecyclerView myRecylerView;
    public List<Message> MessageList;
    public ChatBoxAdapter chatBoxAdapter;
    public EditText messagetxt;
    public Button send;

    //declare socket object
    private Socket socket;
    private String Nickname;
    private String TAG = ChatBoxActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_box);


        initilization();
// get the nickame of the user


        Nickname = (String) getIntent().getExtras().getString(MainActivity.NICKNAME);

//connect you socket client to the server

        try {


//if you are using a phone device you should connect to same local network as your laptop and disable your pubic firewall as well

            socket = IO.socket("http://192.168.0.102:3000");

            //create connection

            socket.connect();

// emit the event join along side with the nickname

            socket.emit("join", Nickname);


            socket.on("userjoinedthechat", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    final String data = (String) args[0];
                    // get the extra data from the fired event and display a toast
                    // Toast.makeText(getApplicationContext(), data, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "call: " + data);
                    runOnUiThread(new Runnable() {
                        public void run() {

                            Toast.makeText(getApplicationContext(), "" + data, Toast.LENGTH_SHORT).show();
                        }
                    });


                }
            });


            socket.on("message", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject data = (JSONObject) args[0];
                            try {
                                //extract data from fired event

                                String nickname = data.getString("senderNickname");
                                String message = data.getString("message");

                                // make instance of message

                                Message m = new Message(nickname, message);


                                //add the message to the messageList

                                MessageList.add(m);

                                // add the new updated list to the adapter
                                chatBoxAdapter = new ChatBoxAdapter(MessageList);

                                // notify the adapter to update the recycler view

                                chatBoxAdapter.notifyDataSetChanged();

                                //set the adapter for the recycler view

                                myRecylerView.setAdapter(chatBoxAdapter);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
                    });
                }
            });


            socket.on("userdisconnect", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String data = (String) args[0];

                            Toast.makeText(ChatBoxActivity.this, data, Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            });


        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    private void initilization() {
        messagetxt = (EditText) findViewById(R.id.message);
        send = (Button) findViewById(R.id.send);
        MessageList = new ArrayList<>();
        myRecylerView = (RecyclerView) findViewById(R.id.messagelist);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        myRecylerView.setLayoutManager(mLayoutManager);
        myRecylerView.setItemAnimator(new DefaultItemAnimator());

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //retrieve the nickname and the message content and fire the event messagedetection


                if (!messagetxt.getText().toString().isEmpty()) {

                    socket.emit("messagedetection", Nickname, messagetxt.getText().toString());

                    messagetxt.setText(" ");
                }


            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }
}