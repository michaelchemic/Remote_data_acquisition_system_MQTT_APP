package com.example.myhome;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class MessageViewModel extends ViewModel {
    private final MutableLiveData<List<String>> messages = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<String>> getMessages() {
        return messages;
    }

    public void addMessage(String message) {
        List<String> currentMessages = messages.getValue();
        if (currentMessages != null) {
            currentMessages.add(message);
            messages.setValue(currentMessages);
        }
    }


}
