package com.example.safedut;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatWithAIActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText inputEditText;
    private ImageButton sendButton;
    private AIChatAdapter chatAdapter;
    private List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_with_ai);

        recyclerView = findViewById(R.id.recyclerViewChat);
        inputEditText = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.buttonSend);

        messageList = new ArrayList<>();
        chatAdapter = new AIChatAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String userInput = inputEditText.getText().toString().trim();

        if (TextUtils.isEmpty(userInput)) return;

        // Add user message
        messageList.add(new Message(userInput, true));
        chatAdapter.notifyItemInserted(messageList.size() - 1);

        // Generate bot response
        String botReply = getAIResponse(userInput);
        messageList.add(new Message(botReply, false));
        chatAdapter.notifyItemInserted(messageList.size() - 1);

        recyclerView.scrollToPosition(messageList.size() - 1);
        inputEditText.setText("");
    }

    private String getAIResponse(String userInput) {
        userInput = userInput.toLowerCase().trim();

        // General greetings and small talk
        if (userInput.contains("hello") || userInput.contains("hi") || userInput.contains("hey")) {
            return "👋 Hey there! I'm your safety assistant. How can I help you feel more safe or informed today?";
        } else if (userInput.contains("how are you")) {
            return "😊 I'm here and ready to help — thanks for asking! What can I help you with regarding safety?";
        } else if (userInput.contains("what's up") || userInput.contains("sup")) {
            return "👋 Just here making sure you're safe and supported. What’s going on?";
        } else if (userInput.contains("thank you") || userInput.contains("thanks")) {
            return "You're very welcome! Always here when you need advice or support. 🙌";

            // Fire
        } else if (userInput.contains("fire") || userInput.contains("burn") || userInput.contains("smoke") || userInput.contains("flames")) {
            return "🔥 That sounds serious. If there’s a fire risk:\n- Leave the area immediately and calmly.\n- Use the stairs, not the elevator.\n- Call 101 for emergency services.\n- Use a fire extinguisher only if you're trained and it’s safe.";

            // Medical
        } else if (userInput.contains("medical") || userInput.contains("hurt") || userInput.contains("injury") ||
                userInput.contains("bleed") || userInput.contains("sick") || userInput.contains("unconscious") ||
                userInput.contains("fainted") || userInput.contains("ill")) {
            return "🚑 If someone is injured or unwell:\n- Call 112 for an ambulance.\n- Keep the person still and calm.\n- Apply first aid if you know how.\n- Don’t move them unless it’s unsafe to stay where they are.";

            // Suspicious activity
        } else if (userInput.contains("suspicious") || userInput.contains("strange") || userInput.contains("weird") ||
                userInput.contains("creepy") || userInput.contains("unusual") || userInput.contains("activity")) {
            return "👀 If you saw something that doesn’t feel right:\n- Avoid engaging directly.\n- Take mental notes of what you saw.\n- Contact campus security and let them know where and when it happened.";

            // Stalking / being followed
        } else if (userInput.contains("stalking") || userInput.contains("stalker") || userInput.contains("followed") ||
                userInput.contains("someone is following me") || userInput.contains("being followed") ||
                userInput.contains("chased") || userInput.contains("creep") || userInput.contains("i feel watched")) {
            return "🚷 That sounds really scary. If you feel like you’re being followed:\n- Head to a public or secure area right away.\n- Contact campus security or 10111.\n- Try to stay with others and stay visible.\n- Trust your instincts — your safety matters.";

            // Traveling late or feeling unsafe at night
        } else if (userInput.contains("travel") || userInput.contains("late") || userInput.contains("night") ||
                userInput.contains("dark") || userInput.contains("walking home") || userInput.contains("going out at night") ||
                userInput.contains("feel unsafe")) {
            return "🌙 If you're out late or feeling uneasy:\n- Let someone you trust know where you are.\n- Stick to lit, busy paths.\n- Use safe transport options.\n- Keep your phone ready to call for help if needed.";

            // Harassment or abuse
        } else if (userInput.contains("harass") || userInput.contains("abuse") || userInput.contains("threat") ||
                userInput.contains("intimidate") || userInput.contains("violence") || userInput.contains("sexual") ||
                userInput.contains("molest")) {
            return "🚨 I’m really sorry you’re dealing with something like that.\nYou deserve to feel safe and respected.\n- Please report it to campus security or a trusted authority.\n- You can also talk to support services — you're not alone.\n- If you're in danger now, call 10111.";

            // Theft or robbery
        } else if (userInput.contains("stolen") || userInput.contains("robbery") || userInput.contains("theft") ||
                userInput.contains("pickpocket") || userInput.contains("snatched") || userInput.contains("bag taken")) {
            return "👜 If something was stolen:\n- Get to a safe location.\n- Contact campus security immediately.\n- Report what was taken and where.\n- If your phone or card was taken, consider locking or disabling them right away.";

            // Lockdown / gun violence
        } else if (userInput.contains("lockdown") || userInput.contains("shooting") || userInput.contains("gun") ||
                userInput.contains("shooter") || userInput.contains("weapon") || userInput.contains("attack")) {
            return "🔒 If you're in or hearing about a lockdown:\n- Find a secure space to hide.\n- Lock or block the door.\n- Silence your phone, stay low, and stay quiet.\n- Don’t open the door until authorities give the all-clear.";

            // Mental health and emotional distress
        } else if (userInput.contains("depressed") || userInput.contains("mental") || userInput.contains("panic") ||
                userInput.contains("anxious") || userInput.contains("overwhelmed") || userInput.contains("sad") ||
                userInput.contains("feel down") || userInput.contains("mental health") || userInput.contains("crying")) {
            return "🧠 It sounds like you're going through a tough time, and I'm really glad you reached out.\n- You're not alone, and how you feel is valid.\n- Try talking to someone you trust or a mental health counselor.\n- If it ever feels too much, call 112 or your campus support line.";

            // Accidents
        } else if (userInput.contains("accident") || userInput.contains("fall") || userInput.contains("crash") ||
                userInput.contains("injured") || userInput.contains("hit") || userInput.contains("slip")) {
            return "🚧 If there’s been an accident:\n- Check if anyone is hurt and call for help if needed.\n- Keep the area safe and clear.\n- Report the incident to security or emergency responders.";

            // Emergencies in general
        } else if (userInput.contains("emergency") || userInput.contains("urgent") || userInput.contains("danger") ||
                userInput.contains("help me") || userInput.contains("need help")) {
            return "⚠️ I’ve got your back.\n- If it’s a serious emergency, call 10111 or 112 right away.\n- Let someone near you know what’s happening.\n- Stay calm and give your location clearly.";

            // Catch-all fallback
        } else {
            return "🤖 I’m here to help with anything safety-related — big or small.\nYou can ask about things like:\n- What to do in an emergency\n- How to stay safe at night\n- Reporting suspicious behavior\nJust tell me what’s on your mind. 💬";
        }
    }



}
