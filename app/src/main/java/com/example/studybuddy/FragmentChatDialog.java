package com.example.studybuddy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studybuddy.databinding.FragmentChatDialogBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentChatDialog extends DialogFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private SharedPreferences userPref, chatNamePref;
    private Map<String,Object> codeList = new HashMap<>();
    private DocumentReference docRef;

    private String mParam1;
    private String mParam2;

    public FragmentChatDialog() {
        // Required empty public constructor
    }

    public static FragmentChatDialog newInstance(String param1, String param2) {
        FragmentChatDialog fragment = new FragmentChatDialog();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_dialog,container,false);
        userPref = getContext().getSharedPreferences("userData", Context.MODE_PRIVATE);
        chatNamePref = getContext().getSharedPreferences("chatName", Context.MODE_PRIVATE);

        docRef = db.collection(userPref.getString("School","none")).document("chat")
                .collection("chatRoom").document("privateChat");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> data = task.getResult().getData();
                            List<String> list = new ArrayList<>(data.keySet());
                            Log.d("logchk", "onComplete: " + list);
                            for (String chaname : list){
                                Log.d("logchk", "onComplete: " + chaname);
                                docRef.collection(chaname).document("chatSetting").collection("setting")
                                        .document("setting").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        Log.d("logchk", "onComplete: "+ task.getResult().getData().get("chatCode"));
                                        codeList.put(task.getResult().getData().get("chatCode").toString(), chaname);
                                    }
                                });
                            }
                        }
                    }
                });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText editCode = view.findViewById(R.id.editcode);

        TextView cancel = view.findViewById(R.id.no_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        RelativeLayout findcode = view.findViewById(R.id.extralayout);
        TextView chat = view.findViewById(R.id.codechat);
        Button goButton = view.findViewById(R.id.codeokbtn);

        Button okButton = view.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = editCode.getText().toString();
                if (code.isEmpty())
                    Toast.makeText(getContext(), "공백은 입력할 수 없습니다.", Toast.LENGTH_SHORT).show();
                else if (codeList.getOrDefault(code,null) != null){
                    Toast.makeText(getContext(), "해당 채팅방이 존재합니다.", Toast.LENGTH_SHORT).show();
                    Log.d("logchk", "onClick: " + codeList.get(code));
                    chat.setText(codeList.get(code).toString());
                    findcode.setVisibility(View.VISIBLE);
                    goButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Map<String, Object> adduser = new HashMap<>();
                            adduser.put(mAuth.getCurrentUser().getUid(),mAuth.getCurrentUser().getUid());
                            docRef.collection(codeList.get(code).toString()).document("chatSetting").collection("setting")
                                    .document("user").update(adduser);
                            SharedPreferences.Editor editor = chatNamePref.edit();
                            editor.putString("Name",codeList.get(code).toString());
                            editor.putString("open","privateChat");
                            editor.commit();
                            requireActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new ChatRoomFragment())
                                    .commit();
                            dismiss();

                        }
                    });
                } else Toast.makeText(getContext(), "잘못된 코드입니다.", Toast.LENGTH_SHORT).show();
            }
        });

        TextView cancel2 = view.findViewById(R.id.codecancelbtn);
        cancel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chat.setText("");
                findcode.setVisibility(View.GONE);
            }
        });


    }
}