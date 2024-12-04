package com.example.studybuddy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateChatFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private SharedPreferences chatNamePref;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public CreateChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateChatFragment newInstance(String param1, String param2) {
        CreateChatFragment fragment = new CreateChatFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_chat,container,false);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        chatNamePref = getContext().getSharedPreferences("chatName", Context.MODE_PRIVATE);

        HashMap<String, Object> chatSetting = new HashMap<>();

        ImageButton backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .remove(CreateChatFragment.this)
                        .commit();
            }
        });
        EditText chatNameText = view.findViewById(R.id.editTextChatRoomName);
        EditText chatCode = view.findViewById(R.id.editClassCode);

        SharedPreferences.Editor editor = chatNamePref.edit();

        RadioButton anonymousButton = view.findViewById(R.id.radioButtonAnonymous);
        RadioButton realNameButton = view.findViewById(R.id.radioButtonRealName);
        RadioButton publicButton = view.findViewById(R.id.radioButtonPublic);
        RadioButton privateButton = view.findViewById(R.id.radioButtonPrivate);
        anonymousButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) chatSetting.put("name", "anonymous");
            }
        });
        realNameButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) chatSetting.put("name", "realName");
            }
        });
        publicButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) chatSetting.put("open", "singleChat");
            }
        });
        privateButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) chatSetting.put("open", "privateChat");
            }
        });

        Button createChatButton = view.findViewById(R.id.btn_createChat);
        createChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (chatNameText.getText().toString().isEmpty()){
                    Toast.makeText(getContext(), "채팅방 이름은 공백일 수 없습니다.", Toast.LENGTH_SHORT).show();
                } else if(chatSetting.size() != 2){
                    Toast.makeText(getContext(), "모든 속성을 선택해야 합니다.", Toast.LENGTH_SHORT).show();
                } else if (chatSetting.get("open").equals("privateChat") && chatCode.getText().toString().isEmpty()){
                    Toast.makeText(getContext(), "비공개의 경우 코드를 반드시 입력해야 합니다.", Toast.LENGTH_SHORT).show();
                } else {
                    CollectionReference colRef = db.collection("chatRoom").document(chatSetting.get("open").toString())
                        .collection("(실명)"+chatNameText.getText().toString()).document("chatSetting")
                        .collection("setting");
                    editor.putString("Name", "(실명)"+chatNameText.getText().toString());
                    editor.putString("open", chatSetting.get("open").toString());
                    if (chatSetting.get("name").equals("anonymous")) {
                        editor.putString("Name", "(익명)" + chatNameText.getText().toString());
                        colRef = db.collection("chatRoom").document(chatSetting.get("open").toString())
                                .collection("(익명)"+chatNameText.getText().toString()).document("chatSetting")
                                .collection("setting");
                    }
                    editor.apply();
                    if (chatSetting.get("open").equals("privateChat")){
                        chatSetting.put("chatCode", chatCode.getText().toString());
                        Map<String , Object> user = new HashMap<>();
                        user.put(mAuth.getCurrentUser().getUid(),mAuth.getCurrentUser().getUid());
                        colRef.document("user").set(user);
                    }
                    colRef.document("setting").set(chatSetting);

                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ChatRoomFragment())
                            .commit();
                }


            }
        });
    }
}