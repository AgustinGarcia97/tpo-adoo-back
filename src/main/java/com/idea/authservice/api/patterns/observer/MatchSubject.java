package com.idea.authservice.api.patterns.observer;

public interface MatchSubject {
    void addObserver(MatchObserver observer);
    void notifyObservers(String message);
}