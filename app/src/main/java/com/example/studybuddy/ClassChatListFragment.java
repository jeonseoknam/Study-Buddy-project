package com.example.studybuddy;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.databinding.FragmentClassChatListBinding;
import com.example.studybuddy.utility.userData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ClassChatListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClassChatListFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private final int SINGLE_CHAT = 0, GROUP_CHAT = 1;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ChatListAdapter adapter;

    FragmentClassChatListBinding binding;

    ArrayList<ChatListItem> chatListItems = new ArrayList<>();

    DocumentReference docRef = db.collection("chatRoom").document("singleChat");

    public ClassChatListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ClassChatListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ClassChatListFragment newInstance(String param1, String param2) {
        ClassChatListFragment fragment = new ClassChatListFragment();
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
        View view = inflater.inflate(R.layout.fragment_class_chat_list, container, false);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        RecyclerView recyclerView = view.findViewById(R.id.chatListRecyclerView);

        adapter = new ChatListAdapter(chatListItems);
        recyclerView.setAdapter(adapter);

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                Map<String, Object> data = value.getData();
                List<String> list = new ArrayList<>(data.keySet());
                list.sort(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return data.get(o2).toString().compareTo(data.get(o1).toString());
                    }
                });
                chatListItems.clear();
                for (String chatList : list){
                    docRef.collection(chatList).document("msg"+data.get(chatList)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Map<String, Object> list = document.getData();
                                    String chatName = chatList;
                                    String message = list.get("message").toString();
                                    String time = list.get("time").toString();
                                    String profile = list.get("profileUrl").toString();
                                    ChatListItem item = new ChatListItem(chatName, message, time, profile);

                                    chatListItems.add(item);
                                    adapter.notifyItemInserted(chatListItems.size() - 1);
                                    recyclerView.scrollToPosition(0);
                                    Log.d("logchk", "onComplete: " + chatListItems);

                                } else {
                                    Log.d("logchk", "No such document");
                                }
                            } else {
                                Log.d("logchk", "get failed with ", task.getException());
                            }
                        }
                    });
                }
            }
        });

        super.onViewCreated(view, savedInstanceState);

    }
    public static String chatName;

    private class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView profile;
        TextView chatname;
        TextView message;
        TextView time;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.chat_profile);
            chatname = itemView.findViewById(R.id.chat_title);
            message = itemView.findViewById(R.id.chat_subtitle);
            time = itemView.findViewById(R.id.chat_time);
        }
    }

    private class ChatListAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private ArrayList<ChatListItem> listItems;

        private ChatListAdapter(ArrayList<ChatListItem> chatListItem) {
            this.listItems = chatListItem;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            ChatListItem item = listItems.get(position);

            holder.chatname.setText(item.chatname);
            holder.message.setText(item.message);
            holder.time.setText(item.time);
            if (item.profileUrl != null)
                Glide.with(getActivity()).load(item.profileUrl).into(holder.profile);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chatName = item.chatname;
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ChatRoomFragment())
                            .commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return listItems.size();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_recycler_view, parent, false);
            return new MyViewHolder(itemView);
        }
    }
}