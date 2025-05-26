package com.example.safedut;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AIChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 0;
    private static final int VIEW_TYPE_BOT = 1;

    private final List<Message> messageList;

    public AIChatAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message.isUser()) {
            return VIEW_TYPE_USER;
        } else {
            return VIEW_TYPE_BOT;
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_message, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bot_message, parent, false);
            return new BotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_USER) {
            ((UserViewHolder) holder).bind(message.getText());
        } else {
            ((BotViewHolder) holder).bind(message.getText());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView userMessageTextView;

        public UserViewHolder(View itemView) {
            super(itemView);
            userMessageTextView = itemView.findViewById(R.id.textUserMessage);
        }

        public void bind(String text) {
            userMessageTextView.setText(text);
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        private final TextView botMessageTextView;

        public BotViewHolder(View itemView) {
            super(itemView);
            botMessageTextView = itemView.findViewById(R.id.textBotMessage);
        }

        public void bind(String text) {
            botMessageTextView.setText(text);
        }
    }
}
