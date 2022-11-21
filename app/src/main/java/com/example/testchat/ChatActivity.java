package com.example.testchat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ChatActivity extends AppCompatActivity {
    EditText msgInput, msgOut;
    Button sendBtn;
    private BufferedReader in; // 네트워크 입력 수신 스트림
    private PrintWriter out;   // 네트워크 출력 송신 스트림
    String receiveData; // 수신 메시지
    String sendingData; // 송신 메시지
    private final Handler handler = new Handler(); // 메인 스레드의 UI 제어를 위함.

//    ConnectivityManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // UI로 화면 표시

//        WifiManager wifiManager= (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
//        manager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//
//        if (wifi.isConnected()) {
//            Toast.makeText(getApplicationContext(), "와이파이가 연결되었습니다.", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(getApplicationContext(), "와이파이를 활성화하십시오.", Toast.LENGTH_SHORT).show();
//        }
//
//        wifiManager.setWifiEnabled(true);

        setView(); // UI 구성. 객체 레퍼런스 설정
        setEvent();
        Intent intent = getIntent();
        final String server = intent.getStringExtra("server");
        //https://stackoverflow.com/questions/18341652/connect-failed-econnrefused
        //To access your PC localhost from Android emulator, use 10.0.2.2 instead of 127.0.0.1.
        // localhost or 127.0.0.1 refers to the emulated device itself, not the host the emulator is running on.
        final String nickname = intent.getStringExtra("nickname");
        Log.d("채팅", server + nickname);
        // 메인 스레드에서 네트워킹 작업을 하면 안됨. (=> NetworkOnMainThreadException 오류 발생)
        // connectServer 메소드 내에 소켓 생성 작업이 있기 때문에 쓰레드 내에서 실행하도록 함.
        // connectServer 메소드 내에 메시지 수신 쓰레드가 또 있기 때문에 추후 깔끔하게 정리할 필요는 있음.
        new Thread(new Runnable() {
            @Override
            public void run() {
                assert server != null;
                connectServer(server, nickname);

            }
        }).start();
    }

    private void connectServer(String server, String nickname) {
        String[] str = server.split(":");
        try {
            // 서버에 접속하여 소켓 객체 생성
            Socket s = new Socket(str[0], Integer.parseInt(str[1]));
            Log.d("채팅", "서버에 접속 정보" + s);

            // 입출력 스트림 생성 : 안드로이드 - 이클립스
            in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true); //auto flush

            // 1-(1) 스레드를 만들어서 서버에서 받은 데이터를 화면에 출력하자 (안드로이드 - 이클립스)
            new Thread() {
                @Override
                public void run() {
                    receiveData = "";
                    try {
                        while ((receiveData = in.readLine()) != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    msgOut.append(receiveData + "\n");

                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            // 1-(2) 접속하자마자 첫번째 메시지로 로그인 명령 + 닉네임 전달
            out.println("login " + nickname);

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

//            // 입출력 스트림 생성 : MFC - 안드로이드
//            in = new BufferedReader(new InputStreamReader(s.getInputStream(), "euc-kr"), 200);
//            out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true); //auto flush
//
//            // 2-(1) 스레드를 만들어서 서버에서 받은 데이터를 화면에 출력하자 (MFC - 안드로이드)
//            new Thread() {
//                @Override
//                public void run() {
//                    final char[] chars = new char[1024];
//                    try {
//                        // 서버가 나에게 보낸 문자 한 줄을 계속 받아서 처리함.
//                        while ((in.read(chars, 0, 1024)) > 0) {
//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(getApplicationContext(), "Connected with Server!", Toast.LENGTH_SHORT).show();
//                                    receiveData = new String(chars);
//                                    msgOut.append("receiveData : " + receiveData + "\n");
//
//                                }
//                            });
//                        }
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//
//                    }
//                }
//
//            }.start();
//
//            // 2-(2) 접속하자마자 첫번째 메시지로 로그인 명령 + 닉네임 전달
//            out.println(nickname + " has logged in now.");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        }
//    }

    public void setView() {
        msgOut = findViewById(R.id.msgOut);
        msgInput = findViewById(R.id.msgInput);
        sendBtn = findViewById(R.id.sendBtn);

    }

     public void setEvent() {
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMsg();

            }
        });

        msgInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                // 엔터키를 눌렀을때 메시지 보내기
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    sendMsg();
                    return true;

                }
                return false;
            }
        });
    }

    private void sendMsg() {
        // 실제 output 스트림에 메시지 보내기
        // UI 요소에 접근하는 코드가 있기 때문에 핸들러를 통해서 작업
        handler.post(new Runnable() {
            @Override
            public void run() {
                sendingData = msgInput.getText().toString(); // 보낼 텍스트를 sendingData 변수에 저장
                msgInput.setText(""); // 입력 내용 클리어
                msgInput.requestFocus();

                // 메인 스레드에서 네트워킹 작업을 하면 NetworkOnMainThreadException(이) 발생하므로
                // 별도 스레드를 만들어서 out 스트림에 데이터 보내기. AsyncTask 사용 가능
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 네트워크로 입력된 텍스트 보내기
                        if (sendingData != null) {
                            out.println(sendingData);

                        } else {
                            out.println("");

                        }
                    }

                }).start();
            }
        });
    }
}