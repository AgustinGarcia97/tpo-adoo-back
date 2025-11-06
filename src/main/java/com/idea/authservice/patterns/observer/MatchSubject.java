package com.idea.authservice.patterns.observer;

public interface MatchSubject {
    void addObserver(MatchObserver observer);
    void notifyObservers(String message);
}