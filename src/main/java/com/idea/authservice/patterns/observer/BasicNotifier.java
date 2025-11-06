package com.idea.authservice.patterns.observer;

import com.idea.authservice.model.Match;
import com.idea.authservice.patterns.decorator.INotifier;
import lombok.extern.slf4j.Slf4j;

import java.util.Observer;


@Slf4j
public class BasicNotifier implements INotifier {



    @Override
    public void notify(Match match,String message) {
        log.info("[Notifier:Basic] {}", message);
    }

    public void update(Match match, String message) {
        notify(match, message);
    }
}
